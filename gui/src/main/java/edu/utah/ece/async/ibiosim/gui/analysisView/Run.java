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
package edu.utah.ece.async.ibiosim.gui.analysisView;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;

import edu.utah.ece.async.ibiosim.analysis.fba.FluxBalanceAnalysis;
import edu.utah.ece.async.ibiosim.analysis.markov.BuildStateGraphThread;
import edu.utah.ece.async.ibiosim.analysis.markov.PerformSteadyStateMarkovAnalysisThread;
import edu.utah.ece.async.ibiosim.analysis.markov.PerformTransientMarkovAnalysisThread;
import edu.utah.ece.async.ibiosim.analysis.markov.StateGraph;
import edu.utah.ece.async.ibiosim.analysis.markov.StateGraph.Property;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation.SimulationType;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.Simulator;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.MutableString;
import edu.utah.ece.async.ibiosim.dataModels.util.dataparser.DataParser;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.graphEditor.Graph;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.ModelEditor;
import edu.utah.ece.async.ibiosim.gui.util.Log;
import edu.utah.ece.async.ibiosim.gui.verificationView.AbstractionPanel;
import edu.utah.ece.async.lema.verification.lpn.Abstraction;
import edu.utah.ece.async.lema.verification.lpn.LPN;
import edu.utah.ece.async.lema.verification.lpn.Translator;

/**
 * This class creates the properties file that is given to the reb2sac program.
 * It also executes the reb2sac program.
 * 
 * @author Curtis Madsen 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Run implements ActionListener, Observer
{

  private Process       reb2sac;
  private final AnalysisView  analysisView;
  private DynamicSimulation     dynSim;
  private Log log;
  private StateGraph          sg;
  private FileWriter logFile;

  private double initialTime, outputStartTime, printInterval, timeLimit, timeStep;
  private long rndSeed;
  private JProgressBar progress;
  private int runs;
  private JLabel progressLabel;
  private JFrame running;
  private String[] intSpecies;
  private double absError;
  private String printer_track_quantity;
  private boolean genStats;
  private JTabbedPane  simTab;

  public Run(AnalysisView analysisView)
  {
    this.analysisView = analysisView;
  }



  /**
   * Executes the reb2sac program. If ODE, monte carlo, or markov is selected,
   * this method creates a Graph object.
   * 
   * @param runTime
   * @param refresh
   * @throws BioSimException 
   * @throws XMLStreamException
   * @throws NumberFormatException
   */
  public int execute(String filename, JRadioButton fba, JRadioButton sbml, JRadioButton dot, JRadioButton xhtml, Component component,
    JRadioButton ode, JRadioButton monteCarlo, String sim, String printer_id, String printer_track_quantity, String outDir,
    JRadioButton nary, int naryRun, String[] intSpecies, Log log, Gui gui, JTabbedPane simTab, String root, JProgressBar progress,
    String simName, ModelEditor modelEditor, String direct, double initialTime, double outputStartTime, double timeLimit, double runTime, String modelFile, AbstractionPanel abstPane,
    JRadioButton abstraction, JRadioButton expandReaction, String lpnProperty, double absError, double relError, double timeStep, double printInterval,
    int runs, long rndSeed, boolean refresh, JLabel progressLabel, JFrame running) throws BioSimException
  {
    this.initialTime = initialTime;
    this.outputStartTime = outputStartTime;
    this.timeLimit = timeLimit;
    this.timeStep = timeStep;
    this.rndSeed = rndSeed;
    this.progress = progress;
    this.progressLabel= progressLabel;
    this.running = running;
    this.intSpecies = intSpecies;
    this.absError = absError;
    this.printer_track_quantity = printer_track_quantity;
    this.simTab = simTab;
    this.runs = runs;

    outDir = outDir.replace("\\", "/");
    filename = filename.replace("\\", "/");
    Runtime exec = Runtime.getRuntime();
    int exitValue = 255;
    this.log = log;
    while (outDir.split("/")[outDir.split("/").length - 1].equals("."))
    {
      outDir = outDir.substring(0, outDir.length() - 1 - outDir.split("/")[outDir.split("/").length - 1].length());
    }
    try
    {
      long time1, time2;
      time2 = -1;
      String directory = "";
      String theFile = "";
      String sbmlName = "";
      if (filename.lastIndexOf('/') >= 0)
      {
        directory = filename.substring(0, filename.lastIndexOf('/') + 1);
        theFile = filename.substring(filename.lastIndexOf('/') + 1);
      }
      if (filename.lastIndexOf('\\') >= 0)
      {
        directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
        theFile = filename.substring(filename.lastIndexOf('\\') + 1);
      }
      File work = new File(directory);
      new FileWriter(new File(directory + GlobalConstants.separator + "running")).close();
      logFile = new FileWriter(new File(directory + GlobalConstants.separator + "log.txt"));
      Properties properties = new Properties();
      properties.load(new FileInputStream(directory + GlobalConstants.separator + theFile.replace(".sbml", "").replace(".xml", "") + ".properties"));
      genStats = Boolean.parseBoolean(properties.getProperty("reb2sac.generate.statistics"));
      String out = theFile;
      if (out.length() > 4 && out.substring(out.length() - 5, out.length()).equals(".sbml"))
      {
        out = out.substring(0, out.length() - 5);
      }
      else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".xml"))
      {
        out = out.substring(0, out.length() - 4);
      }
      
      if (nary.isSelected() && modelEditor != null && (monteCarlo.isSelected() || xhtml.isSelected() || dot.isSelected()))
      {
        exitValue =  executeNary( modelFile,  directory,  simName,  root,  lpnProperty,  abstraction != null ? abstraction.isSelected() : false,  modelEditor,  abstPane,  component);
      }

      if (nary.isSelected() && modelEditor == null && !sim.contains("markov-chain-analysis") && naryRun == 1)
      {
        writeLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=nary-level " + filename);
        time1 = System.nanoTime();
        reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=nary-level " + theFile, Executables.envp, work);
      }
      else if (fba.isSelected())
      {
        time1 = System.nanoTime();
        exitValue =  executeFBA( directory, theFile, refresh,  filename,  printer_id,  outDir,  gui);
      }
      else if (sbml.isSelected())
      {
        time1 = System.nanoTime();
        exitValue = executeSBML(gui, abstPane,  modelEditor,  simName,  root,  directory,  filename,  theFile,  modelFile,  abstraction != null ? abstraction.isSelected() : false,  nary != null ? nary.isSelected() : false,  lpnProperty,  work,  exec,  component);
      }
      else if (dot.isSelected())
      {
        time1 = System.nanoTime();
        exitValue =  executeDot(nary != null ? nary.isSelected() : false, abstraction!= null ? abstraction.isSelected() : false, exec, modelEditor, abstPane, directory, root, theFile, modelFile, simName, filename, out, work);
      }
      else if (xhtml.isSelected())
      {
        writeLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=xhtml --out=" + out + ".xhtml " + filename);
        time1 = System.nanoTime();
        exitValue = executeXhtml( filename,  exec,  directory,  theFile,  out,  work);
      }
      else if (sim.equals("atacs"))
      {
        writeLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=hse2 " + filename);
        time1 = System.nanoTime();
        exitValue = executeAtacs( exec,  theFile,  filename,  outDir,  printer_id,  gui,  refresh,  work);
      }
      else if (sim.equals("prism"))
      {
        time1 = System.nanoTime();
        exitValue = executePrism( modelFile,  root,  directory,  out,  filename,  simName,  lpnProperty,  component,  modelEditor,  exec,  work);
      }
      else if (sim.contains("markov-chain-analysis") || sim.equals("reachability-analysis"))
      {
        time1 = System.nanoTime();
        exitValue = executeMarkov( modelFile,  theFile,  sim,  root,  directory,  filename,  printer_id,  outDir,  modelEditor,  lpnProperty,  component, gui,  refresh);
      }
      else
      {
        time1 = System.nanoTime();
        exitValue = executeSimulation(sim, direct, directory, root, filename, outDir, theFile, abstraction != null && abstraction.isSelected(),  expandReaction != null && expandReaction.isSelected(), exec,  properties, work);
        
      }
      
      updateReb2SacProgress(runTime);
      time2 = System.nanoTime();
      displayError(exitValue);
      
      
      String time = SBMLutilities.createTimeString(time1, time2);
      
      
      writeLog("Total Simulation Time: " + time + " for " + simName + "\n\n");
      running.setCursor(null);
      running.dispose();
      
      
      if (exitValue != 0 && !fba.isSelected())
      {
        
      }
      else
      {
        if (nary.isSelected() && modelEditor == null && naryRun == 1)
        {
          
        }
        else if (sbml.isSelected())
        {
          if (sbmlName != null && !sbmlName.trim().equals(""))
          {
            if (!gui.updateOpenModelEditor(sbmlName))
            {
              try
              {
                ModelEditor gcm = new ModelEditor(root + GlobalConstants.separator, sbmlName, gui, log, false, null, null, null, false, false);
                gui.addTab(sbmlName, gcm, "Model Editor");
                gui.addToTree(sbmlName);
              }
              catch (Exception e)
              {
                e.printStackTrace();
              }
            }
            else
            {
              gui.getTab().setSelectedIndex(gui.getTab(sbmlName));
            }
            gui.enableTabMenu(gui.getTab().getSelectedIndex());
          }
        }
        else if (xhtml.isSelected())
        {
        }
        else if (sim.equals("prism"))
        {
         
        }
        else if (sim.equals("atacs"))
        {
          
        }
        else
        {
          if (ode.isSelected())
          {
            for (int i = 0; i < simTab.getComponentCount(); i++)
            {
              if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
              {
                if (simTab.getComponentAt(i) instanceof Graph)
                {
                  boolean outputM = true;
                  boolean outputV = true;
                  boolean outputS = true;
                  boolean outputTerm = false;
                  boolean warning = false;
                  ArrayList<String> run = new ArrayList<String>();
                  for (String f : work.list())
                  {
                    if (f.contains("mean"))
                    {
                      outputM = false;
                    }
                    else if (f.contains("variance"))
                    {
                      outputV = false;
                    }
                    else if (f.contains("standard_deviation"))
                    {
                      outputS = false;
                    }
                    if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
                    {
                      run.add(f);
                    }
                    else if (f.equals("term-time.txt"))
                    {
                      outputTerm = true;
                    }
                  }
                  if (genStats && (outputM || outputV || outputS))
                  {
                    warning = ((Graph) simTab.getComponentAt(i)).getWarning();
                    ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
                  }
                  new File(directory + GlobalConstants.separator + "running").delete();
                  logFile.close();
                  if (outputTerm)
                  {
                    ArrayList<String> dataLabels = new ArrayList<String>();
                    dataLabels.add("time");
                    ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
                    if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
                    {
                      try
                      {
                        Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                        if (s.hasNextLine())
                        {
                          String[] ss = s.nextLine().split(" ");
                          if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
                          {
                          }
                          else
                          {
                            for (String add : ss)
                            {
                              if (!add.equals("#total") && !add.equals("time-limit"))
                              {
                                dataLabels.add(add);
                                ArrayList<Double> times = new ArrayList<Double>();
                                terms.add(times);
                              }
                            }
                          }
                        }
                        s.close();
                      }
                      catch (Exception e)
                      {
                        e.printStackTrace();
                      }
                    }
                    Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
                    while (scan.hasNextLine())
                    {
                      String line = scan.nextLine();
                      String[] term = line.split(" ");
                      if (!dataLabels.contains(term[0]))
                      {
                        dataLabels.add(term[0]);
                        ArrayList<Double> times = new ArrayList<Double>();
                        times.add(Double.parseDouble(term[1]));
                        terms.add(times);
                      }
                      else
                      {
                        terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
                      }
                    }
                    scan.close();
                    ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
                    ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
                    for (int j = 0; j < dataLabels.size(); j++)
                    {
                      ArrayList<Double> temp = new ArrayList<Double>();
                      temp.add(0.0);
                      data.add(temp);
                      temp = new ArrayList<Double>();
                      temp.add(0.0);
                      percentData.add(temp);
                    }
                    for (double j = printInterval; j <= timeLimit; j += printInterval)
                    {
                      data.get(0).add(j);
                      percentData.get(0).add(j);
                      for (int k = 1; k < dataLabels.size(); k++)
                      {
                        data.get(k).add(data.get(k).get(data.get(k).size() - 1));
                        percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
                        for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
                        {
                          if (terms.get(k - 1).get(l) < j)
                          {
                            data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
                            percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
                            terms.get(k - 1).remove(l);
                          }
                        }
                      }
                    }
                    DataParser probData = new DataParser(dataLabels, data);
                    probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
                    probData = new DataParser(dataLabels, percentData);
                    probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
                  }
                  if (refresh)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                }
                else
                {
                  simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, true, false));
                  boolean outputM = true;
                  boolean outputV = true;
                  boolean outputS = true;
                  boolean outputTerm = false;
                  boolean warning = false;
                  ArrayList<String> run = new ArrayList<String>();
                  for (String f : work.list())
                  {
                    if (f.contains("mean"))
                    {
                      outputM = false;
                    }
                    else if (f.contains("variance"))
                    {
                      outputV = false;
                    }
                    else if (f.contains("standard_deviation"))
                    {
                      outputS = false;
                    }
                    if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
                    {
                      run.add(f);
                    }
                    else if (f.equals("term-time.txt"))
                    {
                      outputTerm = true;
                    }
                  }
                  if (genStats && (outputM || outputV || outputS))
                  {
                    warning = ((Graph) simTab.getComponentAt(i)).getWarning();
                    ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
                  }
                  new File(directory + GlobalConstants.separator + "running").delete();
                  logFile.close();
                  if (outputTerm)
                  {
                    ArrayList<String> dataLabels = new ArrayList<String>();
                    dataLabels.add("time");
                    ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
                    if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
                    {
                      try
                      {
                        Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                        if (s.hasNextLine())
                        {
                          String[] ss = s.nextLine().split(" ");
                          if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
                          {
                          }
                          else
                          {
                            for (String add : ss)
                            {
                              if (!add.equals("#total") && !add.equals("time-limit"))
                              {
                                dataLabels.add(add);
                                ArrayList<Double> times = new ArrayList<Double>();
                                terms.add(times);
                              }
                            }
                          }
                        }
                        s.close();
                      }
                      catch (Exception e)
                      {
                        e.printStackTrace();
                      }
                    }
                    Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
                    while (scan.hasNextLine())
                    {
                      String line = scan.nextLine();
                      String[] term = line.split(" ");
                      if (!dataLabels.contains(term[0]))
                      {
                        dataLabels.add(term[0]);
                        ArrayList<Double> times = new ArrayList<Double>();
                        times.add(Double.parseDouble(term[1]));
                        terms.add(times);
                      }
                      else
                      {
                        terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
                      }
                    }
                    scan.close();
                    ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
                    ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
                    for (int j = 0; j < dataLabels.size(); j++)
                    {
                      ArrayList<Double> temp = new ArrayList<Double>();
                      temp.add(0.0);
                      data.add(temp);
                      temp = new ArrayList<Double>();
                      temp.add(0.0);
                      percentData.add(temp);
                    }
                    for (double j = printInterval; j <= timeLimit; j += printInterval)
                    {
                      data.get(0).add(j);
                      percentData.get(0).add(j);
                      for (int k = 1; k < dataLabels.size(); k++)
                      {
                        data.get(k).add(data.get(k).get(data.get(k).size() - 1));
                        percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
                        for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
                        {
                          if (terms.get(k - 1).get(l) < j)
                          {
                            data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
                            percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
                            terms.get(k - 1).remove(l);
                          }
                        }
                      }
                    }
                    DataParser probData = new DataParser(dataLabels, data);
                    probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
                    probData = new DataParser(dataLabels, percentData);
                    probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
                  }
                  simTab.getComponentAt(i).setName("TSD Graph");
                }
              }
              if (refresh)
              {
                if (simTab.getComponentAt(i).getName().equals("Histogram"))
                {
                  if (simTab.getComponentAt(i) instanceof Graph)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                  else
                  {
                    if (new File(filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "sim-rep.txt").exists())
                    {
                      simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
                      simTab.getComponentAt(i).setName("Histogram");
                    }
                  }
                }
              }
            }
          }
          else if (monteCarlo.isSelected())
          {
            for (int i = 0; i < simTab.getComponentCount(); i++)
            {
              if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
              {
                if (simTab.getComponentAt(i) instanceof Graph)
                {
                  boolean outputM = true;
                  boolean outputV = true;
                  boolean outputS = true;
                  boolean outputTerm = false;
                  boolean warning = false;
                  ArrayList<String> run = new ArrayList<String>();
                  for (String f : work.list())
                  {
                    if (f.contains("mean"))
                    {
                      outputM = false;
                    }
                    else if (f.contains("variance"))
                    {
                      outputV = false;
                    }
                    else if (f.contains("standard_deviation"))
                    {
                      outputS = false;
                    }
                    if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
                    {
                      run.add(f);
                    }
                    else if (f.equals("term-time.txt"))
                    {
                      outputTerm = true;
                    }
                  }
                  if (genStats && (outputM || outputV || outputS))
                  {
                    warning = ((Graph) simTab.getComponentAt(i)).getWarning();
                    ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
                  }
                  new File(directory + GlobalConstants.separator + "running").delete();
                  logFile.close();
                  if (outputTerm)
                  {
                    ArrayList<String> dataLabels = new ArrayList<String>();
                    dataLabels.add("time");
                    ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
                    if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
                    {
                      try
                      {
                        Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                        if (s.hasNextLine())
                        {
                          String[] ss = s.nextLine().split(" ");
                          if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
                          {
                          }
                          else
                          {
                            for (String add : ss)
                            {
                              if (!add.equals("#total") && !add.equals("time-limit"))
                              {
                                dataLabels.add(add);
                                ArrayList<Double> times = new ArrayList<Double>();
                                terms.add(times);
                              }
                            }
                          }
                        }
                        s.close();
                      }
                      catch (Exception e)
                      {
                        e.printStackTrace();
                      }
                    }
                    Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
                    while (scan.hasNextLine())
                    {
                      String line = scan.nextLine();
                      String[] term = line.split(" ");
                      if (!dataLabels.contains(term[0]))
                      {
                        dataLabels.add(term[0]);
                        ArrayList<Double> times = new ArrayList<Double>();
                        times.add(Double.parseDouble(term[1]));
                        terms.add(times);
                      }
                      else
                      {
                        terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
                      }
                    }
                    scan.close();
                    ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
                    ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
                    for (int j = 0; j < dataLabels.size(); j++)
                    {
                      ArrayList<Double> temp = new ArrayList<Double>();
                      temp.add(0.0);
                      data.add(temp);
                      temp = new ArrayList<Double>();
                      temp.add(0.0);
                      percentData.add(temp);
                    }
                    for (double j = printInterval; j <= timeLimit; j += printInterval)
                    {
                      data.get(0).add(j);
                      percentData.get(0).add(j);
                      for (int k = 1; k < dataLabels.size(); k++)
                      {
                        data.get(k).add(data.get(k).get(data.get(k).size() - 1));
                        percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
                        for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
                        {
                          if (terms.get(k - 1).get(l) < j)
                          {
                            data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
                            percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
                            terms.get(k - 1).remove(l);
                          }
                        }
                      }
                    }
                    DataParser probData = new DataParser(dataLabels, data);
                    probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
                    probData = new DataParser(dataLabels, percentData);
                    probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
                  }
                  if (refresh)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                }
                else
                {
                  simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, true, false));
                  boolean outputM = true;
                  boolean outputV = true;
                  boolean outputS = true;
                  boolean outputTerm = false;
                  boolean warning = false;
                  ArrayList<String> run = new ArrayList<String>();
                  for (String f : work.list())
                  {
                    if (f.contains("mean"))
                    {
                      outputM = false;
                    }
                    else if (f.contains("variance"))
                    {
                      outputV = false;
                    }
                    else if (f.contains("standard_deviation"))
                    {
                      outputS = false;
                    }
                    if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
                    {
                      run.add(f);
                    }
                    else if (f.equals("term-time.txt"))
                    {
                      outputTerm = true;
                    }
                  }
                  if (genStats && (outputM || outputV || outputS))
                  {
                    warning = ((Graph) simTab.getComponentAt(i)).getWarning();
                    ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
                  }
                  new File(directory + GlobalConstants.separator + "running").delete();
                  logFile.close();
                  if (outputTerm)
                  {
                    ArrayList<String> dataLabels = new ArrayList<String>();
                    dataLabels.add("time");
                    ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
                    if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
                    {
                      try
                      {
                        Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                        if (s.hasNextLine())
                        {
                          String[] ss = s.nextLine().split(" ");
                          if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
                          {
                          }
                          else
                          {
                            for (String add : ss)
                            {
                              if (!add.equals("#total") && !add.equals("time-limit"))
                              {
                                dataLabels.add(add);
                                ArrayList<Double> times = new ArrayList<Double>();
                                terms.add(times);
                              }
                            }
                          }
                        }
                        s.close();
                      }
                      catch (Exception e)
                      {
                        e.printStackTrace();
                      }
                    }
                    Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
                    while (scan.hasNextLine())
                    {
                      String line = scan.nextLine();
                      String[] term = line.split(" ");
                      if (!dataLabels.contains(term[0]))
                      {
                        dataLabels.add(term[0]);
                        ArrayList<Double> times = new ArrayList<Double>();
                        times.add(Double.parseDouble(term[1]));
                        terms.add(times);
                      }
                      else
                      {
                        terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
                      }
                    }
                    scan.close();
                    ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
                    ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
                    for (int j = 0; j < dataLabels.size(); j++)
                    {
                      ArrayList<Double> temp = new ArrayList<Double>();
                      temp.add(0.0);
                      data.add(temp);
                      temp = new ArrayList<Double>();
                      temp.add(0.0);
                      percentData.add(temp);
                    }
                    for (double j = printInterval; j <= timeLimit; j += printInterval)
                    {
                      data.get(0).add(j);
                      percentData.get(0).add(j);
                      for (int k = 1; k < dataLabels.size(); k++)
                      {
                        data.get(k).add(data.get(k).get(data.get(k).size() - 1));
                        percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
                        for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
                        {
                          if (terms.get(k - 1).get(l) < j)
                          {
                            data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
                            percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
                            terms.get(k - 1).remove(l);
                          }
                        }
                      }
                    }
                    DataParser probData = new DataParser(dataLabels, data);
                    probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
                    probData = new DataParser(dataLabels, percentData);
                    probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
                  }
                  simTab.getComponentAt(i).setName("TSD Graph");
                }
              }
              if (refresh)
              {
                if (simTab.getComponentAt(i).getName().equals("Histogram"))
                {
                  if (simTab.getComponentAt(i) instanceof Graph)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                  else
                  {
                    if (new File(filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "sim-rep.txt").exists())
                    {
                      simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
                      simTab.getComponentAt(i).setName("Histogram");
                    }
                  }
                }
              }
            }
          }
        }
      }
      new File(directory + GlobalConstants.separator + "running").delete();
      logFile.close();
    }
    catch (InterruptedException e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Error In Execution!", "Error In Execution", JOptionPane.ERROR_MESSAGE);
      e1.printStackTrace();
    }
    catch (IOException e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "File I/O Error!", "File I/O Error", JOptionPane.ERROR_MESSAGE);
      e1.printStackTrace();
    }
    catch (XMLStreamException e) {
      JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
    return exitValue;
  }

  
  private int executeAtacs(Runtime exec, String theFile, String filename, String outDir, String printer_id, Gui gui, boolean refresh, File work) throws IOException, InterruptedException
  {
    int exitValue = 0;
    reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=hse2 " + theFile, Executables.envp, work);
    writeLog("Executing:\natacs -T0.000001 -oqoflhsgllvA " + filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "out.hse");
    exec.exec("atacs -T0.000001 -oqoflhsgllvA out.hse", null, work);
    if (refresh)
    {
      for (int i = 0; i < simTab.getComponentCount(); i++)
      {
        if (simTab.getComponentAt(i).getName().equals("Histogram"))
        {
          if (simTab.getComponentAt(i) instanceof Graph)
          {
            ((Graph) simTab.getComponentAt(i)).refresh();
          }
          else
          {
            simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
            simTab.getComponentAt(i).setName("Histogram");
          }
        }
      }
    }
    
    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }
    
    return exitValue;
  }
  private int executeNary(String modelFile, String directory, String simName, String root, String lpnProperty, boolean abstraction, ModelEditor modelEditor, AbstractionPanel abstPane, Component component) throws XMLStreamException, IOException
  {
    String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
    ArrayList<String> specs = new ArrayList<String>();
    ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
    for (int i = 0; i < intSpecies.length; i++)
    {
      if (!intSpecies[i].equals(""))
      {
        String[] split = intSpecies[i].split(" ");
        if (split.length > 1)
        {
          String[] levels = split[1].split(",");
          if (levels.length > 0)
          {
            specs.add(split[0]);
            conLevel.add(levels);
          }
        }
      }
    }
    BioModel bioModel = new BioModel(root);
    bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
    String prop = null;
    if (!lpnProperty.equals(""))
    {
      prop = lpnProperty;
    }
    ArrayList<String> propList = new ArrayList<String>();
    if (prop == null)
    {
      Model m = bioModel.getSBMLDocument().getModel();
      for (int num = 0; num < m.getConstraintCount(); num++)
      {
        String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
        if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
        {
          propList.add(constraint);
        }
      }
    }
    if (propList.size() > 0)
    {
      String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
      if ((s != null) && (s.length() > 0))
      {
        Model m = bioModel.getSBMLDocument().getModel();
        for (int num = 0; num < m.getConstraintCount(); num++)
        {
          String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
          if (s.equals(constraint))
          {
            prop = Translator.convertProperty(m.getConstraint(num).getMath());
          }
        }
      }
    }
    MutableString mutProp = new MutableString(prop);
    LPN lpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
    prop = mutProp.getString();
    if (lpnFile == null)
    {
      new File(directory + GlobalConstants.separator + "running").delete();
      logFile.close();
      return 0;
    }
    lpnFile.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
    try
    {
      Translator t1 = new Translator();
      if (abstraction)
      {
        LPN lhpnFile = new LPN();
        lhpnFile.load(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
        Abstraction abst = new Abstraction(lhpnFile, abstPane.getAbstractionProperty());
        abst.addObserver(this);
        abst.abstractSTG(false);
        abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp");
        t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp", prop);
      }
      else
      {
        t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName, prop);
      }
      t1.setFilename(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName.replace(".lpn", ".xml"));
      t1.outputSBML();
    }
    catch(BioSimException e)
    {
      e.printStackTrace();
    }
    return 0;
  }
  
  private int executeFBA(String directory, String theFile, boolean refresh, String filename, String printer_id, String outDir, Gui gui) throws XMLStreamException, IOException
  {
    int exitValue = 255;

    FluxBalanceAnalysis fluxBalanceAnalysis = new FluxBalanceAnalysis(directory + GlobalConstants.separator, theFile, absError);
    exitValue = fluxBalanceAnalysis.PerformFluxBalanceAnalysis();
    
    if (exitValue == 0)
    {
      if (refresh)
      {
        for (int i = 0; i < simTab.getComponentCount(); i++)
        {
          if (simTab.getComponentAt(i).getName().equals("Histogram"))
          {
            if (simTab.getComponentAt(i) instanceof Graph)
            {
              ((Graph) simTab.getComponentAt(i)).setYLabel("Flux");
              ((Graph) simTab.getComponentAt(i)).refresh();
            }
            else
            {
              if (new File(filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "sim-rep.txt").exists())
              {
                simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "Flux", gui, null, log, null, false, false));
                simTab.getComponentAt(i).setName("Histogram");
              }
            }
          }
        }
      }
    }
    else if (exitValue == 1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Flux balance analysis did not converge.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == 2)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Flux balance analysis failed.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "No flux balance constraints.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -2)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Initial point must be strictly feasible.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -3)
    {
      JOptionPane.showMessageDialog(Gui.frame, "The FBA problem is infeasible.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -4)
    {
      JOptionPane.showMessageDialog(Gui.frame, "The FBA problem has a singular KKT system.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -5)
    {
      JOptionPane.showMessageDialog(Gui.frame, "The matrix must have at least one row.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -6)
    {
      JOptionPane.showMessageDialog(Gui.frame, "The matrix is singular.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -7)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Equalities matrix A must have full rank.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -8)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Miscellaneous FBA failure (see console for details).", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -9)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Reaction in flux objective does not have a flux bound.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -10)
    {
      JOptionPane.showMessageDialog(Gui.frame, "All reactions must have flux bounds for FBA.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -11)
    {
      JOptionPane.showMessageDialog(Gui.frame, "No flux objectives.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -12)
    {
      JOptionPane.showMessageDialog(Gui.frame, "No active flux objective.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == -13)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Unknown flux balance analysis error.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    return exitValue;
  }

  private int executeXhtml(String filename, Runtime exec, String directory, String theFile, String out, File work) throws IOException, InterruptedException
  {
    int exitValue = 0;
    Simulator.expandArrays(filename, 1);
    reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=xhtml --out=" + out + ".xhtml " + theFile, Executables.envp, work);

    Preferences biosimrc = Preferences.userRoot();
    String xhtmlCmd = biosimrc.get("biosim.general.browser", "");
    writeLog("Executing:\n" + xhtmlCmd + " " + directory + out + ".xhtml");
    exec.exec(xhtmlCmd + " " + out + ".xhtml", null, work);
    
    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }
    
    return exitValue;
  }
  private int executeMarkov(String modelFile, String theFile, String sim, String root, String directory, String filename, String printer_id, String outDir, ModelEditor modelEditor, String lpnProperty, Component component,Gui gui, boolean refresh) throws XMLStreamException, IOException, InterruptedException
  {
    String prop = null;
    progress.setIndeterminate(true);
    LPN lhpnFile = null;
    if (modelFile.contains(".lpn"))
    {
      lhpnFile = new LPN();
      try {
        lhpnFile.load(root + GlobalConstants.separator + modelFile);
      } catch (BioSimException e) {
        JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
          JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }
    }
    else
    {
      new File(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
      ArrayList<String> specs = new ArrayList<String>();
      ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
      for (int i = 0; i < intSpecies.length; i++)
      {
        if (!intSpecies[i].equals(""))
        {
          String[] split = intSpecies[i].split(" ");
          if (split.length > 1)
          {
            String[] levels = split[1].split(",");
            if (levels.length > 0)
            {
              specs.add(split[0]);
              conLevel.add(levels);
            }
          }
        }
      }
      BioModel bioModel = new BioModel(root);
      bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
      if (bioModel.flattenModel(true) != null)
      {
        if (!lpnProperty.equals(""))
        {
          prop = lpnProperty;
        }
        ArrayList<String> propList = new ArrayList<String>();
        if (prop == null)
        {
          Model m = bioModel.getSBMLDocument().getModel();
          for (int num = 0; num < m.getConstraintCount(); num++)
          {
            String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
            if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
            {
              propList.add(constraint);
            }
          }
        }
        if (propList.size() > 0)
        {
          String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
          if ((s != null) && (s.length() > 0))
          {
            Model m = bioModel.getSBMLDocument().getModel();
            for (int num = 0; num < m.getConstraintCount(); num++)
            {
              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
              if (s.equals(constraint))
              {
                prop = Translator.convertProperty(m.getConstraint(num).getMath());
              }
            }
          }
        }
        MutableString mutProp = new MutableString(prop);
        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
        prop = mutProp.getString();
        if (lhpnFile == null)
        {
          new File(directory + GlobalConstants.separator + "running").delete();
          logFile.close();
          return 0;
        }
        lhpnFile.save(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
        writeLog("Saving SBML file as LPN:\n" + filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
      }
      else
      {
        new File(directory + GlobalConstants.separator + "running").delete();
        logFile.close();
        return 0;
      }
    }
    sg = new StateGraph(lhpnFile);
    sg.addObserver(this);
    BuildStateGraphThread buildStateGraph = new BuildStateGraphThread(sg, progress);
    buildStateGraph.start();
    buildStateGraph.join();
    writeLog("Number of states found: " + sg.getNumberOfStates());
    writeLog("Number of transitions found: " + sg.getNumberOfTransitions());
    writeLog("Memory used during state exploration: " + sg.getMemoryUsed() + "MB");
    writeLog("Total memory used: " + sg.getTotalMemoryUsed() + "MB");
    if (sim.equals("reachability-analysis") && !sg.getStop())
    {
      Object[] options = { "Yes", "No" };
      int value = JOptionPane.showOptionDialog(Gui.frame, "The state graph contains " + sg.getNumberOfStates() + " states and " + sg.getNumberOfTransitions() + " transitions.\n" + "Do you want to view it in Graphviz?", "View State Graph", JOptionPane.YES_NO_OPTION,
        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
      if (value == JOptionPane.YES_OPTION)
      {
        String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
        sg.outputStateGraph(graphFile, true);
        try
        {
          Runtime execGraph = Runtime.getRuntime();
          if (System.getProperty("os.name").contentEquals("Linux"))
          {

            writeLog("Executing:\ndotty " + graphFile);
            execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
          }
          else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
          {
            writeLog("Executing:\nopen " + graphFile);
            execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
          }
          else
          {
            writeLog("Executing:\ndotty " + graphFile);
            execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
          }
        }
        catch (Exception e1)
        {
          JOptionPane.showMessageDialog(Gui.frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
    else if (sim.equals("steady-state-markov-chain-analysis"))
    {
      if (!sg.getStop())
      {
        writeLog("Performing steady state Markov chain analysis.");
        PerformSteadyStateMarkovAnalysisThread performMarkovAnalysis = new PerformSteadyStateMarkovAnalysisThread(sg, progress);
        if (modelFile.contains(".lpn"))
        {
          performMarkovAnalysis.start(absError, null);
        }
        else
        {
          BioModel gcm = new BioModel(root);
          gcm.load(root + GlobalConstants.separator + modelEditor.getRefFile());
          ArrayList<Property> propList = new ArrayList<Property>();
          if (prop == null)
          {
            Model m = gcm.getSBMLDocument().getModel();
            for (int num = 0; num < m.getConstraintCount(); num++)
            {
              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
              if (constraint.startsWith("St("))
              {
                propList.add(sg.createProperty(constraint.trim().replace(" ", ""), Translator.convertProperty(m.getConstraint(num).getMath())));
              }
            }
          }
          // TODO: THIS NEEDS FIXING
          /*
           * for (int i = 0; i <
           * gcmEditor.getGCM().getConditions().size();
           * i++) { if
           * (gcmEditor.getGCM().getConditions().
           * get(i).startsWith("St")) {
           * conditions.add(Translator
           * .getProbpropExpression
           * (gcmEditor.getGCM().getConditions().get(i)));
           * } }
           */
          performMarkovAnalysis.start(absError, propList);
        }
        performMarkovAnalysis.join();
        if (!sg.getStop())
        {
          String simrep = sg.getMarkovResults();
          if (simrep != null)
          {
            FileOutputStream simrepstream = new FileOutputStream(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
            simrepstream.write((simrep).getBytes());
            simrepstream.close();
          }
          Object[] options = { "Yes", "No" };
          int value = JOptionPane.showOptionDialog(Gui.frame, "The state graph contains " + sg.getNumberOfStates() + " states and " + sg.getNumberOfTransitions() + " transitions.\n" + "Do you want to view it in Graphviz?", "View State Graph", JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
          if (value == JOptionPane.YES_OPTION)
          {
            String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
            sg.outputStateGraph(graphFile, true);
            try
            {
              Runtime execGraph = Runtime.getRuntime();
              if (System.getProperty("os.name").contentEquals("Linux"))
              {
                writeLog("Executing:\ndotty " + graphFile);
                execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
              }
              else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
              {
                writeLog("Executing:\nopen " + graphFile);
                execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
              }
              else
              {
                writeLog("Executing:\ndotty " + graphFile);
                execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
              }
            }
            catch (Exception e1)
            {
              JOptionPane.showMessageDialog(Gui.frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }
    }
    else if (sim.equals("transient-markov-chain-analysis"))
    {
      if (!sg.getStop())
      {
        writeLog("Performing transient Markov chain analysis with uniformization.");
        PerformTransientMarkovAnalysisThread performMarkovAnalysis = new PerformTransientMarkovAnalysisThread(sg, progress);
        if (prop != null)
        {
          String[] condition;
          try {
            condition = Translator.getProbpropParts(Translator.getProbpropExpression(prop));
            boolean globallyTrue = false;
            if (prop.contains("PF"))
            {
              condition[0] = "true";
            }
            else if (prop.contains("PG"))
            {
              condition[0] = "true";
              globallyTrue = true;
            }
            performMarkovAnalysis.start(timeLimit, timeStep, printInterval, absError, condition, globallyTrue);
          } catch (BioSimException e) {
            e.printStackTrace();
          }

        }
        else
        {
          performMarkovAnalysis.start(timeLimit, timeStep, printInterval, absError, null, false);
        }
        performMarkovAnalysis.join();
        if (!sg.getStop())
        {
          String simrep = sg.getMarkovResults();
          if (simrep != null)
          {
            FileOutputStream simrepstream = new FileOutputStream(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
            simrepstream.write((simrep).getBytes());
            simrepstream.close();
          }
          Object[] options = { "Yes", "No" };
          int value = JOptionPane.showOptionDialog(Gui.frame, "The state graph contains " + sg.getNumberOfStates() + " states and " + sg.getNumberOfTransitions() + " transitions.\n" + "Do you want to view it in Graphviz?", "View State Graph", JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
          if (value == JOptionPane.YES_OPTION)
          {
            String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
            sg.outputStateGraph(graphFile, true);
            try
            {
              Runtime execGraph = Runtime.getRuntime();
              if (System.getProperty("os.name").contentEquals("Linux"))
              {
                writeLog("Executing:\ndotty " + graphFile);
                execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
              }
              else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
              {
                writeLog("Executing:\nopen " + graphFile);
                execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
              }
              else
              {
                writeLog("Executing:\ndotty " + graphFile);
                execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
              }
            }
            catch (Exception e1)
            {
              JOptionPane.showMessageDialog(Gui.frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
            }
          }
          if (sg.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd"))
          {
            if (refresh)
            {
              for (int i = 0; i < simTab.getComponentCount(); i++)
              {
                if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
                {
                  if (simTab.getComponentAt(i) instanceof Graph)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                }
              }
            }
          }
        }
      }
      if (refresh)
      {
        for (int i = 0; i < simTab.getComponentCount(); i++)
        {
          if (simTab.getComponentAt(i).getName().equals("Histogram"))
          {
            if (simTab.getComponentAt(i) instanceof Graph)
            {
              ((Graph) simTab.getComponentAt(i)).refresh();
            }
            else
            {
              simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
              simTab.getComponentAt(i).setName("Histogram");
            }
          }
        }
      }
    }
    return 0;
  }

  private int executeDot(boolean nary, boolean abstraction, Runtime exec, ModelEditor modelEditor, AbstractionPanel abstPane, String directory, String root, String theFile, String modelFile, String simName, String filename, String out, File work) throws IOException, InterruptedException
  {
    int exitValue= 255;
    if (nary && modelEditor != null)
    {
      LPN lhpnFile = new LPN();
      lhpnFile.addObserver(this);
      try {
        lhpnFile.load(directory + GlobalConstants.separator + theFile.replace(".sbml", "").replace(".xml", "") + ".lpn");
      } catch (BioSimException e) {
        JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(),
          JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }
      lhpnFile.printDot(directory + GlobalConstants.separator + theFile.replace(".sbml", "").replace(".xml", "") + ".dot");
      exitValue = 0;
    }
    else if (modelFile.contains(".lpn"))
    {
      LPN lhpnFile = new LPN();
      try {
        lhpnFile.load(root + GlobalConstants.separator + modelFile);
      } catch (BioSimException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (abstraction)
      {
        Abstraction abst = new Abstraction(lhpnFile, abstPane.getAbstractionProperty());
        abst.abstractSTG(false);
        abst.printDot(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile.replace(".lpn", ".dot"));
      }
      else
      {
        lhpnFile.printDot(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile.replace(".lpn", ".dot"));
      }
      exitValue = 0;
    }
    else
    {
      writeLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=dot --out=" + out + ".dot " + filename);
      reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=dot --out=" + out + ".dot " + theFile, Executables.envp, work);
    }
    
    if (System.getProperty("os.name").contentEquals("Linux"))
    {
      writeLog("Executing:\ndotty " + directory + out + ".dot" );
      exec.exec("dotty " + out + ".dot", null, work);
    }
    else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
    {
      writeLog("Executing:\nopen " + directory + out + ".dot");
      exec.exec("open " + out + ".dot", null, work);
    }
    else
    {
      writeLog("Executing:\ndotty " + directory + out + ".dot");
      exec.exec("dotty " + out + ".dot", null, work);
    }
    
    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }
    
    return exitValue;
  }

  private int executePrism(String modelFile, String root, String directory, String out, String filename, String simName, String lpnProperty, Component component, ModelEditor modelEditor, Runtime exec, File work) throws IOException, InterruptedException, XMLStreamException, BioSimException
  {
    int exitValue = 255;
    String prop = null;
    progress.setIndeterminate(true);
    LPN lhpnFile = null;
    if (modelFile.contains(".lpn"))
    {
      lhpnFile = new LPN();
       lhpnFile.load(root + GlobalConstants.separator + modelFile);
    }
    else
    {
      new File(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
      ArrayList<String> specs = new ArrayList<String>();
      ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
      for (int i = 0; i < intSpecies.length; i++)
      {
        if (!intSpecies[i].equals(""))
        {
          String[] split = intSpecies[i].split(" ");
          if (split.length > 1)
          {
            String[] levels = split[1].split(",");
            if (levels.length > 0)
            {
              specs.add(split[0]);
              conLevel.add(levels);
            }
          }
        }
      }
      BioModel bioModel = new BioModel(root);
      bioModel.addObserver(this);
      bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
      if (bioModel.flattenModel(true) != null)
      {
        if (!lpnProperty.equals(""))
        {
          prop = lpnProperty;
        }
        ArrayList<String> propList = new ArrayList<String>();
        if (prop == null)
        {
          Model m = bioModel.getSBMLDocument().getModel();
          for (int num = 0; num < m.getConstraintCount(); num++)
          {
            String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
            if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
            {
              propList.add(constraint);
            }
          }
        }
        if (propList.size() > 0)
        {
          String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
          if ((s != null) && (s.length() > 0))
          {
            Model m = bioModel.getSBMLDocument().getModel();
            for (int num = 0; num < m.getConstraintCount(); num++)
            {
              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
              if (s.equals(constraint))
              {
                prop = Translator.convertProperty(m.getConstraint(num).getMath());
              }
            }
          }
        }
        MutableString mutProp = new MutableString(prop);
        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
        prop = mutProp.getString();
        if (lhpnFile == null)
        {
          new File(directory + GlobalConstants.separator + "running").delete();
          logFile.close();
          return 0;
        }
        writeLog("Saving SBML file as PRISM file:\n" + filename.replace(".xml", ".prism"));
        writeLog("Saving PRISM Property file:\n" + filename.replace(".xml", ".pctl"));
        LPN.convertLPN2PRISM(logFile, lhpnFile, filename.replace(".xml", ".prism"), 
          bioModel.getSBMLDocument());
        Preferences biosimrc = Preferences.userRoot();
        String prismCmd = biosimrc.get("biosim.general.prism", "");
        writeLog("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl"); 
        reb2sac = exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
        String error = "", result = "", fullLog = "";
        try
        {
          InputStream reb = reb2sac.getInputStream();
          InputStreamReader isr = new InputStreamReader(reb);
          BufferedReader br = new BufferedReader(isr);
          String line;
          while ((line = br.readLine()) != null)
          {
            fullLog += line + '\n';
            if (line.startsWith("Result:"))
            {
              result = line + '\n';
            }
          }
          InputStream reb2 = reb2sac.getErrorStream();
          int read = reb2.read();
          while (read != -1)
          {
            error += (char) read;
            read = reb2.read();
          }
          br.close();
          isr.close();
          reb.close();
          reb2.close();
        }
        catch (Exception e)
        {
          // e.printStackTrace();
        }
        if (reb2sac != null)
        {
          exitValue = reb2sac.waitFor();
        }
        running.setCursor(null);
        running.dispose();
        if (!error.equals(""))
        {
          writeLog("Errors:\n" + error + "\n");
        }
        else if (!result.equals(""))
        {
          writeLog(result);
        }
        else
        {
          JTextArea messageArea = new JTextArea(fullLog);
          messageArea.setEditable(false);
          JScrollPane scroll = new JScrollPane();
          scroll.setMinimumSize(new Dimension(500, 500));
          scroll.setPreferredSize(new Dimension(500, 500));
          scroll.setViewportView(messageArea);
          JOptionPane.showMessageDialog(Gui.frame, scroll, "Verification Failed", JOptionPane.ERROR_MESSAGE);
        }

        exitValue = 0;
      }
      else
      {
        new File(directory + GlobalConstants.separator + "running").delete();
        logFile.close();
        exitValue = 0;
      }
    }
    
    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }
    
    Preferences biosimrc = Preferences.userRoot();
    String prismCmd = biosimrc.get("biosim.general.prism", "");
    writeLog("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl");
    exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
    
    return exitValue;
  }


  private int executeSBML(Gui gui, AbstractionPanel abstPane, ModelEditor modelEditor, String simName, String root, String directory, String filename, String theFile, String modelFile, boolean abstraction, boolean nary, String lpnProperty, File work, Runtime exec, Component component) throws IOException, HeadlessException, XMLStreamException, InterruptedException
  {
    String sbmlName = JOptionPane.showInputDialog(component, "Enter Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
    int exitValue = 255;

    if (sbmlName != null && !sbmlName.trim().equals(""))
    {
      sbmlName = sbmlName.trim();
      if (!sbmlName.endsWith(".xml"))
      {
        sbmlName += ".xml";
      }
      File f = new File(root + GlobalConstants.separator + sbmlName);
      if (f.exists())
      {
        Object[] options = { "Overwrite", "Cancel" };
        int value = JOptionPane.showOptionDialog(component, "File already exists." + "\nDo you want to overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (value == JOptionPane.YES_OPTION)
        {
          File dir = new File(root + GlobalConstants.separator + sbmlName);
          if (dir.isDirectory())
          {
            gui.deleteDir(dir);
          }
          else
          {
            System.gc();
            dir.delete();
          }
        }
        else
        {
          new File(directory + GlobalConstants.separator + "running").delete();
          logFile.close();
          return 0;
        }
      }
      if (modelFile.contains(".lpn"))
      {
        progress.setIndeterminate(true);
        try
        {
          Translator t1 = new Translator();
          if (abstraction)
          {
            LPN lhpnFile = new LPN();
            lhpnFile.load(root + GlobalConstants.separator + modelFile);
            Abstraction abst = new Abstraction(lhpnFile, abstPane.getAbstractionProperty());
            abst.abstractSTG(false);
            abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile);
            t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile, lpnProperty);
          }
          else
          {
            t1.convertLPN2SBML(root + GlobalConstants.separator + modelFile, lpnProperty);
          }
          t1.setFilename(root + GlobalConstants.separator + sbmlName);
          t1.outputSBML();
          exitValue = 0;
          logFile.close();
        }
        catch(BioSimException e)
        {
          e.printStackTrace();
        }
        return exitValue;
      }
      else if (modelEditor != null && nary)
      {
        String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
        ArrayList<String> specs = new ArrayList<String>();
        ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
        for (int i = 0; i < intSpecies.length; i++)
        {
          if (!intSpecies[i].equals(""))
          {
            String[] split = intSpecies[i].split(" ");
            if (split.length > 1)
            {
              String[] levels = split[1].split(",");
              if (levels.length > 0)
              {
                specs.add(split[0]);
                conLevel.add(levels);
              }
            }
          }
        }
        progress.setIndeterminate(true);
        BioModel bioModel = new BioModel(root);
        bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
        if (bioModel.flattenModel(true) != null)
        {
          String prop = null;
          if (!lpnProperty.equals(""))
          {
            prop = lpnProperty;
          }
          ArrayList<String> propList = new ArrayList<String>();
          if (prop == null)
          {
            Model m = bioModel.getSBMLDocument().getModel();
            for (int num = 0; num < m.getConstraintCount(); num++)
            {
              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
              if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
              {
                propList.add(constraint);
              }
            }
          }
          if (propList.size() > 0)
          {
            String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
            if ((s != null) && (s.length() > 0))
            {
              Model m = bioModel.getSBMLDocument().getModel();
              for (int num = 0; num < m.getConstraintCount(); num++)
              {
                String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
                if (s.equals(constraint))
                {
                  prop = Translator.convertProperty(m.getConstraint(num).getMath());
                }
              }
            }
          }
          MutableString mutProp = new MutableString(prop);
          LPN lpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
          prop = mutProp.getString();
          if (lpnFile == null)
          {
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return 0;
          }
          lpnFile.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
          try
          {
            Translator t1 = new Translator();
            if (abstraction)
            {
              LPN lhpnFile = new LPN();
              lhpnFile.load(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
              Abstraction abst = new Abstraction(lhpnFile, abstPane.getAbstractionProperty());
              abst.abstractSTG(false);
              abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp");
              t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp", prop);
            }
            else
            {
              t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName, prop);
            }
            t1.setFilename(root + GlobalConstants.separator + sbmlName);
            t1.outputSBML();
          }
          catch(BioSimException e)
          {
            e.printStackTrace();
          }
        }
        else
        {
          new File(directory + GlobalConstants.separator + "running").delete();
          logFile.close();
          return 0;
        }
        exitValue = 0;
      }
      else
      {
        if (analysisView.reb2sacAbstraction() && (abstraction || nary))
        {
          writeLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=sbml --out=" + ".." + GlobalConstants.separator + sbmlName + " " + filename);
          reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=sbml --out=" + ".." + GlobalConstants.separator + sbmlName + " " + theFile, Executables.envp, work);
        }
        else
        {
          writeLog("Outputting SBML file:\n" + root + GlobalConstants.separator + sbmlName);
          FileOutputStream fileOutput = new FileOutputStream(new File(root + GlobalConstants.separator + sbmlName));
          FileInputStream fileInput = new FileInputStream(new File(filename));
          int read = fileInput.read();
          while (read != -1)
          {
            fileOutput.write(read);
            read = fileInput.read();
          }
          fileInput.close();
          fileOutput.close();
          exitValue = 0;
        }
      }
    }

    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }
    
    return exitValue;
  }

  private int executeSimulation(String sim, String direct, String directory, String root, String filename, String outDir, String theFile, boolean abstraction, boolean expandReaction, Runtime exec, Properties properties, File work) throws IOException, InterruptedException
  {
    int exitValue = 0;
    Preferences biosimrc = Preferences.userRoot();
    String reactionAbstraction = abstraction ? "reactionAbstraction" : expandReaction ? "expandReaction" : "None";
    double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));
    String SBMLFileName = directory + GlobalConstants.separator + theFile;
    String command = null;
    String[] env = null;
    if (direct != null && !direct.equals("."))
    {
      outDir = outDir + GlobalConstants.separator + direct;
    }
    boolean runJava = true;
    if (sim.equals("SSA-CR (Dynamic)"))
    {
      dynSim = new DynamicSimulation(SimulationType.CR);
    }
    else if (sim.equals("SSA-Direct (Dynamic)"))
    {
      dynSim = new DynamicSimulation(SimulationType.DIRECT);
    }
    else if (sim.equals("SSA-Direct (Flatten)"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
    }
    else if (sim.equals("SSA-Direct (Hierarchical)"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
    }
    else if (sim.equals("Mixed-Hierarchical"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_MIXED);
    }
    else if (sim.equals("Hybrid-Hierarchical"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_HYBRID);
    }
    else if (sim.equals("Runge-Kutta-Fehlberg (Dynamic)"))
    {
      dynSim = new DynamicSimulation(SimulationType.RK);
    }
    else if (sim.equals("Runge-Kutta-Fehlberg (Hierarchical)"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
    }
    else if (sim.equals("Runge-Kutta-Fehlberg (Flatten)"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
    }
    else if (biosimrc.get("biosim.sim.command", "").equals(""))
    {
      command = Executables.reb2sacExecutable + " --target.encoding=" + sim + " " + theFile;
      Simulator.expandArrays(filename, stoichAmpValue);
      env = Executables.envp;
      runJava = false;
    }
    else
    {
      command = biosimrc.get("biosim.sim.command", "");
      String fileStem = theFile.replaceAll(".xml", "");
      fileStem = fileStem.replaceAll(".sbml", "");
      command = command.replaceAll("filename", fileStem);
      command = command.replaceAll("sim", sim);
      runJava = false;
    }

    if(runJava)
    {
      if (direct != null && !direct.equals("."))
      {
        outDir = outDir + GlobalConstants.separator + direct;
      }
      dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, 0.0, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, absError, printer_track_quantity, genStats, simTab, reactionAbstraction,  initialTime, outputStartTime);

      new File(directory + GlobalConstants.separator + "running").delete();
    }
    else
    {
      Simulator.expandArrays(filename, stoichAmpValue);
      writeLog("Executing:\n" + command + "\n");

      reb2sac = exec.exec(command, env, work);
    }
    
    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }
    
    return exitValue;
  }

  private void displayError(int exitValue)
  {
    if (exitValue == 143)
    {
      JOptionPane.showMessageDialog(Gui.frame, "The simulation was" + " canceled by the user.", "Canceled Simulation", JOptionPane.ERROR_MESSAGE);
    }
    else if (exitValue == 139)
    {
      JOptionPane.showMessageDialog(Gui.frame, "The selected model is not a valid sbml file." + "\nYou must select an sbml file.", "Not An SBML File", JOptionPane.ERROR_MESSAGE);
    }
    else if(exitValue != 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Error In Execution!\n" + "Bad Return Value!\n" + "The reb2sac program returned " + exitValue + " as an exit value.", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  private void updateReb2SacProgress(double runTime) throws IOException
  {
    String error = "";
    try
    {
      InputStream reb = reb2sac.getInputStream();
      InputStreamReader isr = new InputStreamReader(reb);
      BufferedReader br = new BufferedReader(isr);
      String line;
      double time = 0;
      double oldTime = 0;
      int runNum = 0;
      int prog = 0;
      while ((line = br.readLine()) != null)
      {
        try
        {
          if (line.contains("Time"))
          {
            time = Double.parseDouble(line.substring(line.indexOf('=') + 1, line.length()));
            if (oldTime > time)
            {
              runNum++;
            }
            oldTime = time;
            time += (runNum * timeLimit);
            double d = ((time * 100) / runTime);
            String s = d + "";
            double decimal = Double.parseDouble(s.substring(s.indexOf('.'), s.length()));
            if (decimal >= 0.5)
            {
              prog = (int) (Math.ceil(d));
            }
            else
            {
              prog = (int) (d);
            }
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        progress.setValue(prog);
      }
      InputStream reb2 = reb2sac.getErrorStream();
      int read = reb2.read();
      while (read != -1)
      {
        error += (char) read;
        read = reb2.read();
      }
      br.close();
      isr.close();
      reb.close();
      reb2.close();
    }
    catch (Exception e)
    {
      // e.printStackTrace();
    }
    
    if (!error.equals(""))
    {
      writeLog("Errors:\n" + error);
    }
    
  }
  private void writeLog(String message) throws IOException
  {
    log.addText(message+"\n");
    logFile.write(message+ "\n\n");
  }

  /**
   * This method is called if a button that cancels the simulation is pressed.
   */
  @Override
  public void actionPerformed(ActionEvent e)
  {

    if (dynSim != null)
    {
      dynSim.cancel();
    }
    if (reb2sac != null)
    {
      reb2sac.destroy();
    }
    if (sg != null)
    {
      sg.stop();
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    Message message = (Message) arg;

    if(message.isCancel())
    {
      JOptionPane.showMessageDialog(Gui.frame, "Simulation Canceled", "Canceled", JOptionPane.ERROR_MESSAGE);
    }
    else if(message.isConsole())
    {
      System.out.println(message.getMessage());
    }
    else if(message.isErrorDialog())
    {
      JOptionPane.showMessageDialog(Gui.frame, message.getMessage(), message.getTitle(), JOptionPane.ERROR_MESSAGE);
    }
    else if(message.isDialog())
    {
      JOptionPane.showMessageDialog(Gui.frame, message.getMessage(), message.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }
    else if(message.isLog())
    {
      log.addText(message.getMessage());
    }
  }
}
