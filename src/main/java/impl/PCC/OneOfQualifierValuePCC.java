package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.TS.OneOfQualifierValueTS;
import impl.TS.TripleSet;
import utility.InequalityHelper;
import utility.PrepareQueriesException;
import utility.Utility;

public class OneOfQualifierValuePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(OneOfQualifierValuePCC.class);
	
	final OneOfQualifierValueTS tripleSet;
	
	final Map<String, HashSet<String>> qualifiersAndValues;

	public OneOfQualifierValuePCC(String property_, Map<String, HashSet<String>> qualifiersAndValues_) throws IOException {
		super(property_);
		qualifiersAndValues = qualifiersAndValues_;
		tripleSet = new OneOfQualifierValueTS(property, qualifiersAndValues.keySet());
	}

	@Override
	public String violations() throws IOException {
		if (!tripleSet.notEmpty())
			return "";
		
		try {
			loadTripleSets(tripleSet);
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		InequalityHelper.setOrReset(reasoner);
		
		try {
			Set<String> values = tripleSet.getValues();
			for (Set<String> set : qualifiersAndValues.values()) {
				values.addAll(set);
			}
			InequalityHelper.addUnequalConstantsToReasoner(values);
		} catch (ReasonerStateException e) {
			logger.error("Trying to add unequal constants to reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		    	
		List<Rule> rules = new ArrayList<Rule>();
		
		for (Map.Entry<String, HashSet<String>> entry : qualifiersAndValues.entrySet()) {
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

		try {
			return prepareAndExecuteQueries(rules, violation_triple_query);
		} catch (PrepareQueriesException e) {
			return e.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
