package experiment;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.QueryResult;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.QueryResultIterator;


public class MinimalExample {
	
	static Variable s = Expressions.makeVariable("s");
	static Variable i = Expressions.makeVariable("i");
	static Variable p = Expressions.makeVariable("p");
	static Variable v = Expressions.makeVariable("v");
	
	static Constant id = Expressions.makeConstant("id");
	static Constant subject = Expressions.makeConstant("subject");
	static Constant predicate = Expressions.makeConstant("predicate");
	static Constant object = Expressions.makeConstant("object");
	
	static Constant required_property = Expressions.makeConstant("required_property");
	
	static Predicate tripleEDB = Expressions.makePredicate("tripleEDB", 4); 
	static Predicate first = Expressions.makePredicate("first", 2);
	static Predicate last = Expressions.makePredicate("last", 2);
	static Predicate unequalEDB = Expressions.makePredicate("unequalEDB", 2);
	static Predicate require = Expressions.makePredicate("require", 2);

	public static void main(String[] args) throws Exception {
		
		Reasoner reasoner = Reasoner.getInstance();
		
		// loading database
		Atom triplEDB_ISPO = Expressions.makeAtom(tripleEDB, id, subject, predicate, object);
		
		Atom first_IS = Expressions.makeAtom(first, id, subject);
		
		Atom last_IS = Expressions.makeAtom(last, id, subject);
		
		reasoner.addFacts(triplEDB_ISPO, first_IS, last_IS);

		// establishing inequality by hand
		Atom unequal_RP = Expressions.makeAtom(unequalEDB, required_property, predicate);
		Atom unequal_PR = Expressions.makeAtom(unequalEDB, predicate, required_property);
		
		reasoner.addFacts(unequal_PR, unequal_RP);
		
		// require(S, requiredTerm)
		Atom require_Sr = Expressions.makeAtom(require, s, required_property);
		
		// first(S, I)
		Atom first_SI = Expressions.makeAtom(first, s, i);
		
		// tripleEDB(S, I, P, V)
		Atom tripleEDB_SIPV = Expressions.makeAtom(tripleEDB, s, i, p, v);
		
		// unequal(requiredTerm, P)
		Atom unequal_rP = Expressions.makeAtom(unequalEDB, required_property, p);
		
		// This variant crashes
		// require(S, requiredTerm) :- first(S, I), unequal(requiredTerm, P)
		Rule firstRequire = Expressions.makeRule(require_Sr, first_SI, unequal_rP);
		
		// This variant does not result in any derivations but should, as far as I can see
		// require(S, requiredTerm) :- first(S, I), tripleEDB(S, I, P, V), unequal(requiredTerm, P)
		//Rule firstRequire = Expressions.makeRule(require_Sr, first_SI, tripleEDB_SIPV unequal_rP);
		
		reasoner.addRules(firstRequire);
		
		reasoner.load();
		
		reasoner.reason();
		
		queries(reasoner, Expressions.makeAtom(require, s, i));
	}
	
	static void queries(Reasoner reasoner, Atom...queries) throws ReasonerStateException {
		for (int i = 0; i < queries.length; i++) {
			Atom query = queries[i];
			QueryResultIterator iterator = reasoner.answerQuery(query, true);
			while (iterator.hasNext()) {
				QueryResult queryResult = iterator.next();
				String triple = "";
				for (Term term : queryResult.getTerms()) {
					triple += term.getName() + "\t";
				}
				
				System.out.println(triple.substring(0, triple.length() - 1));
			}
		}
	}

}
