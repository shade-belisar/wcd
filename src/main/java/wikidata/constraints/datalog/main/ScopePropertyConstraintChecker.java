/**
 * 
 */
package wikidata.constraints.datalog.main;

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

import wikidata.constraints.datalog.rdf.ScopeTripleSet;
import wikidata.constraints.datalog.rdf.TripleSet;

/**
 * @author adrian
 *
 */
public class ScopePropertyConstraintChecker extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ScopePropertyConstraintChecker.class);
	
	final static String TRIPLE = "triple";
	final static String QUALIFIER = "qualifier";
	final static String REFERENCE = "reference";
	
	final static String STATEMENT = "statement";
	final static String VIOLATION_SHORT = "violation_short";
	final static String VIOLATION_LONG = "violation_long";
	
	final static String X = "x";
	final static String Y = "y";
	
	final String TRIPLE_SET = "triple_set";
	
	final Predicate tripleEDB = Expressions.makePredicate(TRIPLE, 4);
	final Predicate qualifierEDB = Expressions.makePredicate(QUALIFIER, 3);
	final Predicate referenceEDB = Expressions.makePredicate(REFERENCE, 3);
	
	final Variable statement = Expressions.makeVariable(STATEMENT);
	final Predicate violation_short = Expressions.makePredicate(VIOLATION_SHORT, 3);
	final Predicate violation_long = Expressions.makePredicate(VIOLATION_LONG, 4);
	
	
	final Variable x = Expressions.makeVariable(X);
	final Variable y = Expressions.makeVariable(Y);
	
	public ScopePropertyConstraintChecker(String constraint_, Map<String, String> qualifiers_) throws IOException {
		super(constraint_, qualifiers_);
	}
	
	public String violations() throws IOException {
		TripleSet tripleSet = tripleSets.get(TRIPLE_SET);
		
		if (!tripleSet.notEmpty())
			return "";
		
		final Reasoner reasoner = Reasoner.getInstance();
		reasoner.setAlgorithm(Algorithm.RESTRICTED_CHASE);
		
		final DataSource tripleEDBPath = new CsvFileDataSource(tripleSet.getTripleSetFile());
		final DataSource qualifierEDBPath = new CsvFileDataSource(tripleSet.getQualifierTripleSetFile());
		final DataSource referenceEDBPath = new CsvFileDataSource(tripleSet.getReferenceTripleSetFile());
		
		try {
			reasoner.addFactsFromDataSource(tripleEDB, tripleEDBPath);
			reasoner.addFactsFromDataSource(qualifierEDB, qualifierEDBPath);
			reasoner.addFactsFromDataSource(referenceEDB, referenceEDBPath);
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
		
		for (String allowed : qualifiers.get(ScopeConstraintChecker.SCOPE).split(",")) {
			if (allowed.equals(Main.BASE_URI + ScopeConstraintChecker.AS_MAIN_VALUE))
				rules.remove(notTriple);
			if (allowed.equals(Main.BASE_URI + ScopeConstraintChecker.AS_QUALIFIER))
				rules.remove(notQualifier);
			if (allowed.equals(Main.BASE_URI + ScopeConstraintChecker.AS_REFERENCE))
				rules.remove(notReference);
		}
		
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
	Map<String, TripleSet> getRequiredTripleSets(String property, Map<String, String> qualifiers) throws IOException {
		Map<String, TripleSet> result = new HashMap<String, TripleSet>();
		result.put(TRIPLE_SET, new ScopeTripleSet(property, qualifiers));
		return result;
	}

}
