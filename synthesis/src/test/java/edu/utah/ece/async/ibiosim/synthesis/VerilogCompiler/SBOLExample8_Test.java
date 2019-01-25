package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SystemsBiologyOntology;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * Test hierarchical model for an SR-latch example exported into SBOL.
 * @author Tramy Nguyen 
 *
 */
public class SBOLExample8_Test {

	private static SBOLDocument sbolDoc;
	private static ModuleDefinition fullCircuit, subcircuit_q, subcircuit_qnot;
		
	@BeforeClass
	public static void setupTest() throws ParseException, SBOLValidationException, VerilogCompilerException, XMLStreamException, IOException, BioSimException, org.apache.commons.cli.ParseException, SBOLConversionException {
		CompilerOptions setupOpt = new CompilerOptions();
		setupOpt.addVerilogFile(CompilerTestSuite.verilogCont5_file);
		VerilogCompiler compiledVerilog = VerilogRunner.compile(setupOpt.getVerilogFiles());
		compiledVerilog.compileVerilogOutputData(false);  
		
		String vName = "contAssign5";
		WrappedSBOL sbolWrapper = compiledVerilog.getSBOLWrapper(vName);
		Assert.assertNotNull(sbolWrapper);
	
		sbolDoc = sbolWrapper.getSBOLDocument();
		fullCircuit = sbolDoc.getModuleDefinition("circuit_contAssign5", "1.0");
		Assert.assertNotNull(fullCircuit);
		
		subcircuit_q = sbolDoc.getModuleDefinition("circuit_q", "1.0");
		Assert.assertNotNull(subcircuit_q);
		
		subcircuit_qnot = sbolDoc.getModuleDefinition("circuit_qnot", "1.0");
		Assert.assertNotNull(subcircuit_qnot);
		
	}

	@Test 
	public void Test_mdSize() {
		Assert.assertEquals(3, sbolDoc.getModuleDefinitions().size());
	}

	@Test
	public void Test_cdSize() {
		Assert.assertEquals(40, sbolDoc.getComponentDefinitions().size());
	}
	
	@Test
	public void Test_fullCircuitProteinSize() {
		Assert.assertEquals(4, fullCircuit.getFunctionalComponents().size());
	}
	
	@Test
	public void Test_qCircuitProteinSize() {
		Assert.assertEquals(8, subcircuit_q.getFunctionalComponents().size());
	}

	@Test
	public void Test_qnotCircuitProteinSize() {
		Assert.assertEquals(8, subcircuit_qnot.getFunctionalComponents().size());
	}

	@Test
	public void Test_qNOT1() {
		FunctionalComponent gate = subcircuit_q.getFunctionalComponent("FC5_notTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent input = subcircuit_q.getFunctionalComponent("FC6_wiredProtein");
		Assert.assertNotNull(input);
		
		FunctionalComponent output = subcircuit_q.getFunctionalComponent("FC4_q");
		Assert.assertNotNull(output);
		
		Interaction inhibition = subcircuit_q.getInteraction("I0_Inhib");
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
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	
		Interaction production = subcircuit_q.getInteraction("I1_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());

		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals(gate, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals(output, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	
	@Test
	public void Test_qNOT2() {
		FunctionalComponent gate = subcircuit_q.getFunctionalComponent("FC7_notTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent input = subcircuit_q.getFunctionalComponent("FC8_wiredProtein");
		Assert.assertNotNull(input);
		
		FunctionalComponent output = subcircuit_q.getFunctionalComponent("FC6_wiredProtein");
		Assert.assertNotNull(output);
		
		Interaction inhibition = subcircuit_q.getInteraction("I2_Inhib");
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
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	
		Interaction production = subcircuit_q.getInteraction("I3_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());

		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals(gate, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals(output, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	@Test
	public void Test_qNOR1() {
		FunctionalComponent gate = subcircuit_q.getFunctionalComponent("FC9_norTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent in1 = subcircuit_q.getFunctionalComponent("FC10_r");
		Assert.assertNotNull(in1);
		
		FunctionalComponent in2 = subcircuit_q.getFunctionalComponent("FC11_qnot");
		Assert.assertNotNull(in2);
		
		
		FunctionalComponent out = subcircuit_q.getFunctionalComponent("FC8_wiredProtein");
		Assert.assertNotNull(out);
		
		Interaction inhibition1 = subcircuit_q.getInteraction("I4_Inhib");
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
		
		Interaction inhibition2 = subcircuit_q.getInteraction("I5_Inhib");
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
		Interaction production = subcircuit_q.getInteraction("I6_Prod");
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
	public void Test_qnotNOT1() {
		FunctionalComponent gate = subcircuit_qnot.getFunctionalComponent("FC13_notTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent input = subcircuit_qnot.getFunctionalComponent("FC14_wiredProtein");
		Assert.assertNotNull(input);
		
		FunctionalComponent output = subcircuit_qnot.getFunctionalComponent("FC12_qnot");
		Assert.assertNotNull(output);
		
		Interaction inhibition = subcircuit_qnot.getInteraction("I7_Inhib");
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
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	
		Interaction production = subcircuit_qnot.getInteraction("I8_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());

		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals(gate, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals(output, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}

	@Test
	public void Test_qnotNOT2() {
		FunctionalComponent gate = subcircuit_qnot.getFunctionalComponent("FC15_notTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent input = subcircuit_qnot.getFunctionalComponent("FC16_wiredProtein");
		Assert.assertNotNull(input);
		
		FunctionalComponent output = subcircuit_qnot.getFunctionalComponent("FC14_wiredProtein");
		Assert.assertNotNull(output);
		
		Interaction inhibition = subcircuit_qnot.getInteraction("I9_Inhib");
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
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	
		Interaction production = subcircuit_qnot.getInteraction("I10_Prod");
		Assert.assertNotNull(production);
		Assert.assertEquals(SystemsBiologyOntology.GENETIC_PRODUCTION, production.getTypes().iterator().next());
		Assert.assertEquals(2, production.getParticipations().size());

		for(Participation p : production.getParticipations()) {
			URI role = p.getRoles().iterator().next();
			if(role.equals(SystemsBiologyOntology.TEMPLATE)) {
				Assert.assertEquals(gate, p.getParticipant());
			}
			else if(role.equals(SystemsBiologyOntology.PRODUCT)){
				Assert.assertEquals(output, p.getParticipant());
			}
			else {
				Assert.fail("Unexpected role found: " + role);
			}
		}
	}
	
	@Test
	public void Test_qnotNOR1() {
		FunctionalComponent gate = subcircuit_qnot.getFunctionalComponent("FC17_norTU");
		Assert.assertNotNull(gate);
		
		FunctionalComponent in1 = subcircuit_qnot.getFunctionalComponent("FC18_s");
		Assert.assertNotNull(in1);
		
		FunctionalComponent in2 = subcircuit_qnot.getFunctionalComponent("FC19_q");
		Assert.assertNotNull(in2);
		
		
		FunctionalComponent out = subcircuit_qnot.getFunctionalComponent("FC16_wiredProtein");
		Assert.assertNotNull(out);
		
		Interaction inhibition1 = subcircuit_qnot.getInteraction("I11_Inhib");
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
		
		Interaction inhibition2 = subcircuit_qnot.getInteraction("I12_Inhib");
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
		}
		Interaction production = subcircuit_qnot.getInteraction("I13_Prod");
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
	public void Test_fullCircuitInteractionSize() {
		Assert.assertEquals(0, fullCircuit.getInteractions().size());
	}

	@Test
	public void Test_qSubcircuitInteractionSize() {
		Assert.assertEquals(7, subcircuit_q.getInteractions().size());
	}

	@Test
	public void Test_qnotSubcircuitInteractionSize() {
		Assert.assertEquals(7, subcircuit_qnot.getInteractions().size());
	}
	
	@Test
	public void Test_subcircuitSize() {
		Assert.assertEquals(2, fullCircuit.getModules().size());
	}

	@Test
	public void Test_qMapsTo() {
		Module qCircuit_instance = fullCircuit.getModule("M0");
		Assert.assertNotNull(qCircuit_instance);
		
		for(MapsTo wire : qCircuit_instance.getMapsTos()) {
			Assert.assertEquals(RefinementType.USELOCAL, wire.getRefinement());
			
			String local = wire.getLocal().getDisplayId();
			String remote = wire.getRemote().getDisplayId();
			if(local.equals("FC2_q")) {
				Assert.assertEquals("FC4_q", remote);
			}
			else if(local.equals("FC3_qnot")) {
				Assert.assertEquals("FC11_qnot", wire.getRemote().getDisplayId());
			}
			else if(local.equals("FC1_r")) {
				Assert.assertEquals("FC10_r", wire.getRemote().getDisplayId());
			}
		}
	}
	
	@Test
	public void Test_qMapsToSize() {
		Module qModule = fullCircuit.getModule("M0");
		Assert.assertEquals(3, qModule.getMapsTos().size());
	}

	@Test
	public void Test_qnotMapsToSize() {
		Module qModule = fullCircuit.getModule("M1");
		Assert.assertEquals(3, qModule.getMapsTos().size());
	}
	
	@Test
	public void Test_qnotMapsTo() {
		Module qCircuit_instance = fullCircuit.getModule("M1");
		Assert.assertNotNull(qCircuit_instance);
		
		for(MapsTo wire : qCircuit_instance.getMapsTos()) {
			Assert.assertEquals(RefinementType.USELOCAL, wire.getRefinement());
			
			String local = wire.getLocal().getDisplayId();
			String remote = wire.getRemote().getDisplayId();
			if(local.equals("FC3_qnot")) {
				Assert.assertEquals("FC12_qnot", remote);
			}
			else if(local.equals("FC2_q")) {
				Assert.assertEquals("FC19_q", wire.getRemote().getDisplayId());
			}
			else if(local.equals("FC0_s")) {
				Assert.assertEquals("FC18_s", wire.getRemote().getDisplayId());
			}
		}
	}

	@Test
	public void Test_fullCircuitInputProteins() {
		String[] expectedProteins = new String[] {"FC0_s", "FC1_r"};
		
		for(int index = 0; index < expectedProteins.length; index++) {
			FunctionalComponent actualProtein = fullCircuit.getFunctionalComponent(expectedProteins[index]);
			Assert.assertNotNull(actualProtein);
			Assert.assertEquals(DirectionType.IN, actualProtein.getDirection());
		}
	}

	@Test
	public void Test_fullCircuitOutputProteins() {
		String[] expectedProteins = new String[] {"FC2_q", "FC3_qnot"};
		
		for(int index = 0; index < expectedProteins.length; index++) {
			FunctionalComponent actualProtein = fullCircuit.getFunctionalComponent(expectedProteins[index]);
			Assert.assertNotNull(actualProtein);
			Assert.assertEquals(DirectionType.OUT, actualProtein.getDirection());
		}
	}

	@Test
	public void Test_dnaSize() {
		int actualSize = 0; 
		for(ComponentDefinition cd : sbolDoc.getComponentDefinitions()) {
			if(cd.getTypes().iterator().next().equals(ComponentDefinition.DNA_REGION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(32, actualSize);
	}
	
	@Test
	public void Test_qSubcircuitInhibitionSize() {
		int actualSize = 0; 
		for(Interaction inter : subcircuit_q.getInteractions()) {
			if(inter.getTypes().iterator().next().equals(SystemsBiologyOntology.INHIBITION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(4, actualSize);
	}

	@Test
	public void Test_qnotSubcircuitInhibitionSize() {
		int actualSize = 0; 
		for(Interaction inter : subcircuit_qnot.getInteractions()) {
			if(inter.getTypes().iterator().next().equals(SystemsBiologyOntology.INHIBITION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(4, actualSize);
	}
	
	@Test
	public void Test_qSubcircuitProductionSize() {
		int actualSize = 0; 
		for(Interaction inter : subcircuit_q.getInteractions()) {
			if(inter.getTypes().iterator().next().equals(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(3, actualSize);
	}
	
	@Test
	public void Test_qnotSubcircuitProductionSize() {
		int actualSize = 0; 
		for(Interaction inter : subcircuit_qnot.getInteractions()) {
			if(inter.getTypes().iterator().next().equals(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
				actualSize++;
			}
		}
		Assert.assertEquals(3, actualSize);
	}
	
	@Test
	public void Test_FCtoCD() {
		for(FunctionalComponent fullCircuitProtein : fullCircuit.getFunctionalComponents()) {
			
			String fullCircuitId = fullCircuitProtein.getDisplayId();
			FunctionalComponent subcircuitProtein = null;
			switch(fullCircuitId) {
			case "FC0_s":
				subcircuitProtein = subcircuit_qnot.getFunctionalComponent("FC18_s");
				assertNotNull(subcircuitProtein);
				Assert.assertEquals("CD0_s", fullCircuitProtein.getDefinition().getDisplayId());
				Assert.assertTrue(fullCircuitProtein.getDefinition().equals(subcircuitProtein.getDefinition()));
				break;
			case "FC1_r":
				subcircuitProtein = subcircuit_q.getFunctionalComponent("FC10_r");
				assertNotNull(subcircuitProtein);
				Assert.assertEquals("CD1_r", fullCircuitProtein.getDefinition().getDisplayId());
				Assert.assertTrue(fullCircuitProtein.getDefinition().equals(subcircuitProtein.getDefinition()));
				break;
			case "FC2_q":
				subcircuitProtein = subcircuit_q.getFunctionalComponent("FC4_q");
				assertNotNull(subcircuitProtein);
				Assert.assertEquals("CD2_q", fullCircuitProtein.getDefinition().getDisplayId());
				Assert.assertTrue(fullCircuitProtein.getDefinition().equals(subcircuitProtein.getDefinition()));

				subcircuitProtein = subcircuit_qnot.getFunctionalComponent("FC19_q");
				assertNotNull(subcircuitProtein);
				Assert.assertEquals("CD2_q", fullCircuitProtein.getDefinition().getDisplayId());
				Assert.assertTrue(fullCircuitProtein.getDefinition().equals(subcircuitProtein.getDefinition()));
				break;
			case "FC3_qnot":
				subcircuitProtein = subcircuit_q.getFunctionalComponent("FC11_qnot"); 
				assertNotNull(subcircuitProtein);
				Assert.assertEquals("CD3_qnot", fullCircuitProtein.getDefinition().getDisplayId());
				Assert.assertTrue(fullCircuitProtein.getDefinition().equals(subcircuitProtein.getDefinition()));

				subcircuitProtein = subcircuit_qnot.getFunctionalComponent("FC12_qnot"); 
				assertNotNull(subcircuitProtein);
				Assert.assertEquals("CD3_qnot", fullCircuitProtein.getDefinition().getDisplayId());
				Assert.assertTrue(fullCircuitProtein.getDefinition().equals(subcircuitProtein.getDefinition()));
				break;
			default:
				Assert.fail("Unexpected protein found on full circuit with the following displayId " + fullCircuitId);
			}
			
		}
	}

}
