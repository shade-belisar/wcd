package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.TS.DistinctValuesTS;
import impl.TS.TripleSet;
import utility.InequalityHelper;
import utility.PrepareQueriesException;

public class DistinctValuesPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(DistinctValuesPCC.class);
	
	final DistinctValuesTS tripleSet;

	public DistinctValuesPCC(String property_) throws IOException {
		super(property_);
		tripleSet = new DistinctValuesTS(property);
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
		
		List<Rule> rules = new ArrayList<Rule>();
		
		InequalityHelper.setOrReset(reasoner);
		
		try {
			InequalityHelper.addUnequalConstantsToReasoner(tripleSet.getStatements());
		} catch (ReasonerStateException e) {
			logger.error("Trying to add unequal constants to reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		// violation_triple(S, I, propertyConstant, V)
		Atom violation_triple_SIpV = Expressions.makeAtom(violation_triple, s, i, propertyConstant, v);
		
		// tripleEDB(S, I, propertyConstant, V)
		Atom tripleEDB_SIpV = Expressions.makeAtom(tripleEDB, s, i, propertyConstant, v);
		
		// tripleEDB(O, X, propertyConstant, V)
		Atom tripleEDB_OXpV = Expressions.makeAtom(tripleEDB, o, x, propertyConstant, v);
		
		// unequal(S, O)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, s, o);
		
		//violation_triple(S, I, propertyConstant, V) :-
		//	tripleEDB(S, I, propertyConstant, V),
		//	tripleEDB(O, X, propertyConstant, V),
		//	unequal(S, O)
		Rule violation = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OXpV, unequal_SO);
		
		rules.add(violation);
		
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
