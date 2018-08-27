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
	
	static Predicate statementEDB = Expressions.makePredicate("statementEDB", 4);
	static Predicate violation_triple = Expressions.makePredicate("violation_triple", 4);
	static Predicate unequal = Expressions.makePredicate("unequal", 2);
	
	static Variable s = Expressions.makeVariable("S");
	static Variable o = Expressions.makeVariable("O");
	static Variable i = Expressions.makeVariable("I");
	static Variable v = Expressions.makeVariable("V");
	static Variable w = Expressions.makeVariable("W");
	static Variable p = Expressions.makeVariable("P");
	
	static Constant id30 = Expressions.makeConstant("Q1$12345678-0030-abcd-efgh-ijklmnopqrst");
	static Constant id31 = Expressions.makeConstant("Q1$12345678-0031-abcd-efgh-ijklmnopqrst");
	static Constant Q1 = Expressions.makeConstant("http://www.wikidata.org/entity/Q1");
	static Constant Q2 = Expressions.makeConstant("http://www.wikidata.org/entity/Q2"); 
	static Constant P209 = Expressions.makeConstant("http://www.wikidata.org/entity/P209");
	static Constant P518 = Expressions.makeConstant("http://www.wikidata.org/entity/P518");
	
	public static void main(String[] args) throws Exception {
		
		Reasoner reasoner = Reasoner.getInstance();
		
		Atom triple1 = Expressions.makeAtom(statementEDB, id30, Q1, P209, Q1);
		Atom triple2 = Expressions.makeAtom(statementEDB, id31, Q1, P209, Q2);
		Atom unequal1 = Expressions.makeAtom(unequal, id30, id31);
		
		reasoner.addFacts(triple1, triple2, unequal1);
		
		// violation_triple(?s, ?i, http://www.wikidata.org/entity/P209, ?v)
		Atom violation_tripleSIpV = Expressions.makeAtom(violation_triple, s, i, P209, v);
		
		// statementEDB(?s, ?i, http://www.wikidata.org/entity/P209, ?v)
		Atom statementEDB_SIpV = Expressions.makeAtom(statementEDB, s, i, P209, v);
		
		// statementEBD(?o, ?i, http://www.wikidata.org/entity/P209, ?w)
		Atom statementEDB_OIpW = Expressions.makeAtom(statementEDB, o, i, P209, w);
		
		// unequal(?s, ?o)
		Atom unequal_SO = Expressions.makeAtom(unequal, s, o);
		
		// violation_triple(?s, ?i, http://www.wikidata.org/entity/P209, ?v) :-
		//	tripleEBD(?s, ?i, http://www.wikidata.org/entity/P209, ?v),
		//	tripleEBD(?o, ?i, http://www.wikidata.org/entity/P209, ?w),
		//	unequal(?s, ?o)
		Rule violation = Expressions.makeRule(violation_tripleSIpV, statementEDB_SIpV, statementEDB_OIpW, unequal_SO);
		
		reasoner.addRules(violation);
		
		reasoner.load();
		
		reasoner.reason();
		
		queries(reasoner, Expressions.makeAtom(statementEDB, s, i, p, v), Expressions.makeAtom(unequal, s, i), Expressions.makeAtom(violation_triple, s, i, p, v));
	}
	
	static void queries(Reasoner reasoner, Atom...queries) throws ReasonerStateException {
		for (int i = 0; i < queries.length; i++) {
			Atom query = queries[i];
			System.out.println(query);
			QueryResultIterator iterator = reasoner.answerQuery(query, true);
			while (iterator.hasNext()) {
				QueryResult queryResult = iterator.next();
				String triple = "";
				for (Term term : queryResult.getTerms()) {
					triple += term.getName() + "\t";
				}
				
				System.out.println("\t" + triple.substring(0, triple.length() - 1));
			}
		}
	}

}