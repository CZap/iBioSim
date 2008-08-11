package gcm2sbml.visitor;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Properties;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SpeciesReference;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.network.Promoter;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

public class PrintActivatedProductionVisitor extends AbstractPrintVisitor {

	public PrintActivatedProductionVisitor(SBMLDocument document, Promoter p,
			Collection<SpeciesInterface> species, double act, double stoc) {
		super(document);
		this.defaultact = act;
		this.promoter = p;
		this.species = species;
		this.defaultstoc = stoc;
	}

	/**
	 * Prints out all the species to the file
	 * 
	 */
	public void run() {
		for (SpeciesInterface specie : species) {
			specie.accept(this);
		}
	}

	public void visitSpecies(SpeciesInterface specie) {
		// TODO Auto-generated method stub

	}

	public void visitDimer(DimerSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = new Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addReactant(new SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(new SpeciesReference(species.getId(), stoc));
		}
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);

	}

	public void visitBiochemical(BiochemicalSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = new Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addReactant(new SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(new SpeciesReference(species.getId(), stoc));
		}
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);
	}

	public void visitBaseSpecies(BaseSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = new Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addReactant(new SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(new SpeciesReference(species.getId(), stoc));
		}
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);
	}

	public void visitConstantSpecies(ConstantSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = new Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addReactant(new SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(new SpeciesReference(species.getId(), stoc));
		}
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);
	}

	public void visitSpasticSpecies(SpasticSpecies specie) {
		loadValues(specie.getProperties());
		Reaction r = new Reaction("R_act_production_" + promoter.getId() + "_"
				+ specie.getId());
		r.addReactant(new SpeciesReference("RNAP_" + "_" + promoter.getId()
				+ "_" + specie.getId(), 1));
		for (SpeciesInterface species : promoter.getOutputs()) {
			r.addProduct(new SpeciesReference(species.getId(), stoc));
		}
		r.addProduct(new SpeciesReference("RNAP_" + promoter.getId() + "_"
				+ specie.getId(), 1));
		r.setReversible(false);
		r.setFast(false);
		KineticLaw kl = new KineticLaw();
		kl.addParameter(new Parameter(actString, act, GeneticNetwork
				.getMoleTimeParameter(1)));
		kl.setFormula(actString+ "*RNAP_" + promoter.getId() + "_"
				+ specie.getId());
		r.setKineticLaw(kl);
		Utility.addReaction(document, r);
	}

	private void loadValues(Properties property) {
		stoc = getProperty(GlobalConstants.STOICHIOMETRY_STRING, property,
				defaultstoc);
		act = getProperty(GlobalConstants.ACTIVED_STRING, property, defaultact);
	}

	private Promoter promoter = null;

	private double act = .25;
	private double stoc = 1;

	private double defaultact = .25;
	private double defaultstoc = 1;

	private String actString = CompatibilityFixer
			.getSBMLName(GlobalConstants.ACTIVED_STRING);
	private String stocString = CompatibilityFixer
			.getSBMLName(GlobalConstants.STOICHIOMETRY_STRING);

	private Collection<SpeciesInterface> species = null;

}
