package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import impl.TS.PropertyPredicateTS;
import impl.TS.TripleSet;
import utility.PrepareQueriesException;

public class NoneOfPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(NoneOfPCC.class);
	
	final TripleSet tripleSet;
	
	final Set<String> qualifiers;

	public NoneOfPCC(String property_, Set<String> qualifiers_) throws IOException {
		super(property_);
		qualifiers = qualifiers_;
		tripleSet = new PropertyPredicateTS(property);
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
		
		
		for (String notAllowedValue : qualifiers) {
			
			Constant confValueConstant =  Expressions.makeConstant(notAllowedValue);
			
			// violation_long(STATEMENT, X, propertyConstant, Y)
			Atom violation_long_SXpc = Expressions.makeAtom(violation_long, statement, x, propertyConstant, confValueConstant);
			
			// tripleEDB(STATEMENT, X, propertyConstant, confValueConstant)
			Atom tripleEDB_SXpc = Expressions.makeAtom(tripleEDB, statement, x, propertyConstant, confValueConstant);
			
			// violation_long(STATEMENT, X, propertyConstant, Y) :- tripleEDB(STATEMENT, X, propertyConstant, confValueConstant)
			Rule conflict = Expressions.makeRule(violation_long_SXpc, tripleEDB_SXpc);
			
			rules.add(conflict);
		}
		
		try {
			return prepareAndExecuteQueries(rules, Arrays.asList(violation_long_query));
		} catch (PrepareQueriesException e) {
			return e.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
