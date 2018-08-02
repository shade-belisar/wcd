/**
 * 
 */
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
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.CC.ScopeCC;
import impl.TS.PropertyPredicateTS;
import impl.TS.TripleSet;
import main.Main;
import utility.PrepareQueriesException;
import utility.Utility;

/**
 * @author adrian
 *
 */
public class ScopePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ScopePCC.class);
	
	final TripleSet tripleSet;
	
	final Set<String> qualifiers;
	
	public ScopePCC(String property_, Set<String> qualifiers_) throws IOException {
		super(property_);
		qualifiers = qualifiers_;
		tripleSet = new PropertyPredicateTS(property);
	}
	
	public String violations() throws IOException {
		if (!tripleSet.notEmpty())
			return "";
		
		try {
			loadTripleSets(tripleSet);
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}

		// violation_short(STATEMENT, propertyConstant, X)
		Atom violation_short_SpX = Expressions.makeAtom(violation_short, statement, propertyConstant, x);
		// violation_long(STATEMENT, X, propertyConstant, Y)
		Atom violation_long_SXpY = Expressions.makeAtom(violation_long, statement, x, propertyConstant, y);
		// tripleEDB(STATEMENT, X, propertyConstant, Y)
		Atom tripleEDB_SXpY = Expressions.makeAtom(tripleEDB, statement, x, propertyConstant, y);
		// qualifierEDB(STATEMENT, propertyConstant, X)
		Atom qualifierEDB_SpX = Expressions.makeAtom(qualifierEDB, statement, propertyConstant, x);
		// referenceEDB(STATEMENT, propertyConstant, X)
		Atom referenceEDB_SpX = Expressions.makeAtom(referenceEDB, statement, propertyConstant, x);
		
		// violation_long(STATEMENT, X, propertyConstant, Y) :- tripleEDB(STATEMENT, X, propertyConstant, Y)
		Rule notTriple = Expressions.makeRule(violation_long_SXpY, tripleEDB_SXpY);
		// violation(STATEMENT, propertyConstant, X) :- qualifierEDB(STATEMENT, propertyConstant, X)
		Rule notQualifier = Expressions.makeRule(violation_short_SpX, qualifierEDB_SpX);
		// violation(STATEMENT, propertyConstant, X) :- referenceEDB(STATEMENT, propertyConstant, X)
		Rule notReference = Expressions.makeRule(violation_short_SpX, referenceEDB_SpX);
		
		List<Rule> rules = new ArrayList<Rule>();
		rules.add(notTriple);
		rules.add(notQualifier);
		rules.add(notReference);
		
		if (allowedAs(ScopeCC.AS_MAIN_VALUE))
			rules.remove(notTriple);
		
		if (allowedAs(ScopeCC.AS_QUALIFIER))
			rules.remove(notQualifier);
		
		if (allowedAs(ScopeCC.AS_REFERENCE))
			rules.remove(notReference);
		
		try {
			return prepareAndExecuteQueries(rules, Arrays.asList(violation_long_query, violation_short_query));
		} catch (PrepareQueriesException e1) {
			return e1.getMessage();
		}
	}
	
	protected boolean allowedAs(String qualifier) {
		boolean result = false;
		for (String allowed : qualifiers) {
			if (allowed.equals(Utility.BASE_URI + qualifier))
				result = true;
		}
		return result;
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}