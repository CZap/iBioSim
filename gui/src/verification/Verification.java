package verification;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.*;
import java.util.HashMap;

import lpn.parser.Abstraction;
import lpn.parser.LhpnFile;
import main.*;

import gcm.gui.PropertyList;
import gcm.util.Utility;


/**
 * This class creates a GUI front end for the Verification tool. It provides the
 * necessary options to run an atacs simulation of the circuit and manage the
 * results from the BioSim GUI.
 * 
 * @author Kevin Jones
 */

public class Verification extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -5806315070287184299L;

	private JButton save, run, viewCircuit, viewTrace, viewLog, addComponent,
			removeComponent, addSFile;

	private JLabel algorithm, timingMethod, timingOptions, otherOptions,
			otherOptions2, compilation, bddSizeLabel, advTiming, abstractLabel,
			listLabel;

	public JRadioButton untimed, geometric, posets, bag, bap, baptdc, verify,
			vergate, orbits, search, trace, bdd, dbm, smt, lhpn, view, none,
			simplify, abstractLhpn;

	private JCheckBox abst, partialOrder, dot, verbose, graph, genrg,
			timsubset, superset, infopt, orbmatch, interleav, prune, disabling,
			nofail, noproj, keepgoing, explpn, nochecks, reduction, newTab,
			postProc, redCheck, xForm2, expandRate;

	private JTextField bddSize, backgroundField, componentField, preprocStr;
	
	private JList sList;
	
	private DefaultListModel sListModel;

	private ButtonGroup timingMethodGroup, algorithmGroup, abstractionGroup;

	private String directory, separator, root, verFile, oldBdd,
			sourceFileNoPath;

	public String verifyFile;

	private boolean change, atacs, lema;

	private PropertyList componentList;

	private AbstPane abstPane;

	private Log log;

	private Gui biosim;

	/**
	 * This is the constructor for the Verification class. It initializes all
	 * the input fields, puts them on panels, adds the panels to the frame, and
	 * then displays the frame.
	 */
	public Verification(String directory, String verName, String filename,
			Log log, Gui biosim, boolean lema, boolean atacs) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		} else {
			separator = File.separator;
		}
		this.atacs = atacs;
		this.lema = lema;
		this.biosim = biosim;
		this.log = log;
		this.directory = directory;
		verFile = verName + ".ver";
		String[] tempArray = filename.split("\\.");
		String traceFilename = tempArray[0] + ".trace";
		File traceFile = new File(traceFilename);
		String[] tempDir = directory.split(separator);
		root = tempDir[0];
		for (int i = 1; i < tempDir.length - 1; i++) {
			root = root + separator + tempDir[i];
		}
		this.setMaximumSize(new Dimension(300,300));
		this.setMinimumSize(new Dimension(300,300));

		JPanel abstractionPanel = new JPanel();
		abstractionPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel timingRadioPanel = new JPanel();
		timingRadioPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel timingCheckBoxPanel = new JPanel();
		timingCheckBoxPanel.setMaximumSize(new Dimension(1000, 30));
		JPanel otherPanel = new JPanel();
		otherPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel preprocPanel = new JPanel();
		preprocPanel.setMaximumSize(new Dimension(1000, 700));
		JPanel algorithmPanel = new JPanel();
		algorithmPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel buttonPanel = new JPanel();
		JPanel compilationPanel = new JPanel();
		compilationPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel advancedPanel = new JPanel();
		advancedPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel bddPanel = new JPanel();
		bddPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel pruningPanel = new JPanel();
		pruningPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel advTimingPanel = new JPanel();
		advTimingPanel.setMaximumSize(new Dimension(1000, 62));
		advTimingPanel.setPreferredSize(new Dimension(1000, 62));

		bddSize = new JTextField("");
		bddSize.setPreferredSize(new Dimension(40, 18));
		oldBdd = bddSize.getText();
		componentField = new JTextField("");
		componentList = new PropertyList("");

		abstractLabel = new JLabel("Abstraction:");
		algorithm = new JLabel("Verification Algorithm:");
		timingMethod = new JLabel("Timing Method:");
		timingOptions = new JLabel("Timing Options:");
		otherOptions = new JLabel("Other Options:");
		otherOptions2 = new JLabel("Other Options:");
		compilation = new JLabel("Compilation Options:");
		bddSizeLabel = new JLabel("BDD Linkspace Size:");
		advTiming = new JLabel("Timing Options:");
		//preprocLabel = new JLabel("Preprocess Command:");
		listLabel = new JLabel("Assembly Files:");
		JPanel labelPane = new JPanel();
		labelPane.add(listLabel);
		sListModel = new DefaultListModel();
		sList = new JList(sListModel);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 200));
		scroll.setPreferredSize(new Dimension(276, 132));
		scroll.setViewportView(sList);
		JPanel scrollPane = new JPanel();
		scrollPane.add(scroll);
		addSFile = new JButton("Add File");
		addSFile.addActionListener(this);
		JPanel buttonPane = new JPanel();
		buttonPane.add(addSFile);
		//preprocStr = new JTextField();
		//preprocStr.setPreferredSize(new Dimension(500, 18));

		// Initializes the radio buttons and check boxes
		// Abstraction Options
		none = new JRadioButton("None");
		simplify = new JRadioButton("Simplification");
		abstractLhpn = new JRadioButton("Abstraction");
		// Timing Methods
		if (atacs) {
			untimed = new JRadioButton("Untimed");
			geometric = new JRadioButton("Geometric");
			posets = new JRadioButton("POSETs");
			bag = new JRadioButton("BAG");
			bap = new JRadioButton("BAP");
			baptdc = new JRadioButton("BAPTDC");
			untimed.addActionListener(this);
			geometric.addActionListener(this);
			posets.addActionListener(this);
			bag.addActionListener(this);
			bap.addActionListener(this);
			baptdc.addActionListener(this);
		} else {
			bdd = new JRadioButton("BDD");
			dbm = new JRadioButton("DBM");
			smt = new JRadioButton("SMT");
			bdd.addActionListener(this);
			dbm.addActionListener(this);
			smt.addActionListener(this);
		}
		lhpn = new JRadioButton("LPN");
		view = new JRadioButton("View");
		lhpn.addActionListener(this);
		view.addActionListener(this);
		// Basic Timing Options
		abst = new JCheckBox("Abstract");
		partialOrder = new JCheckBox("Partial Order");
		abst.addActionListener(this);
		partialOrder.addActionListener(this);
		// Other Basic Options
		dot = new JCheckBox("Dot");
		verbose = new JCheckBox("Verbose");
		graph = new JCheckBox("Show State Graph");
		dot.addActionListener(this);
		verbose.addActionListener(this);
		graph.addActionListener(this);
		// Verification Algorithms
		verify = new JRadioButton("Verify");
		vergate = new JRadioButton("Verify Gates");
		orbits = new JRadioButton("Orbits");
		search = new JRadioButton("Search");
		trace = new JRadioButton("Trace");
		verify.addActionListener(this);
		vergate.addActionListener(this);
		orbits.addActionListener(this);
		search.addActionListener(this);
		trace.addActionListener(this);
		// Compilations Options
		newTab = new JCheckBox("New Tab");
		postProc = new JCheckBox("Post Processing");
		redCheck = new JCheckBox("Redundancy Check");
		xForm2 = new JCheckBox("Don't Use Transform 2");
		expandRate = new JCheckBox("Expand Rate");
		newTab.addActionListener(this);
		postProc.addActionListener(this);
		redCheck.addActionListener(this);
		xForm2.addActionListener(this);
		expandRate.addActionListener(this);
		// Advanced Timing Options
		genrg = new JCheckBox("Generate RG");
		timsubset = new JCheckBox("Subsets");
		superset = new JCheckBox("Supersets");
		infopt = new JCheckBox("Infinity Optimization");
		orbmatch = new JCheckBox("Orbits Match");
		interleav = new JCheckBox("Interleave");
		prune = new JCheckBox("Prune");
		disabling = new JCheckBox("Disabling");
		nofail = new JCheckBox("No fail");
		noproj = new JCheckBox("No project");
		keepgoing = new JCheckBox("Keep going");
		explpn = new JCheckBox("Expand LPN");
		genrg.addActionListener(this);
		timsubset.addActionListener(this);
		superset.addActionListener(this);
		infopt.addActionListener(this);
		orbmatch.addActionListener(this);
		interleav.addActionListener(this);
		prune.addActionListener(this);
		disabling.addActionListener(this);
		nofail.addActionListener(this);
		noproj.addActionListener(this);
		keepgoing.addActionListener(this);
		explpn.addActionListener(this);
		// Other Advanced Options
		nochecks = new JCheckBox("No checks");
		reduction = new JCheckBox("Reduction");
		nochecks.addActionListener(this);
		reduction.addActionListener(this);
		// Component List
		addComponent = new JButton("Add Component");
		removeComponent = new JButton("Remove Component");
		GridBagConstraints constraints = new GridBagConstraints();
		JPanel componentPanel = Utility.createPanel(this, "Components",
				componentList, addComponent, removeComponent, null);
		constraints.gridx = 0;
		constraints.gridy = 1;

		abstractionGroup = new ButtonGroup();
		timingMethodGroup = new ButtonGroup();
		algorithmGroup = new ButtonGroup();

		none.setSelected(true);
		if (lema) {
			dbm.setSelected(true);
		} else {
			untimed.setSelected(true);
		}
		verify.setSelected(true);

		// Groups the radio buttons
		abstractionGroup.add(none);
		abstractionGroup.add(simplify);
		abstractionGroup.add(abstractLhpn);
		if (lema) {
			timingMethodGroup.add(bdd);
			timingMethodGroup.add(dbm);
			timingMethodGroup.add(smt);
		} else {
			timingMethodGroup.add(untimed);
			timingMethodGroup.add(geometric);
			timingMethodGroup.add(posets);
			timingMethodGroup.add(bag);
			timingMethodGroup.add(bap);
			timingMethodGroup.add(baptdc);
		}
		timingMethodGroup.add(lhpn);
		timingMethodGroup.add(view);
		algorithmGroup.add(verify);
		algorithmGroup.add(vergate);
		algorithmGroup.add(orbits);
		algorithmGroup.add(search);
		algorithmGroup.add(trace);

		JPanel basicOptions = new JPanel();
		JPanel advOptions = new JPanel();

		// Adds the buttons to their panels
		abstractionPanel.add(abstractLabel);
		abstractionPanel.add(none);
		abstractionPanel.add(simplify);
		abstractionPanel.add(abstractLhpn);
		timingRadioPanel.add(timingMethod);
		if (lema) {
			timingRadioPanel.add(bdd);
			timingRadioPanel.add(dbm);
			timingRadioPanel.add(smt);
		} else {
			timingRadioPanel.add(untimed);
			timingRadioPanel.add(geometric);
			timingRadioPanel.add(posets);
			timingRadioPanel.add(bag);
			timingRadioPanel.add(bap);
			timingRadioPanel.add(baptdc);
		}
		timingRadioPanel.add(lhpn);
		timingRadioPanel.add(view);

		timingCheckBoxPanel.add(timingOptions);
		timingCheckBoxPanel.add(abst);
		timingCheckBoxPanel.add(partialOrder);

		otherPanel.add(otherOptions);
		otherPanel.add(dot);
		otherPanel.add(verbose);
		otherPanel.add(graph);

		preprocPanel.add(labelPane);
		preprocPanel.add(scrollPane);
		preprocPanel.add(buttonPane);

		algorithmPanel.add(algorithm);
		algorithmPanel.add(verify);
		algorithmPanel.add(vergate);
		algorithmPanel.add(orbits);
		algorithmPanel.add(search);
		algorithmPanel.add(trace);

		compilationPanel.add(compilation);
		compilationPanel.add(newTab);
		compilationPanel.add(postProc);
		compilationPanel.add(redCheck);
		compilationPanel.add(xForm2);
		compilationPanel.add(expandRate);

		advTimingPanel.add(advTiming);
		advTimingPanel.add(genrg);
		advTimingPanel.add(timsubset);
		advTimingPanel.add(superset);
		advTimingPanel.add(infopt);
		advTimingPanel.add(orbmatch);
		advTimingPanel.add(interleav);
		advTimingPanel.add(prune);
		advTimingPanel.add(disabling);
		advTimingPanel.add(nofail);
		advTimingPanel.add(noproj);
		advTimingPanel.add(keepgoing);
		advTimingPanel.add(explpn);

		advancedPanel.add(otherOptions2);
		advancedPanel.add(nochecks);
		advancedPanel.add(reduction);

		bddPanel.add(bddSizeLabel);
		bddPanel.add(bddSize);

		// load parameters
		Properties load = new Properties();
		verifyFile = "";
		try {
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + verFile));
			load.load(in);
			in.close();
			if (load.containsKey("verification.file")) {
				verifyFile = load.getProperty("verification.file");
			}
			if (load.containsKey("verification.bddSize")) {
				bddSize.setText(load.getProperty("verification .bddSize"));
			}
			if (load.containsKey("verification.component")) {
				componentField.setText(load
						.getProperty("verification.component"));
			}
			Integer i = 0;
			while (load.containsKey("verification.compList" + i.toString())) {
				componentList.addItem(load.getProperty("verification.compList"
						+ i.toString()));
				i++;
			}
			if (load.containsKey("verification.abstraction")) {
				if (load.getProperty("verification.abstraction").equals("none")) {
					none.setSelected(true);
				} else if (load.getProperty("verification.abstraction").equals(
						"simplify")) {
					simplify.setSelected(true);
				} else {
					abstractLhpn.setSelected(true);
				}
			}
			abstPane = new AbstPane(root + separator + verName, this, log,
					lema, atacs);
			if (load.containsKey("verification.timing.methods")) {
				if (atacs) {
					if (load.getProperty("verification.timing.methods").equals(
							"untimed")) {
						untimed.setSelected(true);
					} else if (load.getProperty("verification.timing.methods")
							.equals("geometric")) {
						geometric.setSelected(true);
					} else if (load.getProperty("verification.timing.methods")
							.equals("posets")) {
						posets.setSelected(true);
					} else if (load.getProperty("verification.timing.methods")
							.equals("bag")) {
						bag.setSelected(true);
					} else if (load.getProperty("verification.timing.methods")
							.equals("bap")) {
						bap.setSelected(true);
					} else if (load.getProperty("verification.timing.methods")
							.equals("baptdc")) {
						baptdc.setSelected(true);
					} else if (load.getProperty("verification.timing.methods")
							.equals("lhpn")) {
						lhpn.setSelected(true);
					} else {
						view.setSelected(true);
					}
				} else {
					if (load.getProperty("verification.timing.methods").equals(
							"bdd")) {
						bdd.setSelected(true);
					} else if (load.getProperty("verification.timing.methods")
							.equals("dbm")) {
						dbm.setSelected(true);
					} else if (load.getProperty("verification.timing.methods")
							.equals("smt")) {
						smt.setSelected(true);
					} else if (load.getProperty("verification.timing.methods")
							.equals("lhpn")) {
						lhpn.setSelected(true);
					} else {
						view.setSelected(true);
					}
				}
			}
			if (load.containsKey("verification.Abst")) {
				if (load.getProperty("verification.Abst").equals("true")) {
					abst.setSelected(true);
				}
			}
			if (load.containsKey("verification.partial.order")) {
				if (load.getProperty("verification.partial.order").equals(
						"true")) {
					partialOrder.setSelected(true);
				}
			}
			if (load.containsKey("verification.Dot")) {
				if (load.getProperty("verification.Dot").equals("true")) {
					dot.setSelected(true);
				}
			}
			if (load.containsKey("verification.Verb")) {
				if (load.getProperty("verification.Verb").equals("true")) {
					verbose.setSelected(true);
				}
			}
			if (load.containsKey("verification.Graph")) {
				if (load.getProperty("verification.Graph").equals("true")) {
					graph.setSelected(true);
				}
			}
			if (load.containsKey("verification.partial.order")) {
				if (load.getProperty("verification.partial.order").equals(
						"true")) {
					partialOrder.setSelected(true);
				}
			}
			if (load.containsKey("verification.partial.order")) {
				if (load.getProperty("verification.partial.order").equals(
						"true")) {
					partialOrder.setSelected(true);
				}
			}
			if (load.containsKey("verification.algorithm")) {
				if (load.getProperty("verification.algorithm").equals("verify")) {
					verify.setSelected(true);
				} else if (load.getProperty("verification.algorithm").equals(
						"vergate")) {
					vergate.setSelected(true);
				} else if (load.getProperty("verification.algorithm").equals(
						"orbits")) {
					orbits.setSelected(true);
				} else if (load.getProperty("verification.algorithm").equals(
						"search")) {
					search.setSelected(true);
				} else if (load.getProperty("verification.algorithm").equals(
						"trace")) {
					trace.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.newTab")) {
				if (load.getProperty("verification.compilation.newTab").equals(
						"true")) {
					newTab.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.postProc")) {
				if (load.getProperty("verification.compilation.postProc")
						.equals("true")) {
					postProc.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.redCheck")) {
				if (load.getProperty("verification.compilation.redCheck")
						.equals("true")) {
					redCheck.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.xForm2")) {
				if (load.getProperty("verification.compilation.xForm2").equals(
						"true")) {
					xForm2.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.expandRate")) {
				if (load.getProperty("verification.compilation.expandRate")
						.equals("true")) {
					expandRate.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.genrg")) {
				if (load.getProperty("verification.timing.genrg")
						.equals("true")) {
					genrg.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.subset")) {
				if (load.getProperty("verification.timing.subset").equals(
						"true")) {
					timsubset.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.superset")) {
				if (load.getProperty("verification.timing.superset").equals(
						"true")) {
					superset.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.infopt")) {
				if (load.getProperty("verification.timing.infopt").equals(
						"true")) {
					infopt.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.orbmatch")) {
				if (load.getProperty("verification.timing.orbmatch").equals(
						"true")) {
					orbmatch.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.interleav")) {
				if (load.getProperty("verification.timing.interleav").equals(
						"true")) {
					interleav.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.prune")) {
				if (load.getProperty("verification.timing.prune")
						.equals("true")) {
					prune.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.disabling")) {
				if (load.getProperty("verification.timing.disabling").equals(
						"true")) {
					disabling.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.nofail")) {
				if (load.getProperty("verification.timing.nofail").equals(
						"true")) {
					nofail.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.noproj")) {
				if (load.getProperty("verification.timing.noproj").equals(
						"true")) {
					nofail.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.explpn")) {
				if (load.getProperty("verification.timing.explpn").equals(
						"true")) {
					explpn.setSelected(true);
				}
			}
			if (load.containsKey("verification.nochecks")) {
				if (load.getProperty("verification.nochecks").equals("true")) {
					nochecks.setSelected(true);
				}
			}
			if (load.containsKey("verification.reduction")) {
				if (load.getProperty("verification.reduction").equals("true")) {
					reduction.setSelected(true);
				}
			}
			if (verifyFile.endsWith(".s")) {
				sListModel.addElement(verifyFile);
			}
			if (load.containsKey("verification.sList")) {
				String concatList = load.getProperty("verification.sList");
				String[] list = concatList.split("\\s");
				for (String s : list) {
					sListModel.addElement(s);
				}
			}
			if (load.containsKey("abstraction.interesting")) {
				String intVars = load.getProperty("abstraction.interesting");
				String[] array = intVars.split(" ");
				for (String s : array) {
					if (!s.equals("")) {
						abstPane.addIntVar(s);
					}
				}
			}
			HashMap<Integer, String> preOrder = new HashMap<Integer, String>();
			HashMap<Integer, String> loopOrder = new HashMap<Integer, String>();
			HashMap<Integer, String> postOrder = new HashMap<Integer, String>();
			boolean containsAbstractions = false;
			for (String s : abstPane.transforms) {
				if (load.containsKey(s)) {
					containsAbstractions = true;
				}
			}
			for (String s : abstPane.transforms) {
				if (load.containsKey("abstraction.transform." + s)) {
					if (load.getProperty("abstraction.transform." + s).contains("preloop")) {
						Pattern prePattern = Pattern.compile("preloop(\\d+)");
						Matcher intMatch = prePattern.matcher(load
								.getProperty("abstraction.transform." + s));
						if (intMatch.find()) {
							Integer index = Integer.parseInt(intMatch.group(1));
							preOrder.put(index, s);
						} else {
							abstPane.addPreXform(s);
						}
					}
					else {
						abstPane.preAbsModel.removeElement(s);
					}
					if (load.getProperty("abstraction.transform." + s).contains("mainloop")) {
						Pattern loopPattern = Pattern
								.compile("mainloop(\\d+)");
						Matcher intMatch = loopPattern.matcher(load
								.getProperty("abstraction.transform." + s));
						if (intMatch.find()) {
							Integer index = Integer.parseInt(intMatch.group(1));
							loopOrder.put(index, s);
						} else {
							abstPane.addLoopXform(s);
						}
					}
					else {
						abstPane.loopAbsModel.removeElement(s);
					}
					if (load.getProperty("abstraction.transform." + s).contains("postloop")) {
						Pattern postPattern = Pattern
								.compile("postloop(\\d+)");
						Matcher intMatch = postPattern.matcher(load
								.getProperty("abstraction.transform." + s));
						if (intMatch.find()) {
							Integer index = Integer.parseInt(intMatch.group(1));
							postOrder.put(index, s);
						} else {
							abstPane.addPostXform(s);
						}
					}
					else {
						abstPane.postAbsModel.removeElement(s);
					}
				}
				else if (containsAbstractions) {
					abstPane.preAbsModel.removeElement(s);
					abstPane.loopAbsModel.removeElement(s);
					abstPane.postAbsModel.removeElement(s);
				}
			}
			if (preOrder.size() > 0) {
				abstPane.preAbsModel.removeAllElements();
			}
			for (Integer j = 0; j < preOrder.size(); j++) {
				abstPane.preAbsModel.addElement(preOrder.get(j));
			}
			if (loopOrder.size() > 0) {
				abstPane.loopAbsModel.removeAllElements();
			}
			for (Integer j = 0; j < loopOrder.size(); j++) {
				abstPane.loopAbsModel.addElement(loopOrder.get(j));
			}
			if (postOrder.size() > 0) {
				abstPane.postAbsModel.removeAllElements();
			}
			for (Integer j = 0; j < postOrder.size(); j++) {
				abstPane.postAbsModel.addElement(postOrder.get(j));
			}
			abstPane.preAbs.setListData(abstPane.preAbsModel.toArray());
			abstPane.loopAbs.setListData(abstPane.loopAbsModel.toArray());
			abstPane.postAbs.setListData(abstPane.postAbsModel.toArray());
			if (load.containsKey("abstraction.transforms")) {
				String xforms = load.getProperty("abstraction.transforms");
				String[] array = xforms.split(", ");
				for (String s : array) {
					if (!s.equals("")) {
						abstPane.addLoopXform(s.replace(",", ""));
					}
				}
			}
			if (load.containsKey("abstraction.factor")) {
				abstPane.factorField.setText(load
						.getProperty("abstraction.factor"));
			}
			if (load.containsKey("abstraction.iterations")) {
				abstPane.iterField.setText(load
						.getProperty("abstraction.iterations"));
			}
			tempArray = verifyFile.split(separator);
			sourceFileNoPath = tempArray[tempArray.length - 1];
			backgroundField = new JTextField(sourceFileNoPath);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}

		// Creates the run button
		run = new JButton("Save and Verify");
		run.addActionListener(this);
		buttonPanel.add(run);
		run.setMnemonic(KeyEvent.VK_S);

		// Creates the save button
		save = new JButton("Save Parameters");
		save.addActionListener(this);
		buttonPanel.add(save);
		save.setMnemonic(KeyEvent.VK_P);

		// Creates the view circuit button
		viewCircuit = new JButton("View Circuit");
		viewCircuit.addActionListener(this);
		viewCircuit.setMnemonic(KeyEvent.VK_C);

		// Creates the view trace button
		viewTrace = new JButton("View Trace");
		viewTrace.addActionListener(this);
		buttonPanel.add(viewTrace);
		if (!traceFile.exists()) {
			viewTrace.setEnabled(false);
		}
		viewTrace.setMnemonic(KeyEvent.VK_T);

		// Creates the view log button
		viewLog = new JButton("View Log");
		viewLog.addActionListener(this);
		buttonPanel.add(viewLog);
		viewLog.setMnemonic(KeyEvent.VK_V);
		viewLog.setEnabled(false);

		JPanel backgroundPanel = new JPanel();
		JLabel backgroundLabel = new JLabel("Model File:");
		tempArray = verifyFile.split(separator);
		JLabel componentLabel = new JLabel("Component:");
		componentField.setPreferredSize(new Dimension(200, 20));
		String sourceFile = tempArray[tempArray.length - 1];
		backgroundField = new JTextField(sourceFile);
		backgroundField.setMaximumSize(new Dimension(200, 20));
		backgroundField.setEditable(false);
		backgroundPanel.add(backgroundLabel);
		backgroundPanel.add(backgroundField);
		if (verifyFile.endsWith(".vhd")) {
			backgroundPanel.add(componentLabel);
			backgroundPanel.add(componentField);
		}
		backgroundPanel.setMaximumSize(new Dimension(500, 30));
		basicOptions.add(backgroundPanel);
		basicOptions.add(abstractionPanel);
		basicOptions.add(timingRadioPanel);
		if (!lema) {
			basicOptions.add(timingCheckBoxPanel);
		}
		basicOptions.add(otherPanel);
		//if (lema) {
		//	basicOptions.add(preprocPanel);
		//}
		if (!lema) {
			basicOptions.add(algorithmPanel);
		}
		if (verifyFile.endsWith(".vhd")) {
			basicOptions.add(componentPanel);
		}
		basicOptions.setLayout(new BoxLayout(basicOptions, BoxLayout.Y_AXIS));
		basicOptions.add(Box.createVerticalGlue());

		advOptions.add(compilationPanel);
		advOptions.add(advTimingPanel);
		advOptions.add(advancedPanel);
		advOptions.add(bddPanel);
		advOptions.setLayout(new BoxLayout(advOptions, BoxLayout.Y_AXIS));
		advOptions.add(Box.createVerticalGlue());

		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Basic Options", basicOptions);
		tab.addTab("Advanced Options", advOptions);
		tab.addTab("Abstraction Options", abstPane);
		tab.setPreferredSize(new Dimension(1000, 480));

		this.setLayout(new BorderLayout());
		this.add(tab, BorderLayout.PAGE_START);
		change = false;
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 * 
	 * @throws
	 * @throws
	 */
	public void actionPerformed(ActionEvent e) {
		change = true;
		if (e.getSource() == run) {
			save(verFile);
			new Thread(this).start();
		} else if (e.getSource() == save) {
			log.addText("Saving:\n" + directory + separator + verFile + "\n");
			save(verFile);
		} else if (e.getSource() == viewCircuit) {
			viewCircuit();
		} else if (e.getSource() == viewTrace) {
			viewTrace();
		} else if (e.getSource() == viewLog) {
			viewLog();
		} else if (e.getSource() == addComponent) {
			String[] vhdlFiles = new File(root).list();
			ArrayList<String> tempFiles = new ArrayList<String>();
			for (int i = 0; i < vhdlFiles.length; i++) {
				if (vhdlFiles[i].endsWith(".vhd")
						&& !vhdlFiles[i].equals(sourceFileNoPath)) {
					tempFiles.add(vhdlFiles[i]);
				}
			}
			vhdlFiles = new String[tempFiles.size()];
			for (int i = 0; i < vhdlFiles.length; i++) {
				vhdlFiles[i] = tempFiles.get(i);
			}
			String filename = (String) JOptionPane.showInputDialog(this, "",
					"Select Component", JOptionPane.PLAIN_MESSAGE, null,
					vhdlFiles, vhdlFiles[0]);
			if (filename != null) {
				String[] comps = componentList.getItems();
				boolean contains = false;
				for (int i = 0; i < comps.length; i++) {
					if (comps[i].equals(filename)) {
						contains = true;
					}
				}
				if (!filename.endsWith(".vhd")) {
					JOptionPane.showMessageDialog(Gui.frame,
							"You must select a valid VHDL file.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} else if (new File(directory + separator + filename).exists()
						|| filename.equals(sourceFileNoPath) || contains) {
					JOptionPane
							.showMessageDialog(
									Gui.frame,
									"This component is already contained in this tool.",
									"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				componentList.addItem(filename);
				return;
			}
		} else if (e.getSource() == removeComponent) {
			if (componentList.getSelectedValue() != null) {
				String selected = componentList.getSelectedValue().toString();
				componentList.removeItem(selected);
				new File(directory + separator + selected).delete();
			}
		} else if (e.getSource() == addSFile) {
			String sFile = JOptionPane.showInputDialog(this, "Enter Assembly File Name:",
					"Assembly File Name", JOptionPane.PLAIN_MESSAGE);
			if ((!sFile.endsWith(".s") && !sFile.endsWith(".inst")) || !(new File(sFile).exists())) {
				JOptionPane.showMessageDialog(this, "Invalid filename entered.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void run() {
		long time1 = System.nanoTime();
		File work = new File(directory);
		if (!preprocStr.getText().equals("")) {
			try {
				String preprocCmd = preprocStr.getText();
				Runtime exec = Runtime.getRuntime();
				Process preproc = exec.exec(preprocCmd, null, work);
				log.addText("Executing:\n" + preprocCmd + "\n");
				preproc.waitFor();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame,
						"Error with preprocessing.", "Error",
						JOptionPane.ERROR_MESSAGE);
				//e.printStackTrace();
				
			}
		}
		copyFile();
		String[] array = directory.split(separator);
		String tempDir = "";
		String lpnFile = "";
		if (!verifyFile.endsWith("lpn")) {
			String[] temp = verifyFile.split("\\.");
			lpnFile = temp[0] + ".lpn";
		} else {
			lpnFile = verifyFile;
		}
		try {
			if (verifyFile.endsWith(".lpn")) {
				Runtime.getRuntime().exec("atacs -llsl " + verifyFile, null,
						work);
			} else if (verifyFile.endsWith(".vhd")) {
				Runtime.getRuntime().exec("atacs -lvsl " + verifyFile, null,
						work);
			} else if (verifyFile.endsWith(".g")) {
				Runtime.getRuntime().exec("atacs -lgsl " + verifyFile, null,
						work);
			}
		} catch (Exception e) {
		}
		for (int i = 0; i < array.length - 1; i++) {
			tempDir = tempDir + array[i] + separator;
		}
		LhpnFile lhpnFile = new LhpnFile();
		lhpnFile.load(directory + separator + lpnFile);
		Abstraction abstraction = lhpnFile.abstractLhpn(this);
		String abstFilename;
		if (lhpn.isSelected()) {
			abstFilename = (String) JOptionPane.showInputDialog(this,
					"Please enter the file name for the abstracted LPN.",
					"Enter Filename", JOptionPane.PLAIN_MESSAGE);
		} else {
			abstFilename = lpnFile.replace(".lpn", "_abs.lpn");
		}
		String sourceFile;
		if (simplify.isSelected() || abstractLhpn.isSelected()) {
			String[] boolVars = lhpnFile.getBooleanVars();
			String[] contVars = lhpnFile.getContVars();
			String[] intVars = lhpnFile.getIntVars();
			String[] variables = new String[boolVars.length + contVars.length
					+ intVars.length];
			int k = 0;
			for (int j = 0; j < contVars.length; j++) {
				variables[k] = contVars[j];
				k++;
			}
			for (int j = 0; j < intVars.length; j++) {
				variables[k] = intVars[j];
				k++;
			}
			for (int j = 0; j < boolVars.length; j++) {
				variables[k] = boolVars[j];
				k++;
			}
			if (abstFilename != null) {
				if (!abstFilename.endsWith(".lpn"))
					abstFilename = abstFilename + ".lpn";
				if (abstPane.loopAbsModel.contains("Remove Variables")) {
					abstraction.abstractVars(abstPane.getIntVars());
				}
				abstraction.abstractSTG(true);
			}
			if (!lhpn.isSelected() && !view.isSelected()) {
				abstraction.save(directory + separator + abstFilename);
			}
			sourceFile = abstFilename;
		} else {
			String[] tempArray = verifyFile.split(separator);
			sourceFile = tempArray[tempArray.length - 1];
		}
		if (!lhpn.isSelected() && !view.isSelected()) {
			abstraction.save(directory + separator + abstFilename);
		}
		if (!lhpn.isSelected() && !view.isSelected()) {
			String[] tempArray = verifyFile.split("\\.");
			String traceFilename = tempArray[0] + ".trace";
			File traceFile = new File(traceFilename);
			String pargName = "";
			String dotName = "";
			if (componentField.getText().trim().equals("")) {
				if (verifyFile.endsWith(".g")) {
					pargName = directory + separator
							+ sourceFile.replace(".g", ".prg");
					dotName = directory + separator
							+ sourceFile.replace(".g", ".dot");
				} else if (verifyFile.endsWith(".lpn")) {
					pargName = directory + separator
							+ sourceFile.replace(".lpn", ".prg");
					dotName = directory + separator
							+ sourceFile.replace(".lpn", ".dot");
				} else if (verifyFile.endsWith(".vhd")) {
					pargName = directory + separator
							+ sourceFile.replace(".vhd", ".prg");
					dotName = directory + separator
							+ sourceFile.replace(".vhd", ".dot");
				}
			} else {
				pargName = directory + separator
						+ componentField.getText().trim() + ".prg";
				dotName = directory + separator
						+ componentField.getText().trim() + ".dot";
			}
			File pargFile = new File(pargName);
			File dotFile = new File(dotName);
			if (traceFile.exists()) {
				traceFile.delete();
			}
			if (pargFile.exists()) {
				pargFile.delete();
			}
			if (dotFile.exists()) {
				dotFile.delete();
			}
			for (String s : componentList.getItems()) {
				try {
					FileInputStream in = new FileInputStream(new File(root
							+ separator + s));
					FileOutputStream out = new FileOutputStream(new File(
							directory + separator + s));
					int read = in.read();
					while (read != -1) {
						out.write(read);
						read = in.read();
					}
					in.close();
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(Gui.frame,
							"Cannot update the file " + s + ".", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			String options = "";
			// BDD Linkspace Size
			if (!bddSize.getText().equals("") && !bddSize.getText().equals("0")) {
				options = options + "-L" + bddSize.getText() + " ";
			}
			options = options + "-oq";
			// Timing method
			if (atacs) {
				if (untimed.isSelected()) {
					options = options + "tu";
				} else if (geometric.isSelected()) {
					options = options + "tg";
				} else if (posets.isSelected()) {
					options = options + "ts";
				} else if (bag.isSelected()) {
					options = options + "tg";
				} else if (bap.isSelected()) {
					options = options + "tp";
				} else {
					options = options + "tt";
				}
			} else {
				if (bdd.isSelected()) {
					options = options + "tB";
				} else if (dbm.isSelected()) {
					options = options + "tL";
				} else if (smt.isSelected()) {
					options = options + "tM";
				}
			}
			// Timing Options
			if (abst.isSelected()) {
				options = options + "oa";
			}
			if (partialOrder.isSelected()) {
				options = options + "op";
			}
			// Other Options
			if (dot.isSelected()) {
				options = options + "od";
			}
			if (verbose.isSelected()) {
				options = options + "ov";
			}
			// Advanced Timing Options
			if (genrg.isSelected()) {
				options = options + "oG";
			}
			if (timsubset.isSelected()) {
				options = options + "oS";
			}
			if (superset.isSelected()) {
				options = options + "oU";
			}
			if (infopt.isSelected()) {
				options = options + "oF";
			}
			if (orbmatch.isSelected()) {
				options = options + "oO";
			}
			if (interleav.isSelected()) {
				options = options + "oI";
			}
			if (prune.isSelected()) {
				options = options + "oP";
			}
			if (disabling.isSelected()) {
				options = options + "oD";
			}
			if (nofail.isSelected()) {
				options = options + "of";
			}
			if (noproj.isSelected()) {
				options = options + "oj";
			}
			if (keepgoing.isSelected()) {
				options = options + "oK";
			}
			if (explpn.isSelected()) {
				options = options + "oL";
			}
			// Other Advanced Options
			if (nochecks.isSelected()) {
				options = options + "on";
			}
			if (reduction.isSelected()) {
				options = options + "oR";
			}
			// Compilation Options
			if (newTab.isSelected()) {
				options = options + "cN";
			}
			if (postProc.isSelected()) {
				options = options + "cP";
			}
			if (redCheck.isSelected()) {
				options = options + "cR";
			}
			if (xForm2.isSelected()) {
				options = options + "cT";
			}
			if (expandRate.isSelected()) {
				options = options + "cE";
			}
			// Load file type
			if (verifyFile.endsWith(".g")) {
				options = options + "lg";
			} else if (verifyFile.endsWith(".lpn")) {
				options = options + "ll";
			} else if (verifyFile.endsWith(".vhd")
					|| verifyFile.endsWith(".vhdl")) {
				options = options + "lvslll";
			}
			// Verification Algorithms
			if (verify.isSelected()) {
				options = options + "va";
			} else if (vergate.isSelected()) {
				options = options + "vg";
			} else if (orbits.isSelected()) {
				options = options + "vo";
			} else if (search.isSelected()) {
				options = options + "vs";
			} else if (trace.isSelected()) {
				options = options + "vt";
			}
			if (graph.isSelected()) {
				options = options + "ps";
			}
			String cmd = "atacs " + options;
			String[] components = componentList.getItems();
			for (String s : components) {
				cmd = cmd + " " + s;
			}
			cmd = cmd + " " + sourceFile;
			if (!componentField.getText().trim().equals("")) {
				cmd = cmd + " " + componentField.getText().trim();
			}
			final JButton cancel = new JButton("Cancel");
			final JFrame running = new JFrame("Progress");
			WindowListener w = new WindowListener() {
				public void windowClosing(WindowEvent arg0) {
					cancel.doClick();
					running.dispose();
				}

				public void windowOpened(WindowEvent arg0) {
				}

				public void windowClosed(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowActivated(WindowEvent arg0) {
				}

				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			running.addWindowListener(w);
			JPanel text = new JPanel();
			JPanel progBar = new JPanel();
			JPanel button = new JPanel();
			JPanel all = new JPanel(new BorderLayout());
			JLabel label = new JLabel("Running...");
			JProgressBar progress = new JProgressBar();
			progress.setIndeterminate(true);
			text.add(label);
			progBar.add(progress);
			button.add(cancel);
			all.add(text, "North");
			all.add(progBar, "Center");
			all.add(button, "South");
			all.setOpaque(true);
			running.setContentPane(all);
			running.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = running.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			running.setLocation(x, y);
			running.setVisible(true);
			running.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			work = new File(directory);
			Runtime exec = Runtime.getRuntime();
			try {
				Preferences biosimrc = Preferences.userRoot();
				String output = "";
				int exitValue = 0;
				if (biosimrc.get("biosim.verification.command", "").equals("")) {
					final Process ver = exec.exec(cmd, null, work);
					cancel.setActionCommand("Cancel");
					cancel.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ver.destroy();
							running.setCursor(null);
							running.dispose();
						}
					});
					biosim.getExitButton().setActionCommand("Exit program");
					biosim.getExitButton().addActionListener(
							new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									ver.destroy();
									running.setCursor(null);
									running.dispose();
								}
							});
					log.addText("Executing:\n" + cmd + "\n");
					InputStream reb = ver.getInputStream();
					InputStreamReader isr = new InputStreamReader(reb);
					BufferedReader br = new BufferedReader(isr);
					FileWriter out = new FileWriter(new File(directory
							+ separator + "run.log"));
					while ((output = br.readLine()) != null) {
						out.write(output);
						out.write("\n");
					}
					out.close();
					br.close();
					isr.close();
					reb.close();
					viewLog.setEnabled(true);
					exitValue = ver.waitFor();
				} else {
					cmd = biosimrc.get("biosim.verification.command", "") + " "
							+ options + " " + sourceFile.replaceAll(".lpn", "");
					final Process ver = exec.exec(cmd, null, work);
					cancel.setActionCommand("Cancel");
					cancel.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ver.destroy();
							running.setCursor(null);
							running.dispose();
						}
					});
					biosim.getExitButton().setActionCommand("Exit program");
					biosim.getExitButton().addActionListener(
							new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									ver.destroy();
									running.setCursor(null);
									running.dispose();
								}
							});
					log.addText("Executing:\n" + cmd + "\n");
					InputStream reb = ver.getInputStream();
					InputStreamReader isr = new InputStreamReader(reb);
					BufferedReader br = new BufferedReader(isr);
					FileWriter out = new FileWriter(new File(directory
							+ separator + "run.log"));
					while ((output = br.readLine()) != null) {
						out.write(output);
						out.write("\n");
					}
					out.close();
					br.close();
					isr.close();
					reb.close();
					viewLog.setEnabled(true);
					exitValue = ver.waitFor();
				}
				long time2 = System.nanoTime();
				long minutes;
				long hours;
				long days;
				double secs = ((time2 - time1) / 1000000000.0);
				long seconds = ((time2 - time1) / 1000000000);
				secs = secs - seconds;
				minutes = seconds / 60;
				secs = seconds % 60 + secs;
				hours = minutes / 60;
				minutes = minutes % 60;
				days = hours / 24;
				hours = hours % 60;
				String time;
				String dayLabel;
				String hourLabel;
				String minuteLabel;
				String secondLabel;
				if (days == 1) {
					dayLabel = " day ";
				} else {
					dayLabel = " days ";
				}
				if (hours == 1) {
					hourLabel = " hour ";
				} else {
					hourLabel = " hours ";
				}
				if (minutes == 1) {
					minuteLabel = " minute ";
				} else {
					minuteLabel = " minutes ";
				}
				if (seconds == 1) {
					secondLabel = " second";
				} else {
					secondLabel = " seconds";
				}
				if (days != 0) {
					time = days + dayLabel + hours + hourLabel + minutes
							+ minuteLabel + secs + secondLabel;
				} else if (hours != 0) {
					time = hours + hourLabel + minutes + minuteLabel + secs
							+ secondLabel;
				} else if (minutes != 0) {
					time = minutes + minuteLabel + secs + secondLabel;
				} else {
					time = secs + secondLabel;
				}
				log.addText("Total Verification Time: " + time + "\n\n");
				running.setCursor(null);
				running.dispose();
				FileInputStream atacsLog = new FileInputStream(new File(
						directory + separator + "atacs.log"));
				InputStreamReader atacsReader = new InputStreamReader(atacsLog);
				BufferedReader atacsBuffer = new BufferedReader(atacsReader);
				boolean success = false;
				while ((output = atacsBuffer.readLine()) != null) {
					if (output.contains("Verification succeeded.")) {
						JOptionPane.showMessageDialog(Gui.frame,
								"Verification succeeded!", "Success",
								JOptionPane.INFORMATION_MESSAGE);
						success = true;
						break;
					}
				}
				if (exitValue == 143) {
					JOptionPane.showMessageDialog(Gui.frame,
							"Verification was" + " canceled by the user.",
							"Canceled Verification", JOptionPane.ERROR_MESSAGE);
				}
				if (!success) {
					if (new File(pargName).exists()) {
						Process parg = exec.exec("parg " + pargName);
						log.addText("parg " + pargName + "\n");
						parg.waitFor();
					} else if (new File(dotName).exists()) {
						String command;
						if (System.getProperty("os.name")
								.contentEquals("Linux")) {
							command = "gnome-open ";
						} else if (System.getProperty("os.name").toLowerCase()
								.startsWith("mac os")) {
							command = "open ";
						} else {
							command = "dotty ";
						}
						Process dot = exec.exec("open " + dotName);
						log.addText(command + dotName + "\n");
						dot.waitFor();
					} else {
						viewLog();
					}
				}
				if (graph.isSelected()) {
					if (dot.isSelected()) {
						String command;
						if (System.getProperty("os.name")
								.contentEquals("Linux")) {
							command = "gnome-open ";
						} else if (System.getProperty("os.name").toLowerCase()
								.startsWith("mac os")) {
							command = "open ";
						} else {
							command = "dotty ";
						}
						exec.exec(command + dotName);
						log.addText("Executing:\n" + command + dotName + "\n");
					} else {
						exec.exec("parg " + pargName);
						log.addText("Executing:\nparg " + pargName + "\n");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame,
						"Unable to verify model.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			if (lhpn.isSelected()) {
				abstraction.save(tempDir + separator + abstFilename);
				biosim.addToTree(abstFilename);
			} else if (view.isSelected()) {
				abstraction.save(directory + separator + abstFilename);
				work = new File(directory + separator);
				try {
					String dotName = abstFilename.replace(".lpn", ".dot");
					new File(directory + separator + dotName).delete();
					Runtime exec = Runtime.getRuntime();
					abstraction.printDot(directory + separator + dotName);
					if (new File(directory + separator + dotName).exists()) {
						String command;
						if (System.getProperty("os.name")
								.contentEquals("Linux")) {
							command = "gnome-open ";
						} else if (System.getProperty("os.name").toLowerCase()
								.startsWith("mac os")) {
							command = "open ";
						} else {
							command = "dotty ";
						}
						Process dot = exec.exec(command + dotName, null, work);
						log.addText(command + dotName + "\n");
						dot.waitFor();
					} else {
						JOptionPane.showMessageDialog(Gui.frame,
								"Unable to view LHPN.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void saveAs() {
		String newName = JOptionPane.showInputDialog(Gui.frame,
				"Enter Verification name:", "Verification Name",
				JOptionPane.PLAIN_MESSAGE);
		if (newName == null) {
			return;
		}
		if (!newName.endsWith(".ver")) {
			newName = newName + ".ver";
		}
		save(newName);
	}

	public void save() {
		save(verFile);
	}

	public void save(String filename) {
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + filename));
			prop.load(in);
			in.close();
			prop.setProperty("verification.file", verifyFile);
			if (!bddSize.equals("")) {
				prop.setProperty("verification.bddSize", this.bddSize.getText()
						.trim());
			}
			if (!componentField.getText().trim().equals("")) {
				prop.setProperty("verification.component", componentField
						.getText().trim());
			} else {
				prop.remove("verification.component");
			}
			String[] components = componentList.getItems();
			if (components.length == 0) {
				for (Object s : prop.keySet()) {
					if (s.toString().startsWith("verification.compList")) {
						prop.remove(s);
					}
				}
			} else {
				for (Integer i = 0; i < components.length; i++) {
					prop.setProperty("verification.compList" + i.toString(),
							components[i]);
				}
			}
			if (none.isSelected()) {
				prop.setProperty("verification.abstraction", "none");
			} else if (simplify.isSelected()) {
				prop.setProperty("verification.abstraction", "simplify");
			} else {
				prop.setProperty("verification.abstraction", "abstract");
			}
			if (atacs) {
				if (untimed.isSelected()) {
					prop.setProperty("verification.timing.methods", "untimed");
				} else if (geometric.isSelected()) {
					prop
							.setProperty("verification.timing.methods",
									"geometric");
				} else if (posets.isSelected()) {
					prop.setProperty("verification.timing.methods", "posets");
				} else if (bag.isSelected()) {
					prop.setProperty("verification.timing.methods", "bag");
				} else if (bap.isSelected()) {
					prop.setProperty("verification.timing.methods", "bap");
				} else if (baptdc.isSelected()) {
					prop.setProperty("verification.timing.methods", "baptdc");
				} else if (lhpn.isSelected()) {
					prop.setProperty("verification.timing.methods", "lhpn");
				} else {
					prop.setProperty("verification.timing.methods", "view");
				}
			} else {
				if (bdd.isSelected()) {
					prop.setProperty("verification.timing.methods", "bdd");
				} else if (dbm.isSelected()) {
					prop.setProperty("verification.timing.methods", "dbm");
				} else if (smt.isSelected()) {
					prop.setProperty("verification.timing.methods", "smt");
				} else if (lhpn.isSelected()) {
					prop.setProperty("verification.timing.methods", "lhpn");
				} else {
					prop.setProperty("verification.timing.methods", "view");
				}
			}
			if (abst.isSelected()) {
				prop.setProperty("verification.Abst", "true");
			} else {
				prop.setProperty("verification.Abst", "false");
			}
			if (partialOrder.isSelected()) {
				prop.setProperty("verification.partial.order", "true");
			} else {
				prop.setProperty("verification.partial.order", "false");
			}
			if (dot.isSelected()) {
				prop.setProperty("verification.Dot", "true");
			} else {
				prop.setProperty("verification.Dot", "false");
			}
			if (verbose.isSelected()) {
				prop.setProperty("verification.Verb", "true");
			} else {
				prop.setProperty("verification.Verb", "false");
			}
			if (graph.isSelected()) {
				prop.setProperty("verification.Graph", "true");
			} else {
				prop.setProperty("verification.Graph", "false");
			}
			if (verify.isSelected()) {
				prop.setProperty("verification.algorithm", "verify");
			} else if (vergate.isSelected()) {
				prop.setProperty("verification.algorithm", "vergate");
			} else if (orbits.isSelected()) {
				prop.setProperty("verification.algorithm", "orbits");
			} else if (search.isSelected()) {
				prop.setProperty("verification.algorithm", "search");
			} else if (trace.isSelected()) {
				prop.setProperty("verification.algorithm", "trace");
			}
			if (newTab.isSelected()) {
				prop.setProperty("verification.compilation.newTab", "true");
			} else {
				prop.setProperty("verification.compilation.newTab", "false");
			}
			if (postProc.isSelected()) {
				prop.setProperty("verification.compilation.postProc", "true");
			} else {
				prop.setProperty("verification.compilation.postProc", "false");
			}
			if (redCheck.isSelected()) {
				prop.setProperty("verification.compilation.redCheck", "true");
			} else {
				prop.setProperty("verification.compilation.redCheck", "false");
			}
			if (xForm2.isSelected()) {
				prop.setProperty("verification.compilation.xForm2", "true");
			} else {
				prop.setProperty("verification.compilation.xForm2", "false");
			}
			if (expandRate.isSelected()) {
				prop.setProperty("verification.compilation.expandRate", "true");
			} else {
				prop
						.setProperty("verification.compilation.expandRate",
								"false");
			}
			if (genrg.isSelected()) {
				prop.setProperty("verification.timing.genrg", "true");
			} else {
				prop.setProperty("verification.timing.genrg", "false");
			}
			if (timsubset.isSelected()) {
				prop.setProperty("verification.timing.subset", "true");
			} else {
				prop.setProperty("verification.timing.subset", "false");
			}
			if (superset.isSelected()) {
				prop.setProperty("verification.timing.superset", "true");
			} else {
				prop.setProperty("verification.timing.superset", "false");
			}
			if (infopt.isSelected()) {
				prop.setProperty("verification.timing.infopt", "true");
			} else {
				prop.setProperty("verification.timing.infopt", "false");
			}
			if (orbmatch.isSelected()) {
				prop.setProperty("verification.timing.orbmatch", "true");
			} else {
				prop.setProperty("verification.timing.orbmatch", "false");
			}
			if (interleav.isSelected()) {
				prop.setProperty("verification.timing.interleav", "true");
			} else {
				prop.setProperty("verification.timing.interleav", "false");
			}
			if (prune.isSelected()) {
				prop.setProperty("verification.timing.prune", "true");
			} else {
				prop.setProperty("verification.timing.prune", "false");
			}
			if (disabling.isSelected()) {
				prop.setProperty("verification.timing.disabling", "true");
			} else {
				prop.setProperty("verification.timing.disabling", "false");
			}
			if (nofail.isSelected()) {
				prop.setProperty("verification.timing.nofail", "true");
			} else {
				prop.setProperty("verification.timing.nofail", "false");
			}
			if (nofail.isSelected()) {
				prop.setProperty("verification.timing.noproj", "true");
			} else {
				prop.setProperty("verification.timing.noproj", "false");
			}
			if (keepgoing.isSelected()) {
				prop.setProperty("verification.timing.keepgoing", "true");
			} else {
				prop.setProperty("verification.timing.keepgoing", "false");
			}
			if (explpn.isSelected()) {
				prop.setProperty("verification.timing.explpn", "true");
			} else {
				prop.setProperty("verification.timing.explpn", "false");
			}
			if (nochecks.isSelected()) {
				prop.setProperty("verification.nochecks", "true");
			} else {
				prop.setProperty("verification.nochecks", "false");
			}
			if (reduction.isSelected()) {
				prop.setProperty("verification.reduction", "true");
			} else {
				prop.setProperty("verification.reduction", "false");
			}
			String intVars = "";
			for (int i = 0; i < abstPane.listModel.getSize(); i++) {
				if (abstPane.listModel.getElementAt(i) != null) {
					intVars = intVars + abstPane.listModel.getElementAt(i)
							+ " ";
				}
			}
			if (sListModel.size() > 0) {
				String list = "";
				for (Object o : sListModel.toArray()) {
					list = list + o + " ";
				}
				list.trim();
				prop.put("verification.sList", list);
			} else {
				prop.remove("verification.sList");
			}
			if (!intVars.equals("")) {
				prop.setProperty("abstraction.interesting", intVars.trim());
			} else {
				prop.remove("abstraction.interesting");
			}
			for (Integer i=0; i<abstPane.preAbsModel.size(); i++) {
				prop.setProperty("abstraction.transform." + abstPane.preAbsModel.getElementAt(i).toString(), "preloop" + i.toString());
			}
			for (Integer i=0; i<abstPane.loopAbsModel.size(); i++) {
				if (abstPane.preAbsModel.contains(abstPane.loopAbsModel.getElementAt(i))) {
					String value = prop.getProperty("abstraction.transform." + abstPane.loopAbsModel.getElementAt(i).toString());
					value = value + "mainloop" + i.toString();
					prop.setProperty("abstraction.transform." + abstPane.loopAbsModel.getElementAt(i).toString(), value);
				}
				else {
					prop.setProperty("abstraction.transform." + abstPane.loopAbsModel.getElementAt(i).toString(), "mainloop" + i.toString());
				}
			}
			for (Integer i=0; i<abstPane.postAbsModel.size(); i++) {
				if (abstPane.preAbsModel.contains(abstPane.postAbsModel.getElementAt(i)) || abstPane.preAbsModel.contains(abstPane.postAbsModel.get(i))) {
					String value = prop.getProperty("abstraction.transform." + abstPane.postAbsModel.getElementAt(i).toString());
					value = value + "postloop" + i.toString();
					prop.setProperty("abstraction.transform." + abstPane.postAbsModel.getElementAt(i).toString(), value);
				}
				else {
					prop.setProperty("abstraction.transform." + abstPane.postAbsModel.getElementAt(i).toString(), "postloop" + i.toString());
				}
			}
			for (String s : abstPane.transforms) {
				if (!abstPane.preAbsModel.contains(s)
						&& !abstPane.loopAbsModel.contains(s)
						&& !abstPane.postAbsModel.contains(s)) {
					prop.remove(s);
				}
			}
			if (!abstPane.factorField.getText().equals("")) {
				prop.setProperty("abstraction.factor", abstPane.factorField
						.getText());
			}
			if (!abstPane.iterField.getText().equals("")) {
				prop.setProperty("abstraction.iterations", abstPane.iterField
						.getText());
			}
			FileOutputStream out = new FileOutputStream(new File(directory
					+ separator + verFile));
			prop.store(out, verifyFile);
			out.close();
			log.addText("Saving Parameter File:\n" + directory + separator
					+ verFile + "\n");
			change = false;
			oldBdd = bddSize.getText();
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to save parameter file!", "Error Saving File",
					JOptionPane.ERROR_MESSAGE);
		}
		for (String s : componentList.getItems()) {
			try {
				new File(directory + separator + s).createNewFile();
				FileInputStream in = new FileInputStream(new File(root
						+ separator + s));
				FileOutputStream out = new FileOutputStream(new File(directory
						+ separator + s));
				int read = in.read();
				while (read != -1) {
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame,
						"Cannot add the selected component.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void reload(String newname) {
	}

	public void viewCircuit() {
		String[] getFilename;
		if (componentField.getText().trim().equals("")) {
			getFilename = verifyFile.split("\\.");
		} else {
			getFilename = new String[1];
			getFilename[0] = componentField.getText().trim();
		}
		String circuitFile = getFilename[0] + ".prs";
		try {
			if (new File(circuitFile).exists()) {
				File log = new File(circuitFile);
				BufferedReader input = new BufferedReader(new FileReader(log));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(500, 500));
				scrolls.setPreferredSize(new Dimension(500, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls,
						"Circuit View", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"No circuit view exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to view circuit.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean getViewTraceEnabled() {
		return viewTrace.isEnabled();
	}

	public boolean getViewLogEnabled() {
		return viewLog.isEnabled();
	}

	public void viewTrace() {
		String[] getFilename = verifyFile.split("\\.");
		String traceFilename = getFilename[0] + ".trace";
		try {
			if (new File(directory + separator + traceFilename).exists()) {
				File log = new File(directory + separator + traceFilename);
				BufferedReader input = new BufferedReader(new FileReader(log));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(500, 500));
				scrolls.setPreferredSize(new Dimension(500, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls,
						"Trace View", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"No trace file exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane
					.showMessageDialog(Gui.frame, "Unable to view trace.",
							"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewLog() {
		try {
			if (new File(directory + separator + "run.log").exists()) {
				File log = new File(directory + separator + "run.log");
				BufferedReader input = new BufferedReader(new FileReader(log));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(500, 500));
				scrolls.setPreferredSize(new Dimension(500, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls, "Run Log",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"No run log exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to view run log.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean hasChanged() {
		if (!oldBdd.equals(bddSize.getText())) {
			return true;
		}
		return change;
	}

	public boolean isSimplify() {
		if (simplify.isSelected())
			return true;
		return false;
	}

	public void copyFile() {
		String[] tempArray = verifyFile.split(separator);
		String sourceFile = tempArray[tempArray.length - 1];
		String[] workArray = directory.split(separator);
		String workDir = "";
		for (int i = 0; i < (workArray.length - 1); i++) {
			workDir = workDir + workArray[i] + separator;
		}
		try {
			File newFile = new File(directory + separator + sourceFile);
			newFile.createNewFile();
			FileOutputStream copyin = new FileOutputStream(newFile);
			FileInputStream copyout = new FileInputStream(new File(workDir
					+ separator + sourceFile));
			int read = copyout.read();
			while (read != -1) {
				copyin.write(read);
				read = copyout.read();
			}
			copyin.close();
			copyout.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Gui.frame, "Cannot copy file "
					+ sourceFile, "Copy Error", JOptionPane.ERROR_MESSAGE);
		}
		/* TODO Test Assembly File compilation */
		if (sourceFile.endsWith(".s") || sourceFile.endsWith(".inst")) {
			try {
				String preprocCmd;
				if (lema) {
					preprocCmd = System.getenv("LEMA") + "/bin/s2lpn " + verifyFile;
				}
				else if (atacs) {
					preprocCmd = System.getenv("ATACS") + "/bin/s2lpn " + verifyFile;
				}
				else {
					preprocCmd = System.getenv("BIOSIM") + "/bin/s2lpn " + verifyFile;
				}
				//for (Object o : sListModel.toArray()) {
				//	preprocCmd = preprocCmd + " " + o.toString();
				//}
				File work = new File(directory);
				Runtime exec = Runtime.getRuntime();
				Process preproc = exec.exec(preprocCmd, null, work);
				log.addText("Executing:\n" + preprocCmd + "\n");
				preproc.waitFor();
				if (verifyFile.endsWith(".s")) {
					verifyFile.replace(".s", ".lpn");
				}
				else {
					verifyFile.replace(".inst", ".lpn");
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame,
						"Error with preprocessing.", "Error",
						JOptionPane.ERROR_MESSAGE);
				//e.printStackTrace();
			}
		}
	}

	public String getVerName() {
		String verName = verFile.replace(".ver", "");
		return verName;
	}

	public AbstPane getAbstPane() {
		return abstPane;
	}

}