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

import wikidata.constraints.datalog.rdf.TripleSet;

/**
 * @author adrian
 *
 */
public abstract class PropertyConstraintChecker {
	
	final Variable x = Expressions.makeVariable("x");
	final Variable y = Expressions.makeVariable("y");
	final Variable z = Expressions.makeVariable("z");
	
	final static String TRIPLE = "triple";
	final static String QUALIFIER_TRIPLE = "qualifierTriple";
	final static String REFERENCE_TRIPLE = "referenceTriple";
	
	final Predicate tripleEDB = Expressions.makePredicate(TRIPLE, 4);
	final Predicate qualifierTripleEDB = Expressions.makePredicate(QUALIFIER_TRIPLE, 3);
	final Predicate referenceTripleEDB = Expressions.makePredicate(REFERENCE_TRIPLE, 3);
	
	String property;
	
	Map<String, String> qualifiers;
	
	Map<String, TripleSet> tripleSets = new HashMap<String, TripleSet>();
	
	public PropertyConstraintChecker(String property_, Map<String, String> qualifiers_) throws IOException {
		property = property_;
		qualifiers = qualifiers_;
		tripleSets = getRequiredTripleSets(property, qualifiers);
	}
	
	abstract Map<String, TripleSet> getRequiredTripleSets(String property, Map<String, String> qualifiers) throws IOException;
	
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
