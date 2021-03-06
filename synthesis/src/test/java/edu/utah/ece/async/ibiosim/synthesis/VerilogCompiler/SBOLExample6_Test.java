package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SystemsBiologyOntology;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogConstructs.VerilogModule;

/**
 * Test for a 3 input assignment. 
 * 
 * @author Tramy Nguyen 
 */
public class SBOLExample6_Test {

	private static SBOLDocument sbolDoc;
	private static ModuleDefinition sbolDesign;
		
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		VerilogParser verilogParser = new VerilogParser();
		VerilogModule verilogModule = verilogParser.parseVerilogFile(new File(TestingFiles.verilogCont7Decomp_File));
		VerilogToSBOL sbolConverter = new VerilogToSBOL(true);
		WrappedSBOL sbolWrapper = sbolConverter.convertVerilog2SBOL(verilogModule);
		Assert.assertNotNull(sbolWrapper);
			
		sbolDoc = sbolWrapper.getSBOLDocument();
		Assert.assertEquals(1, sbolDoc.getModuleDefinitions().size());
		sbolDesign = sbolDoc.getModuleDefinition("MD0" + verilogModule.getModuleId(), "1.0");
	}

	@Test
	public void Test_cdSize() {
		Assert.assertEquals(18, sbolDoc.getComponentDefinitions().size());
	}
	
	
	@Test
	public void Test_fcSize() {
		Assert.assertEquals(9, sbolDesign.getFunctionalComponents().size());
	}
	
	@Test
	public void Test_proteinSize() {
		int actualSize = 0; 
		for(ComponentDefinition cd : sbolDoc.getComponentDefinitions()) {
			if(cd.getTypes().iterator().next().equals(ComponentDefinition.PROTEIN)) {
				actualSize++;
			}
		}
		Assert.assertEquals(6, actualSize);
	}
	
	@Test
	public void Test_dnaSize() {
		int actualSize = 0; 
		for(ComponentDefinition cd : sbolDoc.getComponentDefinitions()) {
			if(cd.getTypes().iterator().next().equals(ComponentDefinition.DNA_REGION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(12, actualSize);
	}
	
	@Test
	public void Test_interactionSize() {
		Assert.assertEquals(8, sbolDesign.getInteractions().size());
	}
	
	@Test
	public void Test_inhibitionSize() {
		int actualSize = 0; 
		for(Interaction inter : sbolDesign.getInteractions()) {
			if(inter.getTypes().iterator().next().equals(SystemsBiologyOntology.INHIBITION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(5, actualSize);
	}
	
	@Test
	public void Test_productionSize() {
		int actualSize = 0; 
		for(Interaction inter : sbolDesign.getInteractions()) {
			if(inter.getTypes().iterator().next().equals(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(3, actualSize);
	}
	
	@Test
	public void Test_NOT1() {
		FunctionalComponent gate = sbolDesign.getFunctionalComponent("FC7_notTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent input = sbolDesign.getFunctionalComponent("FC4__0_");
		Assert.assertNotNull(input);
		
		FunctionalComponent output = sbolDesign.getFunctionalComponent("FC5__1_");
		Assert.assertNotNull(output);
		
		Interaction inhibition = sbolDesign.getInteraction("I3_Inhib");
		Assert.assertNotNull(inhibition);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition.getTypes().iterator().next());
		Assert.assertEquals(2, inhibition.getParticipations().size());

		for(Participation p : inhibition.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals(input, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals(gate, p.getParticipant());
			}
		}
	
		Interaction production = sbolDesign.getInteraction("I4_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());

		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.PROMOTER)) {
				Assert.assertEquals(gate, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals(output, p.getParticipant());
			}
		}
	}

	
	@Test
	public void Test_NOR1() {
		FunctionalComponent gate = sbolDesign.getFunctionalComponent("FC6_norTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent in1 = sbolDesign.getFunctionalComponent("FC1_b");
		Assert.assertNotNull(in1);
		
		FunctionalComponent in2 = sbolDesign.getFunctionalComponent("FC0_a");
		Assert.assertNotNull(in2);
		
		
		FunctionalComponent out = sbolDesign.getFunctionalComponent("FC4__0_");
		Assert.assertNotNull(out);
		
		Interaction inhibition1 = sbolDesign.getInteraction("I0_Inhib");
		Assert.assertNotNull(inhibition1);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition1.getTypes().iterator().next());
		
		for(Participation p : inhibition1.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals(in2, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals(gate, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
		
		Interaction inhibition2 = sbolDesign.getInteraction("I1_Inhib");
		Assert.assertNotNull(inhibition2);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition2.getTypes().iterator().next());

		for(Participation p : inhibition2.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals(in1, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals(gate, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
		Interaction production = sbolDesign.getInteraction("I2_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());

		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals(gate, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals(out, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
		
	}

	
	

	
	@Test
	public void Test_NOR2() {
		FunctionalComponent gate = sbolDesign.getFunctionalComponent("FC8_norTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent in1 = sbolDesign.getFunctionalComponent("FC5__1_");
		Assert.assertNotNull(in1);
		
		FunctionalComponent in2 = sbolDesign.getFunctionalComponent("FC2_c");
		Assert.assertNotNull(in2);
		
		
		FunctionalComponent out = sbolDesign.getFunctionalComponent("FC3_d");
		Assert.assertNotNull(out);
		
		Interaction inhibition1 = sbolDesign.getInteraction("I6_Inhib");
		Assert.assertNotNull(inhibition1);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition1.getTypes().iterator().next());
		
		for(Participation p : inhibition1.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals(in1, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals(gate, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
		
		Interaction inhibition2 = sbolDesign.getInteraction("I5_Inhib");
		Assert.assertNotNull(inhibition2);
		Assert.assertEquals(SystemsBiologyOntology.INHIBITION, inhibition2.getTypes().iterator().next());

		for(Participation p : inhibition2.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.INHIBITOR)) {
				Assert.assertEquals(in2, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.INHIBITED)){
				Assert.assertEquals(gate, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
		Interaction production = sbolDesign.getInteraction("I7_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());

		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals(gate, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals(out, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
		
	}


}
