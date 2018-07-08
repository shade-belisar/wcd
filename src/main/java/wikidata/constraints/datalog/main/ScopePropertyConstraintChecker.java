/**
 * 
 */
package wikidata.constraints.datalog.main;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Algorithm;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
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
	
	final Predicate tripleEDB = Expressions.makePredicate(TRIPLE, 4);
	final Predicate qualifierEDB = Expressions.makePredicate(QUALIFIER, 3);
	final Predicate referenceEDB = Expressions.makePredicate(REFERENCE, 3);
	
	final String TRIPLE_SET = "triple_set";

	public ScopePropertyConstraintChecker(String constraint_, Map<String, String> qualifiers_) throws IOException {
		super(constraint_, qualifiers_);
	}
	
	public String violations() throws ReasonerStateException, IOException {
		final Reasoner reasoner = Reasoner.getInstance();
		reasoner.setAlgorithm(Algorithm.RESTRICTED_CHASE);
		
		final DataSource tripleEDBPath = new CsvFileDataSource(tripleSets.get(TRIPLE_SET).getTripleSetFile());
		final DataSource qualifierEDBPath = new CsvFileDataSource(tripleSets.get(TRIPLE_SET).getQualifierTripleSetFile());
		final DataSource referenceEDBPath = new CsvFileDataSource(tripleSets.get(TRIPLE_SET).getReferenceTripleSetFile());
		
		reasoner.addFactsFromDataSource(tripleEDB, tripleEDBPath);
		reasoner.addFactsFromDataSource(qualifierEDB, qualifierEDBPath);
		reasoner.addFactsFromDataSource(referenceEDB, referenceEDBPath);
		
		switch (qualifiers.get(ScopeConstraintChecker.SCOPE)) {
		case ScopeConstraintChecker.AS_MAIN_VALUE: 
			// violation(STATEMENTID) :- 
			break;
		case ScopeConstraintChecker.AS_QUALIFIER:
			break;
		case ScopeConstraintChecker.AS_REFERENCE:
			break;
		default:
			logger.error(qualifiers.get(ScopeConstraintChecker.SCOPE) + " is not among as main value, as qualifier or as reference.");
			return "";
		}
		
		Atom query;

		String result = "";
/*    	try (QueryResultIterator iterator = reasoner.answerQuery(query, true)) {
    		while (iterator.hasNext()) {
    			result += iterator.next() + "\n";
    		}
    	}*/
    	return result;
		// subclass_of(X, Y) :- subclass_of_DB(Z, Y), subclass_of_DB(X, Z)
	}

	@Override
	Map<String, TripleSet> getRequiredTripleSets(String property, Map<String, String> qualifiers) throws IOException {
		Map<String, TripleSet> result = new HashMap<String, TripleSet>();
		result.put(TRIPLE_SET, new ScopeTripleSet(property, qualifiers));
		return result;
	}

}
