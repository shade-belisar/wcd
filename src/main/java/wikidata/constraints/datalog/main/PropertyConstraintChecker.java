/**
 * 
 */
package wikidata.constraints.datalog.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Algorithm;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;

import wikidata.constraints.datalog.rdf.TripleSet;

/**
 * @author adrian
 *
 */
public abstract class PropertyConstraintChecker {
	
	protected final static String TRIPLE = "triple";
	protected final static String QUALIFIER = "qualifier";
	protected final static String REFERENCE = "reference";
	
	protected final static String STATEMENT = "statement";
	protected final static String VIOLATION_SHORT = "violation_short";
	protected final static String VIOLATION_LONG = "violation_long";
	
	protected final static String X = "x";
	protected final static String Y = "y";
	protected final static String Z = "z";
	
	protected final Variable x = Expressions.makeVariable(X);
	protected final Variable y = Expressions.makeVariable(Y);
	protected final Variable z = Expressions.makeVariable(Z);
	
	protected final Predicate tripleEDB = Expressions.makePredicate(TRIPLE, 4);
	protected final Predicate qualifierEDB = Expressions.makePredicate(QUALIFIER, 3);
	protected final Predicate referenceEDB = Expressions.makePredicate(REFERENCE, 3);
	
	protected final Variable statement = Expressions.makeVariable(STATEMENT);
	protected final Predicate violation_short = Expressions.makePredicate(VIOLATION_SHORT, 3);
	protected final Predicate violation_long = Expressions.makePredicate(VIOLATION_LONG, 4);
	
	protected final Reasoner reasoner = Reasoner.getInstance();
	
	protected String property;
	
	protected Map<String, String> qualifiers;
	
	protected Map<String, TripleSet> tripleSets = new HashMap<String, TripleSet>();
	
	public PropertyConstraintChecker(String property_, Map<String, String> qualifiers_) throws IOException {
		property = property_;
		qualifiers = qualifiers_;
		tripleSets = getRequiredTripleSets(property, qualifiers);
		
		
		reasoner.setAlgorithm(Algorithm.RESTRICTED_CHASE);
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
	
	public abstract String violations() throws ReasonerStateException, IOException;
	
	protected abstract Map<String, TripleSet> getRequiredTripleSets(String property, Map<String, String> qualifiers) throws IOException;
	
	public void close() throws IOException {
		for (TripleSet tripleSet : tripleSets.values()) {
			tripleSet.close();
		}
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
