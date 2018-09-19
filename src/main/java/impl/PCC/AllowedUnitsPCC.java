package impl.PCC;

import static utility.SC.u;
import static utility.SC.unit;
import static utility.SC.v;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import utility.InequalityHelper;
import utility.Utility;

public class AllowedUnitsPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(AllowedUnitsPCC.class);
	
	final HashSet<String> allowedUnits;

	public AllowedUnitsPCC(String property_, HashSet<String> allowedUnits_) throws IOException {
		super(property_);
		allowedUnits = allowedUnits_;
	}

	@Override
	public Set<Rule> rules() {
		Set<Rule> rules = new HashSet<Rule>();

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
		
		List<Atom> violation_statement_conjunction = new ArrayList<Atom>();
		violation_statement_conjunction.add(statementEDB_SIpV);
		violation_statement_conjunction.addAll(unequal_conjunction);
		
		// violation_statement(S, I, propertyConstant, V) :-  statementEDB(S, I, propertyConstant, V), unit(V, U), unequal({A}, U)
		Rule statementViolation = Expressions.makeRule(violation_statement_SIpV, Utility.toArray(violation_statement_conjunction));
		rules.add(statementViolation);
	
		List<Atom> violation_qualifier_conjunction = new ArrayList<Atom>();
		violation_qualifier_conjunction.add(qualifierEDB_SpV);
		violation_qualifier_conjunction.addAll(unequal_conjunction);
		
		// violation_qualifier(S, propertyConstant, V) :- qualifierEDB(S, propertyConstant, V), unit(V, U), unequal({A}, U)
		Rule qualifierViolation = Expressions.makeRule(violation_qualifier_SpV, Utility.toArray(violation_qualifier_conjunction));
		rules.add(qualifierViolation);
		
		List<Atom> violation_reference_conjunction = new ArrayList<Atom>();
		violation_reference_conjunction.add(referenceEDB_SHpV);
		violation_reference_conjunction.addAll(unequal_conjunction);
		
		// violation_reference(S, H, propertyConstant, V) :- referenceEDB(S, H, propertyConstant, V), unit(V, U), unequal({A}, U)
		Rule referenceViolation = Expressions.makeRule(violation_reference_SHpV, Utility.toArray(violation_reference_conjunction));
		rules.add(referenceViolation);
		
		return rules;
	}
}
