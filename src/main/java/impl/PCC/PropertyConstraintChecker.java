/**
 * 
 */
package impl.PCC;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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

import impl.TS.TripleSet;
import utility.PrepareQueriesException;
import utility.Utility;

/**
 * @author adrian
 *
 */
public abstract class PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(PropertyConstraintChecker.class);
	
	protected final Reasoner reasoner = Reasoner.getInstance();
	
	public final static String TRIPLE = "tripleEBD";
	public final static String QUALIFIER = "qualifierEDB";
	public final static String REFERENCE = "referenceEDB";
	
	public final static String VIOLATION_TRIPLE = "violation_triple";
	public final static String VIOLATION_QUALIFIER = "violation_qualifier";
	public final static String VIOLATION_REFERENCE = "violation_reference";
	
	public final static String S = "s";
	public final static String O = "o";
	public final static String I = "i";
	public final static String X = "x";
	public final static String P = "p";
	public final static String V = "v";
	public final static String C = "c";
	
	public final static Predicate tripleEDB = Expressions.makePredicate(TRIPLE, 4);
	public final static Predicate qualifierEDB = Expressions.makePredicate(QUALIFIER, 3);
	public final static Predicate referenceEDB = Expressions.makePredicate(REFERENCE, 3);
	
	public final static Predicate violation_triple = Expressions.makePredicate(VIOLATION_TRIPLE, 4);
	public final static Predicate violation_qualifier = Expressions.makePredicate(VIOLATION_QUALIFIER, 3);
	public final static Predicate violation_reference = Expressions.makePredicate(VIOLATION_REFERENCE, 3);
	
	public final static Variable s = Expressions.makeVariable(S);
	public final static Variable o = Expressions.makeVariable(O);
	public final static Variable i = Expressions.makeVariable(I);
	public final static Variable x = Expressions.makeVariable(X);
	public final static Variable p = Expressions.makeVariable(P);
	public final static Variable v = Expressions.makeVariable(V);
	public final static Variable c = Expressions.makeVariable(C);

	public final static Atom violation_triple_query = Expressions.makeAtom(violation_triple, s, i, p, v);
	public final static Atom violation_qualifier_query = Expressions.makeAtom(violation_qualifier, s, p, v);
	public final static Atom violation_reference_query = Expressions.makeAtom(violation_reference, s, p, v);
	
	protected final Constant propertyConstant;
	
	protected final String property;
	
	protected final String internalError;
	
	public PropertyConstraintChecker(String property_) throws IOException {
		property = property_;
		internalError = "INTERNAL_ERROR for property " + property + ".";
		propertyConstant = Utility.makeConstant(property);
		
		reasoner.setAlgorithm(Algorithm.RESTRICTED_CHASE);
	}
	
	protected void loadTripleSets(TripleSet... sets) throws ReasonerStateException, IOException {
		for (TripleSet tripleSet : sets) {
			if (tripleSet.tripleNotEmpty()) {
				final DataSource tripleEDBPath = new CsvFileDataSource(tripleSet.getTripleFile());
				reasoner.addFactsFromDataSource(tripleEDB, tripleEDBPath);
			}
			if (tripleSet.qualifierNotEmpty()) {
				final DataSource qualifierEDBPath = new CsvFileDataSource(tripleSet.getQualifierFile());
				reasoner.addFactsFromDataSource(qualifierEDB, qualifierEDBPath);
			}
			if (tripleSet.referenceNotEmpty()) {
				final DataSource referenceEDBPath = new CsvFileDataSource(tripleSet.getReferenceFile());
				reasoner.addFactsFromDataSource(referenceEDB, referenceEDBPath);				
			}
		}
	}
	
	protected String prepareAndExecuteQueries(List<Rule> rules, List<Atom> queries) throws IOException, PrepareQueriesException {
		try {
			reasoner.addRules(rules);
		} catch (ReasonerStateException e) {
			logger.error("Trying to add rules in the wrong state for propery " + property + ".", e);
			throw new PrepareQueriesException(internalError);
		}
		
		try {
			reasoner.load();
		} catch (EdbIdbSeparationException e) {
			logger.error("EDB rule occured in IDB for property " + property + ".", e);
			throw new PrepareQueriesException(internalError);
		} catch (IncompatiblePredicateArityException e) {
			logger.error("Predicate does not match the datasource for property " + property + ".", e);
			throw new PrepareQueriesException(internalError);
		}
		
		try {
			reasoner.reason();
		} catch (ReasonerStateException e) {
			logger.error("Trying to reason in the wrong state for property " + property + ".", e);
			throw new PrepareQueriesException(internalError);
		}
		String result = "";
		for (Atom query : queries) {
	    	try (QueryResultIterator iterator = reasoner.answerQuery(query, true)) {
	    		result += result(iterator);
	    	} catch (ReasonerStateException e) {
				logger.error("Trying to answer query in the wrong state for property " + property + ".", e);
				throw new PrepareQueriesException(internalError);
			}
		}
    	
    	reasoner.close();
    	
    	if (!result.equals(""))
    		result = "Property: " + property + "\n" + result;

    	return result;
	}
	
	public abstract String violations() throws IOException;
	
	protected abstract Set<TripleSet> getRequiredTripleSets() throws IOException;
	
	public void close() throws IOException {
		for (TripleSet tripleSet : getRequiredTripleSets()) {
			tripleSet.close();
		}
	}
	
	protected String result(QueryResultIterator queryResultIterator) {
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
	public String toString() {
		String result = "Property id: " + property + "\n";
		try {
			for (TripleSet tripleSet : getRequiredTripleSets()) {
				result += "  " + tripleSet + "\n";
			}
		} catch (IOException e) {
			result += "INTERNAL ERROR: Could not get triple sets. " + e.getMessage();
		}
		return result;
	}

}
