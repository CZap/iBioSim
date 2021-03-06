/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.analysis.fba;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FBCReactionPlugin;
import org.sbml.jsbml.ext.fbc.Objective.Type;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import com.joptimizer.optimizers.*;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

/**
 * 
 *
 * @author Chris Myers
 * @author Scott Glass
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class FluxBalanceAnalysis extends CoreObservable
{
	private String root;
	
	private Model model;
	
	private FBCModelPlugin fbc;
	
	private double absError;
	
	private HashMap<String,Double> fluxes;
	
	/**
	 * Constructor for flux balance analysis.
	 * 
	 * @param root - the root directory of the project
	 * @param sbmlFileName - the name of the sbml document 
	 * @param absError - absolute error for the flux balance analysis
	 * 
	 * @throws XMLStreamException - problem with the sbml document
	 * @throws IOException - i/o problem
	 * @throws BioSimException - if the sbml model is invalid.
	 */
	public FluxBalanceAnalysis(String root,String sbmlFileName,double absError) throws XMLStreamException, IOException, BioSimException {
		this.root = root;
		this.absError = absError;
		fluxes = new HashMap<String,Double>();
		SBMLDocument sbml = SBMLutilities.readSBML(root + sbmlFileName, this, null);
		model = sbml.getModel();
		fbc = SBMLutilities.getFBCModelPlugin(sbml.getModel(),true);
	}
	
	/**
	 * Constructor for flux balance analysis.
	 * 
	 * @param model - fbc model object
	 * @param absError - absolute error for the flux balance analysis
	 */
	public FluxBalanceAnalysis(Model model,double absError) {
		this.root = null;
		this.absError = absError;
		fluxes = new HashMap<String,Double>();
		this.model = model;
		fbc = SBMLutilities.getFBCModelPlugin(model,true);
	}
	
	/**
	 * Transforms objective function vector to string.
	 * 
	 * @param objective - coefficients of reaction fluxes for the objective function.
	 * @param reactionIndex - map of reaction id to vector index
	 * 
	 * @return a string for the objective function
	 */
	public static String vectorToString(double[] objective, HashMap<String,Integer> reactionIndex) {
		String result = "";
		for (String reaction : reactionIndex.keySet()) {
			double value = objective[reactionIndex.get(reaction)];
			if (value == 1) {
				if (!result.equals("")) result += " + ";
				result += reaction;
			} else if (value == -1) {
				if (!result.equals("")) result += " + ";
				result += "-" + reaction;
			} else if (value != 0) {
				if (!result.equals("")) result += " + ";
				result += value + "*" + reaction;
			}
		}
		return result;
	}
	
	/**
	 * Performs flux balance analysis on the given FBC model.
	 * 
	 * @return error code for the FBA procedure.
	 */
	public int PerformFluxBalanceAnalysis(){
		if (fbc == null) return -1;
		if (fbc.getNumObjective()==0) return -11;
		if (fbc.getActiveObjective()==null || fbc.getActiveObjective().equals("")) return -12; //TODO: what if missing active?

		HashMap<String, Integer> reactionIndex = new HashMap<String, Integer>();
		int kp = 0;
		
		for (int l = 0; l < model.getReactionCount(); l++) {
			Reaction r = model.getReaction(l);
			FBCReactionPlugin rBounds = (FBCReactionPlugin)r.getExtension(FBCConstants.namespaceURI);
			if (rBounds!=null) {
				if (rBounds.isSetLowerFluxBound()||rBounds.isSetUpperFluxBound()) {
					reactionIndex.put(r.getId(), kp);
					kp++;
				}
			}
		}
		for (int i = 0; i < fbc.getListOfObjectives().size(); i++) {
			if (!fbc.getActiveObjective().equals(fbc.getObjective(i).getId())) continue;
			double [] objective = new double[model.getReactionCount()];				
			for (int j = 0; j < fbc.getObjective(i).getListOfFluxObjectives().size(); j++) {
				if (reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())==null) {
					// no flux bound on objective
					return -9;
				}
				if (fbc.getObjective(i).getType().equals(Type.MINIMIZE)) {
					objective [reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())] = fbc.getObjective(i).getListOfFluxObjectives().get(j).getCoefficient();
				} else {
					objective [reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())] = (-1)*fbc.getObjective(i).getListOfFluxObjectives().get(j).getCoefficient();
				}
			}

			double [] lowerBounds = new double[model.getReactionCount()];
			double [] upperBounds = new double[model.getReactionCount()];
			double minLb = LPPrimalDualMethod.DEFAULT_MIN_LOWER_BOUND;
			double maxUb = LPPrimalDualMethod.DEFAULT_MAX_UPPER_BOUND;
			int m = 0;

			for (int l = 0; l < model.getReactionCount(); l++) {
				Reaction r = model.getReaction(l);
				FBCReactionPlugin rBounds = (FBCReactionPlugin)r.getExtension(FBCConstants.namespaceURI);
				if (rBounds!=null) {
					if (rBounds.isSetLowerFluxBound()) {
						lowerBounds[reactionIndex.get(r.getId())] = rBounds.getLowerFluxBoundInstance().getValue();
						if (Double.isInfinite(lowerBounds[reactionIndex.get(r.getId())])) 
							lowerBounds[reactionIndex.get(r.getId())] = minLb;
					}
					if (rBounds.isSetUpperFluxBound()) {
						upperBounds[reactionIndex.get(r.getId())] = rBounds.getUpperFluxBoundInstance().getValue();
						if (Double.isInfinite(upperBounds[reactionIndex.get(r.getId())])) 
							upperBounds[reactionIndex.get(r.getId())] = maxUb;
					}
				}
			}

			m = 0;
			int nonBoundarySpeciesCount = 0;
			for (int j = 0; j < model.getSpeciesCount(); j++) {
				if (!model.getSpecies(j).getBoundaryCondition()) nonBoundarySpeciesCount++;
			}
			double[][] stoch = new double [nonBoundarySpeciesCount][(model.getReactionCount())];
			double[] zero = new double [nonBoundarySpeciesCount];
			for (int j = 0; j < model.getSpeciesCount(); j++) {
				Species species = model.getSpecies(j);
				if (species.getBoundaryCondition()) continue;
				zero[m] = 0;
				for (int k = 0; k < model.getReactionCount(); k++) {
					Reaction r = model.getReaction(k);
					if (reactionIndex.get(r.getId())==null) {
						// reaction missing flux bound
						return -10;
					}
					for (int l = 0; l < r.getReactantCount(); l++) {
						SpeciesReference sr = r.getReactant(l);
						if (sr.getSpecies().equals(species.getId())) {
							stoch[m][(reactionIndex.get(r.getId()))]=(-1)*sr.getStoichiometry();
						}
					}
					for (int l = 0; l < r.getProductCount(); l++) {
						SpeciesReference sr = r.getProduct(l);
						if (sr.getSpecies().equals(species.getId())) {
							stoch[m][(reactionIndex.get(r.getId()))]=sr.getStoichiometry();
						}
					}
				}
				m++;
			}

			//optimization problem
			LPOptimizationRequest or = new LPOptimizationRequest();
			or.setC(objective);
			or.setA(stoch);
			or.setB(zero);
			or.setLb(lowerBounds);
			or.setUb(upperBounds);
			or.setTolerance(absError);
			or.setToleranceFeas(absError);

			//optimization
			LPPrimalDualMethod opt = new LPPrimalDualMethod();
			opt.setLPOptimizationRequest(or);
			try {
				int error = opt.optimize();
				double [] sol = opt.getLPOptimizationResponse().getSolution();
				if (root!=null) {
					File f = new File(root + "sim-rep.txt");
					FileWriter fw = new FileWriter(f);
					BufferedWriter bw = new BufferedWriter(fw);
					double objkVal = 0;
					double objkCo = 0;
					for (int j = 0; j < fbc.getObjective(i).getListOfFluxObjectives().size(); j++) { 
						objkCo = fbc.getObjective(i).getListOfFluxObjectives().get(j).getCoefficient();
						double scale = Math.round(1/absError);
						objkVal += Math.round(objkCo*sol[reactionIndex.get(fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction())] * scale) / scale;
					}
					String firstLine = ("#total Objective");
					String secondLine = ("100 " + objkVal);
					for (String reaction : reactionIndex.keySet()) {
						double value = sol[reactionIndex.get(reaction)];
						double scale = Math.round(1/absError);
						value = Math.round(value * scale) / scale;  
						firstLine += (" " + reaction);
						secondLine += (" "+ value);
					}
					bw.write(firstLine);
					bw.write("\n");
					bw.write(secondLine);
					bw.write("\n");
					bw.close();
				} else {
					for (String reaction : reactionIndex.keySet()) {
						double value = sol[reactionIndex.get(reaction)];
						double scale = Math.round(1/absError);
						value = Math.round(value * scale) / scale;  
						fluxes.put(reaction, value);
					}
				}
				return error;
			} catch (Exception e) {
				File f = new File(root + "sim-rep.txt");
				if (f.exists()) {
					f.delete();
				}
				if (e.getMessage().equals("initial point must be strictly feasible")) return -2;
				else if (e.getMessage().equals("infeasible problem")) return -3;
				else if (e.getMessage().equals("singular KKT system")) return -4;
				else if (e.getMessage().equals("matrix must have at least one row")) return -5;
				else if (e.getMessage().equals("matrix is singular")) return -6;
				else if (e.getMessage().equals("Equalities matrix A must have full rank")) return -7;
				else {
					System.out.println(e.getMessage());
					return -8;
				}
			}
		}
		return -13;
	}

	/**
	 * Getter for flux distribution.
	 * 
	 * @return Mapping of reaction ids to fluxes.
	 */
	public HashMap<String, Double> getFluxes() {
		return fluxes;
	}
	
	/**
	 * Setter for flux bounds.
	 * 
	 * @param parameter values associated with bounds.
	 */
	public void setBoundParameters(HashMap<String,Double> bounds) {
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter p = model.getParameter(i);
			if (bounds.containsKey(p.getId())) {
				p.setValue(bounds.get(p.getId()));
			}
		}
	}
}