package experiment;

import java.io.IOException;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Algorithm;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.QueryResultIterator;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws ReasonerStateException, EdbIdbSeparationException, IncompatiblePredicateArityException, IOException
    {
    	// https://www.wikidata.org/wiki/Help:Property_constraints_portal/Value_type
    	final Predicate triple = Expressions.makePredicate("triple", 3);
    	final Constant subclass_of_DB = Expressions.makeConstant("subclass_of_DB");
    	final Constant instance_of_DB = Expressions.makeConstant("instance_of_DB");
    	
    	final Constant subject = Expressions.makeConstant("subject");
    	final Constant predicate = Expressions.makeConstant("predicate");
    	final Constant object = Expressions.makeConstant("object");
    	
    	final Constant highestclass = Expressions.makeConstant("highestclass");
    	final Constant superclass = Expressions.makeConstant("superclass");
    	final Constant subclass = Expressions.makeConstant("subclass");
    	
    	final Atom atom1 = Expressions.makeAtom(triple, superclass, subclass_of_DB, highestclass);
    	final Atom atom2 = Expressions.makeAtom(triple, subclass, subclass_of_DB, superclass);
    	final Atom atom3 = Expressions.makeAtom(triple, object, instance_of_DB, subclass);
    	final Atom atom4 = Expressions.makeAtom(triple, subject, predicate, object);
    	
    	final Variable x = Expressions.makeVariable("x");
    	final Variable y = Expressions.makeVariable("y");
    	final Variable z = Expressions.makeVariable("z");
    	
    	final Predicate subclass_of = Expressions.makePredicate("subclass_of", 2);
    	
    	// subclass_of(X, Y) :- triple(X, subclass_of_DB, Y)
    	final Atom subclass_of_XY = Expressions.makeAtom(subclass_of, x, y);
    	final Atom subclass_of_DB_XY = Expressions.makeAtom(triple, x, subclass_of_DB, y);
    	
    	final Rule rule1 = Expressions.makeRule(subclass_of_XY, subclass_of_DB_XY);
    	
    	// subclass_of(X, Y) :- subclass_of_DB(Z, Y), subclass_of_DB(X, Z)
    	final Atom subclass_of_DB_XZ = Expressions.makeAtom(subclass_of, x, z);
    	final Atom subclass_of_DB_ZY = Expressions.makeAtom(subclass_of, z, y);
    	
    	final Rule rule2 = Expressions.makeRule(Expressions.makeConjunction(subclass_of_XY), Expressions.makeConjunction(subclass_of_DB_XZ, subclass_of_DB_ZY));
    	
    	final Predicate subclass_or_instance_of = Expressions.makePredicate("subclass_or_instance_of", 2);
    	
    	// subclass_or_instance_of(X, Y) :- subclass_of(X, Y)
    	final Atom subclass_or_instance_of_XY = Expressions.makeAtom(subclass_or_instance_of, x, y);
    	
    	final Rule rule3 = Expressions.makeRule(subclass_or_instance_of_XY, subclass_of_XY);
    	
    	// subclass_or_instance_of(X, Y) :- triple(X, instance_of_DB, Y)
    	final Atom instance_of_DB_XY = Expressions.makeAtom(triple, x, instance_of_DB, y);
    	
    	final Rule rule4 = Expressions.makeRule(subclass_or_instance_of_XY, instance_of_DB_XY);
    	
    	// subclass_or_instance_of(X, Y) :- triple(X, instance_of_DB, Z), subclass_of(Z, Y)
    	final Atom instance_of_DB_XZ = Expressions.makeAtom(triple, x, instance_of_DB, z);
    	final Atom subclass_of_ZY = Expressions.makeAtom(subclass_of, z, y);
    	
    	final Rule rule5 = Expressions.makeRule(Expressions.makeConjunction(subclass_or_instance_of_XY), Expressions.makeConjunction(instance_of_DB_XZ, subclass_of_ZY));    	    	
    	
    	final Reasoner reasoner = Reasoner.getInstance();
    	
    	reasoner.addRules(rule1, rule2, rule3, rule4, rule5);
    	
    	reasoner.addFacts(atom1, atom2, atom3, atom4);
    	
    	reasoner.load();
    	
    	reasoner.reason();
    	
    	try (QueryResultIterator iterator = reasoner.answerQuery(subclass_or_instance_of_XY, true)) {
    		while (iterator.hasNext()) {
    			System.out.println(iterator.next());
    		}
    	}    	
    }
}
