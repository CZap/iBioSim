package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate.GateType;

/**
 * Class to run Gate Generation through command line.
 * @author Tramy Nguyen
 */
public class GateGenerationRunner {
	
	private static final char separator = ' ';
	
	public static void main(String[] args) {
		try {
			CommandLine cmd = parseCommandLine(args);
			GateGeneratorOptions setupOpt = createGateGenerationOptions(cmd);
			
			GateGeneration generator = new GateGeneration();
			List<SBOLDocument> enrichedTU_List = generator.generateGatesFromTranscriptionalUnits(setupOpt.getTUSBOLDocumentList(), setupOpt.getSelectedSBHRepo());
			generator.identifyGeneratedGates(enrichedTU_List);
			List<GeneticGate> notList = generator.getGates(GateType.NOT);
			generator.generateWiredORGates(notList);
			
			String outDir = setupOpt.getOutputDirectory() + File.separator ;
			System.out.println("Total Gate size: " + generator.getLibrary().size());
			if(setupOpt.outputAllLibrary()) {
				List<GeneticGate> gates = generator.getLibrary();
				generator.exportLibrary(gates, outDir + setupOpt.getOutputFileName() + ".xml");
			}
			if(setupOpt.outputNOTLibrary()) {
				List<GeneticGate> gates = generator.getGates(GateType.NOT);
				generator.exportLibrary(gates, outDir + "NOTLibrary_size" + gates.size() + ".xml");
				System.out.println("NOT Gate size: " + gates.size());
			}
			if(setupOpt.outputNORLibrary()) {
				List<GeneticGate> gates = generator.getGates(GateType.NOR);
				generator.exportLibrary(gates, outDir + "NORLibrary_size" + gates.size() + ".xml");
				System.out.println("NOR Gate size: " + gates.size());
			}
			if(setupOpt.outputORLibrary()) {
				List<GeneticGate> gates = generator.getGates(GateType.OR);
				generator.exportLibrary(gates, outDir + "ORLibrary_size" + gates.size() + ".xml");
				System.out.println("OR Gate size: " + gates.size());
			}
			if(setupOpt.outputWiredORLibrary()) {
				List<GeneticGate> gates = generator.getGates(GateType.WIREDOR);
				generator.exportLibrary(gates, outDir + "WiredORLibrary_size" + gates.size() + ".xml");
				System.out.println("WiredOR Gate size: " + gates.size());
			}
			if(setupOpt.outputNANDLibrary()) {
				List<GeneticGate> gates = generator.getGates(GateType.NAND);
				generator.exportLibrary(gates, outDir + "NANDLibrary_size" + gates.size() + ".xml");
				System.out.println("NAND Gate size: " + gates.size());
			}
			if(setupOpt.outputANDLibrary()) {
				List<GeneticGate> gates = generator.getGates(GateType.AND);
				generator.exportLibrary(gates, outDir + "ANDLibrary_size" + gates.size() + ".xml");
				System.out.println("AND Gate size: " + gates.size());
			}
			if(setupOpt.outputNOTSUPPORTEDLibrary()) {
				List<GeneticGate> gates = generator.getGates(GateType.NOTSUPPORTED);
				generator.exportLibrary(gates, outDir + "NOTSUPPORTEDLibrary_size" + gates.size() + ".xml");
				System.out.println("NOTSUPPORTED Gate size: " + gates.size());
			}
			
		} 
		catch (SBOLValidationException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (SBOLConversionException e) {
			e.printStackTrace();
		} 
		catch (VPRException e) {
			e.printStackTrace();
		} 
		catch (VPRTripleStoreException e) {
			e.printStackTrace();
		} 
		catch (GateGenerationExeception e) {
			e.printStackTrace();
		} 
		catch (ParseException e) {
			e.printStackTrace();
		} 
		catch (SBOLException e) {
			e.printStackTrace();
		}
	}

	private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		String sbolUsage = "-f <arg(s)> -od usr/dir -";
		formatter.printHelp(125, sbolUsage, 
				"Gate Generation Options", 
				getCommandLineOptions(), 
				"Note: <arg> is the user's input value.");
	}

	private static Options getCommandLineOptions() {
		Option tuFiles = new Option("f", "SBOLFiles",  true, "List of SBOL files containing transcription unit designs to generate logic gates from.");
		tuFiles.setValueSeparator(separator);
		
		Options options = new Options();
		options.addOption(tuFiles);
		options.addOption("sbh", "sbhRepository", true, "Name of SynBioHub repository to obtain get interaction data from.");
		options.addOption("h", "help", false, "show the available command line needed to run this application.");
		options.addOption("o", "outFileName", true, "Name of output file");
		options.addOption("od", "odir", true, "Path of output directory where GateGeneration stores the result.");
		options.addOption("NOT", false, "Export NOT gates");
		options.addOption("NOR", false, "Export NOR gates");
		options.addOption("OR", false, "Export OR gates");
		options.addOption("WiredOR", false, "Export Wired OR gates");
		options.addOption("NAND", false, "Export NAND gates");
		options.addOption("AND", false, "Export AND gates");
		options.addOption("NOTSUPPORTED", false, "Export gates not identified.");
		options.addOption("all", false, "Export all gates into an SBOL file."); 
		return options;
	}

	protected static CommandLine parseCommandLine(String[] args) throws org.apache.commons.cli.ParseException {
		Options cmd_options = getCommandLineOptions();
		CommandLineParser cmd_parser = new DefaultParser();
		CommandLine cmd = cmd_parser.parse(cmd_options, args);
		return cmd;
	}

	protected static GateGeneratorOptions createGateGenerationOptions(CommandLine cmd) throws FileNotFoundException {
		GateGeneratorOptions gateGenOptions = new GateGeneratorOptions();
		if(cmd.hasOption("h")) {
			printUsage();
		}
		if(cmd.hasOption("o")) {
			String fileName = cmd.getOptionValue("o");
			gateGenOptions.setOutputFileName(fileName);
		}
		if(cmd.hasOption("od")) {
			String outputDirectory = cmd.getOptionValue("od");
			gateGenOptions.setOutputDirectory(outputDirectory);
		}
		if(cmd.hasOption("f")) {
			String[] files = cmd.getOptionValue("f").split(String.valueOf(separator));
			for(String file : files) {
				gateGenOptions.addTUFile(file);
			}
		}
		if(cmd.hasOption("sbh")) {
			String sbhRepo = cmd.getOptionValue("sbh");
			gateGenOptions.setSelectedSBHRepo(sbhRepo);
		}
		if(cmd.hasOption("all")) {
			gateGenOptions.setOutputAllLibrary(true);
		}
		if(cmd.hasOption("NOT")) {
			gateGenOptions.setOutputNOTLibrary(true);
		}
		if(cmd.hasOption("NOR")) {
			gateGenOptions.setOutputNORLibrary(true);
		}
		if(cmd.hasOption("OR")) {
			gateGenOptions.setOutputORLibrary(true);
		}
		if(cmd.hasOption("WiredOR")) {
			gateGenOptions.setOutputWiredORLibrary(true);
		}
		if(cmd.hasOption("NAND")) {
			gateGenOptions.setOutputNANDLibrary(true);
		}
		if(cmd.hasOption("AND")) {
			gateGenOptions.setOutputANDLibrary(true);
		}
		if(cmd.hasOption("NOTSUPPORTED")) {
			gateGenOptions.setOutputNOTSUPPORTEDLibrary(true);
		}
		
		return gateGenOptions;
	}
	
}
