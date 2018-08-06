package utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import static utility.SC.require;
import static utility.SC.require_qualifier;
import static utility.SC.first;
import static utility.SC.first_qualifier;
import static utility.SC.next;
import static utility.SC.next_qualifier;

import static utility.SC.s;
import static utility.SC.o;
import static utility.SC.i;
import static utility.SC.x;
import static utility.SC.p;
import static utility.SC.v;
import static utility.SC.c;


public class StatementNonExistenceHelper {
	
	static Reasoner reasoner;	
	
	public static void setOrReset(Reasoner reasoner_) {
		reasoner = reasoner_;
	}
	
	public static void initRequireTriple(Term requiredTerm, Atom...conjunctionAtoms) throws ReasonerStateException {
		initRequireTriple(requiredTerm, Arrays.asList(conjunctionAtoms));
	}
	
	public static void initRequireTriple(Term requiredTerm, List<Atom> conjunctionAtoms) throws ReasonerStateException {
		// require(S, requiredTerm)
		Atom require_Sr = Expressions.makeAtom(require, s, requiredTerm);
		
		// first(S, I)
		Atom first_SI = Expressions.makeAtom(first, s, i);
		
		List<Atom> firstConjunctionAtoms = new ArrayList<Atom>();
		
		firstConjunctionAtoms.add(first_SI);
		firstConjunctionAtoms.addAll(conjunctionAtoms);
		
		// require(S, requiredTerm) :- first(S, I), conjunctionAtoms
		Rule firstRequire = Expressions.makeRule(require_Sr, toArray(firstConjunctionAtoms));
		
		// require(O, requiredTerm)
		Atom require_Or = Expressions.makeAtom(require, o, requiredTerm);
		
		// next(S, O)
		Atom next_SO = Expressions.makeAtom(next, s, o);
		
		List<Atom> nextConjunctionAtoms = new ArrayList<Atom>();
		nextConjunctionAtoms.add(next_SO);
		nextConjunctionAtoms.add(require_Sr);
		nextConjunctionAtoms.addAll(conjunctionAtoms);
		
		// require(O, requiredTerm) :- next(S, O), require(S, requiredTerm), conjunctionAtoms
		Rule nextRequire = Expressions.makeRule(require_Or, toArray(nextConjunctionAtoms));
		
		reasoner.addRules(firstRequire, nextRequire);
	}
	
	public static void initRequireQualifier(Term requiredTerm, Atom...conjunctionAtoms) throws ReasonerStateException {
		initRequireQualifier(requiredTerm, Arrays.asList(conjunctionAtoms));
	}
	
	public static void initRequireQualifier(Term requiredTerm, List<Atom> conjunctionAtoms) throws ReasonerStateException {
		
		// require_qualifier(S, P, V, requiredTerm)
		Atom require_qualifier_SPVr = Expressions.makeAtom(require_qualifier, s, p, v, requiredTerm);
		
		// first_qualifier(S, P, V)
		Atom first_qualifier_SPV = Expressions.makeAtom(first_qualifier, s, p, v);
		
		List<Atom> firstConjunctionAtoms = new ArrayList<Atom>();
		firstConjunctionAtoms.add(first_qualifier_SPV);
		firstConjunctionAtoms.addAll(conjunctionAtoms);
		
		// require_qualifier(S, P, V, requiredTerm) :- first_qualifier(S, P, V), conjunctionAtoms
		Rule firstRequire = Expressions.makeRule(require_qualifier_SPVr, toArray(firstConjunctionAtoms));
		
		// require_qualifier(O, X, C, requiredTerm)
		Atom require_qualifier_OXCr = Expressions.makeAtom(require_qualifier, o, x, c, requiredTerm);
		
		// next_qualifier(S, P, V, O, X, C)
		Atom next_qualifier_SPVOXC = Expressions.makeAtom(next_qualifier, s, p, v, o, x, c);
		
		List<Atom> nextConjunctionAtoms = new ArrayList<Atom>();
		nextConjunctionAtoms.add(next_qualifier_SPVOXC);
		nextConjunctionAtoms.add(require_qualifier_SPVr);
		nextConjunctionAtoms.addAll(conjunctionAtoms);
		
		// require_qualifier(O, X, C, requiredTerm) :- next_qualifier(S, P, V, O, X, C), require_qualifier(S, P, V, requiredTerm), conjunctionAtoms
		Rule nextRequire = Expressions.makeRule(require_qualifier_OXCr, toArray(nextConjunctionAtoms));
		
		reasoner.addRules(firstRequire, nextRequire);
	}
	
	static Atom[] toArray(List<Atom> list) {
		return list.toArray(new Atom[list.size()]);
	}

}
