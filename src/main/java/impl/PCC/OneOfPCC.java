package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import utility.InequalityHelper;
import static utility.SC.v;

public class OneOfPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(OneOfPCC.class);
	
	final Set<String> allowedValues;

	public OneOfPCC(String property_, Set<String> allowedValues_) throws IOException {
		super(property_);
		allowedValues = allowedValues_;
	}

	@Override
	public List<Rule> rules() {	    	
		List<Rule> rules = new ArrayList<Rule>();
		
		List<Atom> unequal_conjunction = new ArrayList<Atom>();
		for (String allowedValue : allowedValues) {
			Constant allowedValueConstant = Expressions.makeConstant(allowedValue);
			
			// unequal({A}, V)
			Atom unequal_AV = Expressions.makeAtom(InequalityHelper.unequal, allowedValueConstant, v);
			unequal_conjunction.add(unequal_AV);
		}
		
		List<Atom> violation_triple_conjunction = new ArrayList<Atom>();
		violation_triple_conjunction.add(tripleEDB_SIpV);
		violation_triple_conjunction.addAll(unequal_conjunction);

		// violation_triple(S, I, propertyConstant, V) :- tripleEDB(S, I, propertyConstant, V), unequal({A}, V)
		Rule violationTriple = Expressions.makeRule(violation_triple_SIpV, toArray(violation_triple_conjunction));
		
		List<Atom> violation_qualifier_conjunction = new ArrayList<Atom>();
		violation_qualifier_conjunction.add(qualifierEDB_SpV);
		violation_qualifier_conjunction.addAll(unequal_conjunction);
		
		// violation_qualifier(S, propertyConstant, V) :- qualifierEDB(S, propertyConstant, V), unequal({A}, V)
		Rule violationQualifier = Expressions.makeRule(violation_qualifier_SpV, toArray(violation_qualifier_conjunction));
		
		List<Atom> violation_reference_conjunction = new ArrayList<Atom>();
		violation_reference_conjunction.add(referenceEDB_SpV);
		violation_reference_conjunction.addAll(unequal_conjunction);
		
		// violation_reference(S, propertyConstant, V) :- referenceEDB(S, propertyConstant, V), unequal({A}, V)
		Rule violationReference = Expressions.makeRule(violation_qualifier_SpV, toArray(violation_reference_conjunction));
		
		rules.add(violationTriple);
		rules.add(violationQualifier);
		rules.add(violationReference);
		
		return rules;
	}
}
