package stategraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import lhpn2sbml.parser.LHPNFile;

public class StateGraph {
	private HashMap<String, LinkedList<State>> stateGraph;
	private ArrayList<String> variables;
	private LHPNFile lhpn;

	public StateGraph(LHPNFile lhpn) {
		this.lhpn = lhpn;
		buildStateGraph();
	}

	private void buildStateGraph() {
		stateGraph = new HashMap<String, LinkedList<State>>();
		variables = new ArrayList<String>();
		for (String var : lhpn.getBooleanVars()) {
			variables.add(var);
		}
		for (String var : lhpn.getIntVars()) {
			variables.add(var);
		}
		HashMap<String, String> allVariables = new HashMap<String, String>();
		for (String var : lhpn.getBooleanVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getContVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getIntVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		ArrayList<String> markedPlaces = new ArrayList<String>();
		HashMap<String, Boolean> places = lhpn.getPlaces();
		for (String place : places.keySet()) {
			if (places.get(place)) {
				markedPlaces.add(place);
			}
		}
		LinkedList<State> markings = new LinkedList<State>();
		int counter = 0;
		State state = new State(markedPlaces.toArray(new String[0]), new StateTransitionPair[0],
				"S" + counter, createStateVector(variables, allVariables),
				copyAllVariables(allVariables));
		markings.add(state);
		counter++;
		stateGraph.put(createStateVector(variables, allVariables), markings);
		Stack<Transition> transitionsToFire = new Stack<Transition>();
		for (String transition : lhpn.getTransitionList()) {
			boolean addToStack = true;
			if (lhpn.getEnablingTree(transition) != null
					&& lhpn.getEnablingTree(transition).evaluateExp(allVariables) == 0.0) {
				addToStack = false;
			}
			if (lhpn.getPreset(transition).length != 0) {
				for (String place : lhpn.getPreset(transition)) {
					if (!markedPlaces.contains(place)) {
						addToStack = false;
					}
				}
			}
			else {
				addToStack = false;
			}
			if (addToStack) {
				transitionsToFire.push(new Transition(transition, copyArrayList(markedPlaces),
						state));
			}
		}
		while (transitionsToFire.size() != 0) {
			Transition fire = transitionsToFire.pop();
			markedPlaces = fire.getMarkedPlaces();
			allVariables = copyAllVariables(fire.getParent().getVariables());
			for (String place : lhpn.getPreset(fire.getTransition())) {
				markedPlaces.remove(place);
			}
			for (String place : lhpn.getPostset(fire.getTransition())) {
				markedPlaces.add(place);
			}
			for (String key : allVariables.keySet()) {
				if (lhpn.getBoolAssignTree(fire.getTransition(), key) != null) {
					allVariables.put(key, ""
							+ lhpn.getBoolAssignTree(fire.getTransition(), key)[0]
									.evaluateExp(allVariables));
				}
				if (lhpn.getContAssignTree(fire.getTransition(), key) != null) {
					allVariables.put(key, ""
							+ lhpn.getContAssignTree(fire.getTransition(), key)[0]
									.evaluateExp(allVariables));
				}
				if (lhpn.getIntAssignTree(fire.getTransition(), key) != null) {
					allVariables.put(key, ""
							+ ((int) lhpn.getIntAssignTree(fire.getTransition(), key)[0]
									.evaluateExp(allVariables)));
				}
			}
			if (!stateGraph.containsKey(createStateVector(variables, allVariables))) {
				markings = new LinkedList<State>();
				state = new State(markedPlaces.toArray(new String[0]), new StateTransitionPair[0],
						"S" + counter, createStateVector(variables, allVariables),
						copyAllVariables(allVariables));
				markings.add(state);
				fire.getParent().addNextState(state, fire.getTransition());
				counter++;
				stateGraph.put(createStateVector(variables, allVariables), markings);
				for (String transition : lhpn.getTransitionList()) {
					boolean addToStack = true;
					if (lhpn.getEnablingTree(transition) != null
							&& lhpn.getEnablingTree(transition).evaluateExp(allVariables) == 0.0) {
						addToStack = false;
					}
					if (lhpn.getPreset(transition).length != 0) {
						for (String place : lhpn.getPreset(transition)) {
							if (!markedPlaces.contains(place)) {
								addToStack = false;
							}
						}
					}
					else {
						addToStack = false;
					}
					if (addToStack) {
						transitionsToFire.push(new Transition(transition,
								copyArrayList(markedPlaces), state));
					}
				}
			}
			else {
				markings = stateGraph.get(createStateVector(variables, allVariables));
				boolean add = true;
				boolean same = true;
				for (State mark : markings) {
					for (String place : mark.getMarkings()) {
						if (!markedPlaces.contains(place)) {
							same = false;
						}
					}
					for (String place : markedPlaces) {
						boolean contains = false;
						for (String place2 : mark.getMarkings()) {
							if (place2.equals(place)) {
								contains = true;
							}
						}
						if (!contains) {
							same = false;
						}
					}
					if (same) {
						add = false;
						fire.getParent().addNextState(mark, fire.getTransition());
					}
					same = true;
				}
				if (add) {
					state = new State(markedPlaces.toArray(new String[0]),
							new StateTransitionPair[0], "S" + counter, createStateVector(variables,
									allVariables), copyAllVariables(allVariables));
					markings.add(state);
					fire.getParent().addNextState(state, fire.getTransition());
					counter++;
					stateGraph.put(createStateVector(variables, allVariables), markings);
					for (String transition : lhpn.getTransitionList()) {
						boolean addToStack = true;
						if (lhpn.getEnablingTree(transition) != null
								&& lhpn.getEnablingTree(transition).evaluateExp(allVariables) == 0.0) {
							addToStack = false;
						}
						if (lhpn.getPreset(transition).length != 0) {
							for (String place : lhpn.getPreset(transition)) {
								if (!markedPlaces.contains(place)) {
									addToStack = false;
								}
							}
						}
						else {
							addToStack = false;
						}
						if (addToStack) {
							transitionsToFire.push(new Transition(transition,
									copyArrayList(markedPlaces), state));
						}
					}
				}
			}
		}
	}

	public void performMarkovianAnalysis() {
		State initial = getInitialState();
		if (initial != null) {
			resetColorsFromMarkovianAnalysis();
			int period = findPeriod(initial);
			if (period == 0) {
				period = 1;
			}
			int step = 0;
			initial.setCurrentProb(1.0);
			double tolerance = 0.01;
			boolean done = false;
			do {
				step++;
				step = step % period;
				for (String state : stateGraph.keySet()) {
					for (State m : stateGraph.get(state)) {
						if (m.getColor() % period == step) {
							double transitionSum = 0.0;
							for (StateTransitionPair prev : m.getPrevStatesWithTrans()) {
								if (lhpn.getTransitionRateTree(prev.getTransition()) != null) {
									transitionSum += lhpn.getTransitionRateTree(
											prev.getTransition()).evaluateExp(m.getVariables());
								}
								else {
									transitionSum += 1.0;
								}
								// try {
								// transitionSum +=
								// Double.parseDouble(lhpn.getTransitionRate(prev
								// .getTransition()));
								// }
								// catch (Exception e) {
								// transitionSum += 1;
								// }
							}
							double nextProb = 0.0;
							for (StateTransitionPair prev : m.getPrevStatesWithTrans()) {
								double transProb = 0.0;
								if (lhpn.getTransitionRateTree(prev.getTransition()) != null) {
									transProb = lhpn.getTransitionRateTree(prev.getTransition())
											.evaluateExp(m.getVariables());
								}
								else {
									transProb = 1.0;
								}
								// try {
								// transProb =
								// Double.parseDouble(lhpn.getTransitionRate(prev
								// .getTransition()));
								// }
								// catch (Exception e) {
								// transProb = 1;
								// }
								if (transitionSum != 0.0) {
									transProb /= transitionSum;
								}
								nextProb += (prev.getState().getCurrentProb() * transProb);
							}
							m.setNextProb(nextProb);
						}
					}
				}
				for (String state : stateGraph.keySet()) {
					for (State m : stateGraph.get(state)) {
						if (m.getColor() % period == step) {
							if ((m.getNextProb() > tolerance)
									&& (Math.abs(((m.getCurrentProb() - m.getNextProb()))
											/ m.getCurrentProb()) > tolerance)) {
							}
							else {
								done = true;
							}
							m.setCurrentProbToNext();
						}
					}
				}
			}
			while (!done);
			double totalProb = 0.0;
			for (String state : stateGraph.keySet()) {
				for (State m : stateGraph.get(state)) {
					double transitionSum = 0.0;
					for (StateTransitionPair next : m.getNextStatesWithTrans()) {
						if (lhpn.getTransitionRateTree(next.getTransition()) != null) {
							transitionSum += lhpn.getTransitionRateTree(next.getTransition())
									.evaluateExp(m.getVariables());
						}
						else {
							transitionSum += 1.0;
						}
						// try {
						// transitionSum +=
						// Double.parseDouble(lhpn.getTransitionRate(prev
						// .getTransition()));
						// }
						// catch (Exception e) {
						// transitionSum += 1;
						// }
					}
					if (transitionSum == 0.0) {
						m.setCurrentProb(0.0);
					}
					else {
						m.setCurrentProb((m.getCurrentProb() / period) / transitionSum);
					}
					totalProb += m.getCurrentProb();
				}
			}
			for (String state : stateGraph.keySet()) {
				for (State m : stateGraph.get(state)) {
					if (totalProb == 0.0) {
						m.setCurrentProb(0.0);
					}
					else {
						m.setCurrentProb(m.getCurrentProb() / totalProb);
					}
				}
			}
			resetColors();
		}
	}

	private int findPeriod(State state) {
		int period = 0;
		int color = 0;
		state.setColor(color);
		color = state.getColor() + 1;
		Queue<State> unVisitedStates = new LinkedList<State>();
		for (State s : state.getNextStates()) {
			if (s.getColor() == -1) {
				s.setColor(color);
				unVisitedStates.add(s);
			}
			else {
				if (period == 0) {
					period = (state.getColor() - s.getColor() + 1);
				}
				else {
					period = gcd(state.getColor() - s.getColor() + 1, period);
				}
			}
		}
		while (!unVisitedStates.isEmpty()) {
			state = unVisitedStates.poll();
			color = state.getColor() + 1;
			for (State s : state.getNextStates()) {
				if (s.getColor() == -1) {
					s.setColor(color);
					unVisitedStates.add(s);
				}
				else {
					if (period == 0) {
						period = (state.getColor() - s.getColor() + 1);
					}
					else {
						period = gcd(state.getColor() - s.getColor() + 1, period);
					}
				}
			}
		}
		return period;
	}

	private int gcd(int a, int b) {
		if (b == 0)
			return a;
		return gcd(b, a % b);
	}

	private void resetColorsFromMarkovianAnalysis() {
		for (String state : stateGraph.keySet()) {
			for (State m : stateGraph.get(state)) {
				m.setColor(-1);
			}
		}
	}

	public void resetColors() {
		for (String state : stateGraph.keySet()) {
			for (State m : stateGraph.get(state)) {
				m.setColor(0);
			}
		}
	}

	public State getInitialState() {
		HashMap<String, String> allVariables = new HashMap<String, String>();
		for (String var : lhpn.getBooleanVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getContVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (String var : lhpn.getIntVars()) {
			allVariables.put(var, lhpn.getInitialVal(var));
		}
		for (State s : stateGraph.get(createStateVector(variables, allVariables))) {
			if (s.getID().equals("S0")) {
				return s;
			}
		}
		return null;
	}

	public HashMap<String, LinkedList<State>> getStateGraph() {
		return stateGraph;
	}

	private ArrayList<String> copyArrayList(ArrayList<String> original) {
		ArrayList<String> copy = new ArrayList<String>();
		for (String element : original) {
			copy.add(element);
		}
		return copy;
	}

	private HashMap<String, String> copyAllVariables(HashMap<String, String> original) {
		HashMap<String, String> copy = new HashMap<String, String>();
		for (String s : original.keySet()) {
			copy.put(s, original.get(s));
		}
		return copy;
	}

	private String createStateVector(ArrayList<String> variables,
			HashMap<String, String> allVariables) {
		String vector = "";
		for (String s : variables) {
			if (allVariables.get(s).toLowerCase().equals("true")) {
				vector += "1,";
			}
			else if (allVariables.get(s).toLowerCase().equals("false")) {
				vector += "0,";
			}
			else {
				vector += allVariables.get(s) + ",";
			}
		}
		if (vector.length() > 0) {
			vector = vector.substring(0, vector.length() - 1);
		}
		return vector;
	}

	public void outputStateGraph(String file, boolean withProbs) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write("digraph G {\n");

			for (String state : stateGraph.keySet()) {
				for (State m : stateGraph.get(state)) {
					if (withProbs) {
						out.write(m.getID() + " [shape=\"ellipse\",label=\"<" + state
								+ ">\\nProb = " + m.getCurrentProb() + "\"]\n");
					}
					else {
						out.write(m.getID() + " [shape=\"ellipse\",label=\"<" + state + ">\"]\n");
					}
					for (State next : m.getNextStates()) {
						out.write(m.getID() + " -> " + next.getID() + "\n");
					}
				}
			}
			out.write("}");
			out.close();
		}
		catch (Exception e) {
			System.err.println("Error outputting state graph as dot file.");
		}
	}

	private class Transition {
		private String transition;
		private ArrayList<String> markedPlaces;
		private State parent;

		private Transition(String transition, ArrayList<String> markedPlaces, State parent) {
			this.transition = transition;
			this.markedPlaces = markedPlaces;
			this.parent = parent;
		}

		private String getTransition() {
			return transition;
		}

		private ArrayList<String> getMarkedPlaces() {
			return markedPlaces;
		}

		private State getParent() {
			return parent;
		}
	}

	private class StateTransitionPair {
		private String transition;
		private State state;

		private StateTransitionPair(State state, String transition) {
			this.state = state;
			this.transition = transition;
		}

		private State getState() {
			return state;
		}

		private String getTransition() {
			return transition;
		}
	}

	public class State {
		private String[] markings;
		private StateTransitionPair[] nextStates;
		private StateTransitionPair[] prevStates;
		private String stateVector;
		private String id;
		private int color;
		private double currentProb;
		private double nextProb;
		private HashMap<String, String> variables;

		public State(String[] markings, StateTransitionPair[] nextStates, String id,
				String stateVector, HashMap<String, String> variables) {
			this.markings = markings;
			this.nextStates = nextStates;
			prevStates = new StateTransitionPair[0];
			this.id = id;
			this.stateVector = stateVector;
			color = 0;
			currentProb = 0.0;
			nextProb = 0.0;
			this.variables = variables;
		}

		private String getID() {
			return id;
		}

		private HashMap<String, String> getVariables() {
			return variables;
		}

		private String[] getMarkings() {
			return markings;
		}

		private void setCurrentProb(double probability) {
			currentProb = probability;
		}

		private double getCurrentProb() {
			return currentProb;
		}

		private void setNextProb(double probability) {
			nextProb = probability;
		}

		private double getNextProb() {
			return nextProb;
		}

		private void setCurrentProbToNext() {
			currentProb = nextProb;
		}

		private StateTransitionPair[] getNextStatesWithTrans() {
			return nextStates;
		}

		private StateTransitionPair[] getPrevStatesWithTrans() {
			return prevStates;
		}

		public State[] getNextStates() {
			ArrayList<State> next = new ArrayList<State>();
			for (StateTransitionPair st : nextStates) {
				next.add(st.getState());
			}
			return next.toArray(new State[0]);
		}

		public State[] getPrevStates() {
			ArrayList<State> next = new ArrayList<State>();
			for (StateTransitionPair st : prevStates) {
				next.add(st.getState());
			}
			return next.toArray(new State[0]);
		}

		public int getColor() {
			return color;
		}

		public void setColor(int color) {
			this.color = color;
		}

		public String getStateVector() {
			return stateVector;
		}

		private void addNextState(State nextState, String transition) {
			StateTransitionPair[] newNextStates = new StateTransitionPair[nextStates.length + 1];
			for (int i = 0; i < nextStates.length; i++) {
				newNextStates[i] = nextStates[i];
			}
			newNextStates[newNextStates.length - 1] = new StateTransitionPair(nextState, transition);
			nextStates = newNextStates;
			nextState.addPreviousState(this, transition);
		}

		private void addPreviousState(State prevState, String transition) {
			StateTransitionPair[] newPrevStates = new StateTransitionPair[prevStates.length + 1];
			for (int i = 0; i < prevStates.length; i++) {
				newPrevStates[i] = prevStates[i];
			}
			newPrevStates[newPrevStates.length - 1] = new StateTransitionPair(prevState, transition);
			prevStates = newPrevStates;
		}
	}
}
