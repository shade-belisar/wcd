package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import utility.InequalityHelper;
import utility.Utility;

import static utility.SC.qualifierEDB;

import static utility.SC.s;
import static utility.SC.o;

public class OneOfQualifierValuePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(OneOfQualifierValuePCC.class);
	
	final Map<String, Set<String>> qualifiersAndValues;

	public OneOfQualifierValuePCC(String property_, Map<String, Set<String>> qualifiersAndValues_) throws IOException {
		super(property_);
		qualifiersAndValues = qualifiersAndValues_;
	}

	@Override
	public List<Rule> rules() {  	
		List<Rule> rules = new ArrayList<Rule>();
		
		for (Map.Entry<String, Set<String>> entry : qualifiersAndValues.entrySet()) {
			Constant qualifierPropertyConstant = Utility.makeConstant(entry.getKey());
			// qualifierEDB(S, qualifierPropertyConstnat, O)
			Atom qualifierEDB_SqO = Expressions.makeAtom(qualifierEDB, s, qualifierPropertyConstant, o);
			
			List<Atom> violation_conjunction = new ArrayList<Atom>();
			violation_conjunction.add(tripleEDB_SIpV);
			violation_conjunction.add(qualifierEDB_SqO);
			for (String allowedValue : entry.getValue()) {
				Constant allowedValueConstant = Utility.makeConstant(allowedValue);
				// unequal({A}, O)
				Atom unequal_AO = Expressions.makeAtom(InequalityHelper.unequal, allowedValueConstant, o);
				
				violation_conjunction.add(unequal_AO);
			}
			
			// violation_triple(S, I, propertyConstant, V) :- tripleEDB(S, I, propertyConstant, V), qualifierEDB(S, qualifierPropertyConstant, O), unequal({A}, O)
			Rule violation = Expressions.makeRule(violation_triple_SIpV, toArray(violation_conjunction));
			rules.add(violation);
		}

		return rules;
	}
}
