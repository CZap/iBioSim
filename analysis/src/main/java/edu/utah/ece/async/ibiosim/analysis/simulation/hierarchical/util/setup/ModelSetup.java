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

package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.Submodel;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalMixedSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.HierarchicalUtilities;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * Used to set up and initialize models in the simulation.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ModelSetup {

  /**
   * Sets up the models for simulation.
   *
   * @param sim
   *          - the hierarchical simulator.
   * @param type
   *          - the simulation type.
   *
   * @throws XMLStreamException
   *           - if there is any issue with the SBML.
   * @throws IOException
   *           - if there is any issue with the input file.
   * @throws BioSimException
   *           - if there is an issue with a model.
   */
  public static void setupModels(HierarchicalSimulation sim, ModelType type) throws XMLStreamException, IOException, BioSimException {
    setupModels(sim, type, null);
  }

  /**
   * Sets up the models for simulation.
   *
   * @param sim
   *          - the hierarchical simulator.
   * @param type
   *          - the simulation type.
   * @param wrapper
   *          - the vector wrapper for the state vector.
   * @throws XMLStreamException
   *           - if there is any issue with the SBML.
   * @throws IOException
   *           - if there is any issue with the input file.
   * @throws BioSimException
   *           - if there is an issue with a model.
   */
  public static void setupModels(HierarchicalSimulation sim, ModelType type, VectorWrapper wrapper) throws XMLStreamException, IOException, BioSimException {

    List<ModelContainer> listOfContainers = new ArrayList<>();
    AnalysisProperties properties = sim.getProperties();
    SBMLDocument document = SBMLReader.read(new File(properties.getFilename()));
    Model model = document.getModel();
    String rootPath = properties.getRoot();
    HierarchicalModel hierarchicalModel = new HierarchicalModel("topmodel", 0);
    sim.setTopmodel(hierarchicalModel);

    LinkedList<ModelContainer> unproc = new LinkedList<>();

    unproc.push(new ModelContainer(model, hierarchicalModel, null, type));
    int count = 0;

    while (!unproc.isEmpty()) {
      ModelContainer container = unproc.pop();
      listOfContainers.add(container);
      sim.addHierarchicalModel(container.getHierarchicalModel());

      if (container.getCompModel() != null) {
        for (Submodel submodel : container.getCompModel().getListOfSubmodels()) {
          model = null;
          CompSBMLDocumentPlugin compDoc = container.getCompDoc();
          if (compDoc != null) {
            if (compDoc.getListOfExternalModelDefinitions() != null && compDoc.getListOfExternalModelDefinitions().get(submodel.getModelRef()) != null) {
              ExternalModelDefinition ext = compDoc.getListOfExternalModelDefinitions().get(submodel.getModelRef());
              String source = ext.getSource();
              String extDef = rootPath + HierarchicalUtilities.separator + source;
              SBMLDocument extDoc = SBMLReader.read(new File(extDef));
              model = extDoc.getModel();
              compDoc = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);

              while (ext.isSetModelRef()) {
                if (compDoc.getExternalModelDefinition(ext.getModelRef()) != null) {
                  ext = compDoc.getListOfExternalModelDefinitions().get(ext.getModelRef());
                  source = ext.getSource().replace("file:", "");
                  extDef = rootPath + HierarchicalUtilities.separator + source;
                  extDoc = SBMLReader.read(new File(extDef));
                  model = extDoc.getModel();
                  compDoc = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);
                } else if (compDoc.getModelDefinition(ext.getModelRef()) != null) {
                  model = compDoc.getModelDefinition(ext.getModelRef());
                  break;
                } else {
                  break;
                }
              }
            } else if (compDoc.getListOfModelDefinitions() != null && compDoc.getListOfModelDefinitions().get(submodel.getModelRef()) != null) {
              model = compDoc.getModelDefinition(submodel.getModelRef());
            }

            if (model != null) {
              hierarchicalModel = new HierarchicalModel(submodel.getId(), ++count);
              unproc.push(new ModelContainer(model, hierarchicalModel, container, type));
            }
          }
        }
      }
    }

    CoreSetup.initializeCore(sim, listOfContainers, sim.getCurrentTime(), wrapper);

    if (sim instanceof HierarchicalMixedSimulator) {
      initializeHybridSimulation((HierarchicalMixedSimulator) sim, listOfContainers);
    }
  }

  private static void initializeHybridSimulation(HierarchicalMixedSimulator sim, List<ModelContainer> listOfContainers) throws IOException, XMLStreamException, BioSimException {

    List<HierarchicalModel> listOfODEModels = new ArrayList<>();
    for (ModelContainer container : listOfContainers) {
      HierarchicalModel state = container.getHierarchicalModel();

      if (state.getModelType() == ModelType.HFBA) {
        sim.createFBASim(state, container.getModel());
      } else if (state.getModelType() == ModelType.HODE) {
        listOfODEModels.add(state);
      }
    }

    sim.createODESim(listOfContainers.get(0).getHierarchicalModel(), listOfODEModels);
  }

}