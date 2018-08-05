package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;

import impl.TS.AllowedUnitsTS;
import impl.TS.TripleSet;
import utility.InequalityHelper;
import utility.PrepareQueriesException;

public class AllowedUnitsPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(AllowedUnitsPCC.class);
	
	final AllowedUnitsTS tripleSet;
	
	final HashSet<String> allowedUnits;
	
	final static String U = "u";
	final static String UNIT = "unit";
	
	final static Variable u = Expressions.makeVariable(U);
	final static Predicate unit = Expressions.makePredicate(UNIT, 2);

	public AllowedUnitsPCC(String property_, HashSet<String> allowedUnits_) throws IOException {
		super(property_);
		tripleSet = new AllowedUnitsTS(property);
		allowedUnits = allowedUnits_;
	}

	@Override
	public String violations() throws IOException {
		if (!tripleSet.notEmpty())
			return "";
		
		try {
			loadTripleSets(tripleSet);
			if (tripleSet.unitsNotEmpty()) {
				final DataSource unitsEDBPath = new CsvFileDataSource(tripleSet.getUnitsFile());
				reasoner.addFactsFromDataSource(unit, unitsEDBPath);
			}
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		InequalityHelper.setOrReset(reasoner);
		try {
			Set<String> units = tripleSet.getUnits();
			units.addAll(allowedUnits);
			InequalityHelper.addUnequalConstantsToReasoner(units);
		} catch (ReasonerStateException e) {
			logger.error("Trying to add unequal constants to reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}

		List<Rule> rules = new ArrayList<Rule>();

		List<Atom> unequal_conjunction = new ArrayList<Atom>();
		// unit(V, U)
		Atom unit_VU = Expressions.makeAtom(unit, v, u);
		unequal_conjunction.add(unit_VU);
		for (String unit : allowedUnits) {
			Constant unitConstant = Expressions.makeConstant(unit);

			// unequal({A}, U)
			Atom unequal_AU = Expressions.makeAtom(InequalityHelper.unequal, unitConstant, u);
			
			unequal_conjunction.add(unequal_AU);
		}
		
		// violation_triple(S, I, propertyConstant, V)
		Atom violation_triple_SIpV = Expressions.makeAtom(violation_triple, s, i, propertyConstant, v);
		
		// tripleEDB(S, I, propertyConstant, V)
		Atom tripleEDB_SIpV = Expressions.makeAtom(tripleEDB, s, i, propertyConstant, v);

		
		List<Atom> violation_triple_conjunction = new ArrayList<Atom>();
		violation_triple_conjunction.add(tripleEDB_SIpV);
		violation_triple_conjunction.addAll(unequal_conjunction);
		
		// violation_triple(S, I, propertyConstant, V) :-  tripleEDB(S, I, propertyConstant, V), unit(V, U), unequal({A}, U)
		Rule tripleViolation = Expressions.makeRule(violation_triple_SIpV, toArray(violation_triple_conjunction));
		rules.add(tripleViolation);
		
		// violation_qualifier(S, propertyConstant, V)
		Atom violation_qualifier_SIpV = Expressions.makeAtom(violation_qualifier, s, propertyConstant, v);
		
		// qualifierEDB(S, propertyConstant, V)
		Atom qualifierEDB_SIpV = Expressions.makeAtom(qualifierEDB, s, propertyConstant, v);
	
		List<Atom> violation_qualifier_conjunction = new ArrayList<Atom>();
		violation_qualifier_conjunction.add(qualifierEDB_SIpV);
		violation_qualifier_conjunction.addAll(unequal_conjunction);
		
		// violation_qualifier(S, propertyConstant, V) :- qualifierEDB(S, propertyConstant, V), unit(V, U), unequal({A}, U)
		Rule qualifierViolation = Expressions.makeRule(violation_qualifier_SIpV, toArray(violation_qualifier_conjunction));
		
		// violation_reference(S, propertyConstant, V)
		Atom violation_reference_SIpV = Expressions.makeAtom(violation_reference, s, propertyConstant, v);
		
		// referenceEDB(S, propertyConstant, V)
		Atom referenceEDB_SpV = Expressions.makeAtom(referenceEDB, s, propertyConstant, v);
		
		List<Atom> violation_reference_conjunction = new ArrayList<Atom>();
		violation_reference_conjunction.add(qualifierEDB_SIpV);
		violation_reference_conjunction.addAll(unequal_conjunction);
		
		// violation_reference(S, propertyConstant, V) :- referenceEDB(S, propertyConstant, V), unit(V, U), unequal({A}, U)
		Rule referenceViolation = Expressions.makeRule(violation_reference_SIpV, toArray(violation_reference_conjunction));
		
		try {
			return prepareAndExecuteQueries(rules, violation_triple_query, violation_qualifier_query, violation_reference_query);
		} catch (PrepareQueriesException e1) {
			return e1.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
