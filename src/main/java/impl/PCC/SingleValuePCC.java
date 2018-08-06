package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.TS.SingleValueTS;
import impl.TS.TripleSet;
import utility.InequalityHelper;
import utility.PrepareQueriesException;

public class SingleValuePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SingleValuePCC.class);
	
	final SingleValueTS tripleSet;
	
	final Set<String> separators;

	public SingleValuePCC(String property_, Set<String> separators_) throws IOException {
		super(property_);
		separators = separators_;
		tripleSet = new SingleValueTS(property, separators);
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
			InequalityHelper.addUnequalConstantsToReasoner(tripleSet.getStatementIDs());
		} catch (ReasonerStateException e) {
			logger.error("Trying to add unequal constants to reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		    	
		List<Rule> rules = new ArrayList<Rule>();
		
		// tripleEDB(O, I, propertyConstant, C)
		Atom tripleEDB_OIpC = Expressions.makeAtom(tripleEDB, o, i, propertyConstant, c);
		
		// unequal (S, O)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, s, o);
		
		if (separators.size() == 0) {
			// violation_triple(S, I, propertyConstant, V) :-
			//	tripleEDB(S, I, propertyConstant, V),
			//	tripleEDB(O, I, propertyConstant, C),
			//	unequal (S, O)
			Rule conflict = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OIpC, unequal_SO);
			
			rules.add(conflict);
			
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
