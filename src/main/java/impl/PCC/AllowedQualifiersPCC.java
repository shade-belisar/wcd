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
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.TS.AllowedQualifiersTS;
import impl.TS.TripleSet;
import utility.InequalityHelper;
import utility.PrepareQueriesException;

public class AllowedQualifiersPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(AllowedQualifiersPCC.class);
	
	final AllowedQualifiersTS tripleSet;
	
	final Set<String> allowedQualifiers;
	
	final static String O = "o";
	final static String Q = "q";
	
	final static Variable o = Expressions.makeVariable(O);
	final static Variable q = Expressions.makeVariable(Q);

	public AllowedQualifiersPCC(String property_, Set<String> allowedQualifiers_) throws IOException {
		super(property_);
		tripleSet = new AllowedQualifiersTS(property);
		allowedQualifiers = allowedQualifiers_;
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
			Set<String> qualifiers = tripleSet.getQualifierProperties();
			qualifiers.addAll(allowedQualifiers);
			InequalityHelper.addUnequalConstantsToReasoner(qualifiers);
		} catch (ReasonerStateException e) {
			logger.error("Trying to add unequal constants to reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		    	
		List<Rule> rules = new ArrayList<Rule>();
		
		// qualifierEDB(S, Q, O)
		Atom qualfierEDB_SQO = Expressions.makeAtom(qualifierEDB, s, q, o);
		
		List<Atom> violation_conjunction = new ArrayList<Atom>();
		violation_conjunction.add(tripleEDB_SIpV);
		violation_conjunction.add(qualfierEDB_SQO);
		for (String allowedQualifier : allowedQualifiers) {
			Constant allowedQualifierConstant = Expressions.makeConstant(allowedQualifier);
			
			// unequal({A}, Q)
			Atom unequal_AQ = Expressions.makeAtom(InequalityHelper.unequal, allowedQualifierConstant, q);
			violation_conjunction.add(unequal_AQ);
		}
		
		// violation_triple(S, I, propertyConstant, V) :- tripleEDB(S, I, propertyConstant, V), qualifierEDB(S, Q, O), unequal({A}, Q)
		Rule violation = Expressions.makeRule(violation_triple_SIpV, toArray(violation_conjunction));
		rules.add(violation);

		try {
			return prepareAndExecuteQueries(rules, Arrays.asList(violation_triple_query));
		} catch (PrepareQueriesException e) {
			return e.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
