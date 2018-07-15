/**
 * 
 */
package wikidata.constraints.datalog.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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

import wikidata.constraints.datalog.impl.PCC.ScopePCC;
import wikidata.constraints.datalog.rdf.TripleSet;

/**
 * @author adrian
 *
 */
public abstract class PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(PropertyConstraintChecker.class);
	
	protected final static String TRIPLE = "triple";
	protected final static String QUALIFIER = "qualifier";
	protected final static String REFERENCE = "reference";
	
	protected final static String VIOLATION_SHORT = "violation_short";
	protected final static String VIOLATION_LONG = "violation_long";
	
	protected final static String X = "x";
	protected final static String Y = "y";
	protected final static String Z = "z";
	
	protected final static String STATEMENT = "statement";
	protected final static String OTHER_STATEMENT = "otherStatement";
	
	protected final Predicate tripleEDB = Expressions.makePredicate(TRIPLE, 4);
	protected final Predicate qualifierEDB = Expressions.makePredicate(QUALIFIER, 3);
	protected final Predicate referenceEDB = Expressions.makePredicate(REFERENCE, 3);
	
	protected final Predicate violation_short = Expressions.makePredicate(VIOLATION_SHORT, 3);
	protected final Predicate violation_long = Expressions.makePredicate(VIOLATION_LONG, 4);
	
	protected final Variable x = Expressions.makeVariable(X);
	protected final Variable y = Expressions.makeVariable(Y);
	protected final Variable z = Expressions.makeVariable(Z);
	
	protected final Variable statement = Expressions.makeVariable(STATEMENT);
	protected final Variable otherStatement = Expressions.makeVariable(OTHER_STATEMENT);
	
	protected final Reasoner reasoner = Reasoner.getInstance();
	
	protected final Constant propertyConstant;
	
	protected final String property;
	
	protected Map<String, TripleSet> tripleSets = new HashMap<String, TripleSet>();
	
	public PropertyConstraintChecker(String property_) throws IOException {
		property = property_;
		tripleSets = getRequiredTripleSets(property);
		propertyConstant = makeConstant(property);
		
		reasoner.setAlgorithm(Algorithm.RESTRICTED_CHASE);
	}
	
	protected Constant makeConstant(String id) {
		return Expressions.makeConstant(Main.BASE_URI + id);
	}
	
	protected void loadTripleSets(TripleSet... sets) throws ReasonerStateException, IOException {
		for (TripleSet tripleSet : sets) {
			if (tripleSet.tripleNotEmpty()) {
				final DataSource tripleEDBPath = new CsvFileDataSource(tripleSet.getTripleSetFile());
				reasoner.addFactsFromDataSource(tripleEDB, tripleEDBPath);
			}
			if (tripleSet.qualifierNotEmpty()) {
				final DataSource qualifierEDBPath = new CsvFileDataSource(tripleSet.getQualifierTripleSetFile());
				reasoner.addFactsFromDataSource(qualifierEDB, qualifierEDBPath);
			}
			if (tripleSet.referenceNotEmpty()) {
				final DataSource referenceEDBPath = new CsvFileDataSource(tripleSet.getReferenceTripleSetFile());
				reasoner.addFactsFromDataSource(referenceEDB, referenceEDBPath);				
			}
		}
	}
	
	protected void prepareQueries(List<Rule> rules) throws IOException, PrepareQueriesException {
		try {
			reasoner.addRules(rules);
		} catch (ReasonerStateException e) {
			logger.error("Trying to add rules in the wrong state for propery " + property + ".", e);
			throw new PrepareQueriesException("INTERNAL ERROR for property " + property + ".");
		}
		
		try {
			reasoner.load();
		} catch (EdbIdbSeparationException e) {
			logger.error("EDB rule occured in IDB for property " + property + ".", e);
			throw new PrepareQueriesException("INTERNAL ERROR for property " + property + ".");
		} catch (IncompatiblePredicateArityException e) {
			logger.error("Predicate does not match the datasource for property " + property + ".", e);
			throw new PrepareQueriesException("INTERNAL ERROR for property " + property + ".");
		}
		
		try {
			reasoner.reason();
		} catch (ReasonerStateException e) {
			logger.error("Trying to reason in the wrong state for property " + property + ".", e);
			throw new PrepareQueriesException("INTERNAL ERROR for property " + property + ".");
		}
	}
	
	public abstract String violations() throws IOException;
	
	protected abstract Map<String, TripleSet> getRequiredTripleSets(String property) throws IOException;
	
	public void close() throws IOException {
		for (TripleSet tripleSet : tripleSets.values()) {
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
		for (TripleSet tripleSet : tripleSets.values()) {
			result += "  " + tripleSet + "\n";
		}
		return result;
	}

}
