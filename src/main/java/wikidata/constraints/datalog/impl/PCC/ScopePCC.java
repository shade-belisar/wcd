/**
 * 
 */
package wikidata.constraints.datalog.impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.QueryResult;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Algorithm;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;
import org.semanticweb.vlog4j.core.reasoner.implementation.QueryResultIterator;

import wikidata.constraints.datalog.impl.ScopeCC;
import wikidata.constraints.datalog.main.Main;
import wikidata.constraints.datalog.main.PropertyConstraintChecker;
import wikidata.constraints.datalog.rdf.ScopeTripleSet;
import wikidata.constraints.datalog.rdf.TripleSet;

/**
 * @author adrian
 *
 */
public class ScopePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ScopePCC.class);
	
	final String TRIPLE_SET = "triple_set";
	
	public ScopePCC(String property_, Map<String, String> qualifiers_) throws IOException {
		super(property_, qualifiers_);
	}
	
	public String violations() throws IOException {
		TripleSet tripleSet = tripleSets.get(TRIPLE_SET);
		
		if (!tripleSet.notEmpty())
			return "";
		
		try {
			loadTripleSets(tripleSet);
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
			return "INTERNAL ERROR for property " + property + ".";
		}

		Constant propertyConstant = Expressions.makeConstant(Main.BASE_URI + property);
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
			reasoner.addRules(rules);
		} catch (ReasonerStateException e) {
			logger.error("Trying to add rules in the wrong state for propery " + property + ".", e);
		}
		
		try {
			reasoner.load();
		} catch (EdbIdbSeparationException e) {
			logger.error("EDB rule occured in IDB for property " + property + ".", e);
			return "INTERNAL ERROR for property " + property + ".";
		} catch (IncompatiblePredicateArityException e) {
			logger.error("Predicate does not match the datasource for property " + property + ".", e);
			return "INTERNAL ERROR for property " + property + ".";
		}
		
		try {
			reasoner.reason();
		} catch (ReasonerStateException e) {
			logger.error("Trying to reason in the wrong state for property " + property + ".", e);
			return "INTERNAL ERROR for property " + property + ".";
		}

		String result = "";
    	try (QueryResultIterator iterator = reasoner.answerQuery(violation_short_SpX, true)) {
    		result += result(iterator);
    	} catch (ReasonerStateException e) {
			logger.error("Trying to answer query in the wrong state for property " + property + ".", e);
			return "INTERNAL ERROR for property " + property + ".";
		}
    	try (QueryResultIterator iterator = reasoner.answerQuery(violation_long_SXpY, true)) {
    		result += result(iterator);
    	} catch (ReasonerStateException e) {
			logger.error("Trying to answer query in the wrong state for property " + property + ".", e);
			return "INTERNAL ERROR for property " + property + ".";
		}
    	reasoner.close();
    	return result;
	}
	
	protected boolean allowedAs(String qualifier) {
		boolean result = false;
		for (String allowed : qualifiers.get(ScopeCC.SCOPE).split(",")) {
			if (allowed.equals(Main.BASE_URI + qualifier))
				result = true;
		}
		return result;
	}

	String result(QueryResultIterator queryResultIterator) {
		String result = ""; 
		while (queryResultIterator.hasNext()) {
			QueryResult queryResult = queryResultIterator.next();
			String triple = "";
			for (Term term : queryResult.getTerms()) {
				triple += term.getName() + "\t";
			}
			
			result += triple.substring(0, triple.length() - 1) + "\n";
		}
		return result;
	}

	@Override
	protected Map<String, TripleSet> getRequiredTripleSets(String property, Map<String, String> qualifiers) throws IOException {
		Map<String, TripleSet> result = new HashMap<String, TripleSet>();
		result.put(TRIPLE_SET, new ScopeTripleSet(property, qualifiers));
		return result;
	}

}
