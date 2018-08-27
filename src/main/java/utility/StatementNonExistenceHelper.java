package utility;

import static utility.SC.c;
import static utility.SC.first;
import static utility.SC.first_qualifier;
import static utility.SC.i;
import static utility.SC.next;
import static utility.SC.next_qualifier;
import static utility.SC.o;
import static utility.SC.p;
import static utility.SC.require;
import static utility.SC.require_qualifier;
import static utility.SC.s;
import static utility.SC.v;
import static utility.SC.x;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;


public class StatementNonExistenceHelper {	
	
	public static List<Rule> initRequireStatement(Term requiringTerm, Term requiredTerm, Atom...conjunctionAtoms) {
		return initRequireStatement(requiringTerm, requiredTerm, Arrays.asList(conjunctionAtoms));
	}
	
	public static List<Rule> initRequireStatement(Term requiringTerm, Term requiredTerm, List<Atom> conjunctionAtoms) {
		// require(S, requiringTerm, requiredTerm)
		Atom require_Srr = Expressions.makeAtom(require, s, requiringTerm, requiredTerm);
		
		// first(S, I)
		Atom first_SI = Expressions.makeAtom(first, s, i);
		
		List<Atom> firstConjunctionAtoms = new ArrayList<Atom>();
		
		firstConjunctionAtoms.addAll(conjunctionAtoms);
		firstConjunctionAtoms.add(first_SI);
		
		// require(S, requiringTerm, requiredTerm) :- first(S, I), conjunctionAtoms
		Rule firstRequire = Expressions.makeRule(require_Srr, toArray(firstConjunctionAtoms));
		
		// next(O, S)
		Atom next_OS = Expressions.makeAtom(next, o, s);
		
		// require(O, requiringTerm, requiredTerm)
		Atom require_Orr = Expressions.makeAtom(require, o, requiringTerm, requiredTerm);
		
		List<Atom> nextConjunctionAtoms = new ArrayList<Atom>();
		nextConjunctionAtoms.addAll(conjunctionAtoms);
		nextConjunctionAtoms.add(next_OS);
		nextConjunctionAtoms.add(require_Orr);
		
		// require(S, requiringTerm, requiredTerm) :- next(O, S), require(O, requiringTerm, requiredTerm), conjunctionAtoms
		Rule nextRequire = Expressions.makeRule(require_Srr, toArray(nextConjunctionAtoms));
		
		return Arrays.asList(firstRequire, nextRequire);
	}
	
	public static List<Rule> initRequireQualifier(Term requiringTerm, Term requiredTerm, Atom...conjunctionAtoms) {
		return initRequireQualifier(requiringTerm, requiredTerm, Arrays.asList(conjunctionAtoms));
	}
	
	public static List<Rule> initRequireQualifier(Term requiringTerm, Term requiredTerm, List<Atom> conjunctionAtoms) {
		
		// require_qualifier(S, P, V, requiringTerm, requiredTerm)
		Atom require_qualifier_SPVrr = Expressions.makeAtom(require_qualifier, s, p, v, requiringTerm, requiredTerm);
		
		// first_qualifier(S, P, V)
		Atom first_qualifier_SPV = Expressions.makeAtom(first_qualifier, s, p, v);
		
		List<Atom> firstConjunctionAtoms = new ArrayList<Atom>();
		firstConjunctionAtoms.add(first_qualifier_SPV);
		firstConjunctionAtoms.addAll(conjunctionAtoms);
		
		// require_qualifier(S, P, V, requiringTerm, requiredTerm) :- first_qualifier(S, P, V), conjunctionAtoms
		Rule firstRequire = Expressions.makeRule(require_qualifier_SPVrr, toArray(firstConjunctionAtoms));

		// next_qualifier(O, X, C, S, P, V)
		Atom next_qualifier_OXCSPV = Expressions.makeAtom(next_qualifier, o, x, c, s, p, v);
		
		// require_qualifier(O, X, C, requiringTerm, requiredTerm)
		Atom require_qualifier_OXCr = Expressions.makeAtom(require_qualifier, o, x, c, requiringTerm, requiredTerm);
		
		List<Atom> nextConjunctionAtoms = new ArrayList<Atom>();
		nextConjunctionAtoms.add(next_qualifier_OXCSPV);
		nextConjunctionAtoms.add(require_qualifier_OXCr);
		nextConjunctionAtoms.addAll(conjunctionAtoms);
		
		// require_qualifier(S, P, V, requiringTerm, requiredTerm) :- next_qualifier(O, X, C, S, P, V), require_qualifier(O, X, C, requiredTerm), conjunctionAtoms
		Rule nextRequire = Expressions.makeRule(require_qualifier_SPVrr, toArray(nextConjunctionAtoms));
		
		return Arrays.asList(firstRequire, nextRequire);
	}
	
	static Atom[] toArray(List<Atom> list) {
		return list.toArray(new Atom[list.size()]);
	}

}
