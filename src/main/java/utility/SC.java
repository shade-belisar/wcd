package utility;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

public class SC {
	
	public final static String TRIPLE = "tripleEDB";
	public final static String QUALIFIER = "qualifierEDB";
	public final static String REFERENCE = "referenceEDB";
	
	public final static String VIOLATION_TRIPLE = "violation_triple";
	public final static String VIOLATION_QUALIFIER = "violation_qualifier";
	public final static String VIOLATION_REFERENCE = "violation_reference";
	
	public final static String REQUIRE_INEQUALITY = "require_inequality";
	
	public final static String S = "s";
	public final static String O = "o";
	public final static String I = "i";
	public final static String X = "x";
	public final static String P = "p";
	public final static String Q = "q";
	public final static String V = "v";
	public final static String C = "c";
	public final static String U = "u";
	public final static String W = "w";
	public final static String R = "r";
	
	public static final String REQUIRE = "require";
	public static final String REQUIRE_SECOND = "require_second";
	
	public static final String FIRST = "first";
	public static final String NEXT = "next";
	public static final String LAST = "last";
	
	public static final String REQUIRE_QUALIFIER = "require_qualifier";
	
	public static final String FIRST_QUALIFIER = "first_qualifier";
	public static final String NEXT_QUALIFIER = "next_qualifier";
	public static final String LAST_QUALIFIER = "last_qualifier";
	
	public final static String ITEM = "item";
	public final static String PROPERTY = "property";
	
	public final static String UNIT = "unit";
	
	public final static String DOES_NOT_HAVE = "does_not_have";
	
	public final static Predicate tripleEDB = Expressions.makePredicate(TRIPLE, 4);
	public final static Predicate qualifierEDB = Expressions.makePredicate(QUALIFIER, 3);
	public final static Predicate referenceEDB = Expressions.makePredicate(REFERENCE, 3);
	
	public final static Predicate violation_triple = Expressions.makePredicate(VIOLATION_TRIPLE, 4);
	public final static Predicate violation_qualifier = Expressions.makePredicate(VIOLATION_QUALIFIER, 3);
	public final static Predicate violation_reference = Expressions.makePredicate(VIOLATION_REFERENCE, 3);
	
	public final static Predicate require_inequality = Expressions.makePredicate(REQUIRE_INEQUALITY, 2);
	
	public final static Variable s = Expressions.makeVariable(S);
	public final static Variable o = Expressions.makeVariable(O);
	public final static Variable i = Expressions.makeVariable(I);
	public final static Variable x = Expressions.makeVariable(X);
	public final static Variable p = Expressions.makeVariable(P);
	public final static Variable q = Expressions.makeVariable(Q);
	public final static Variable v = Expressions.makeVariable(V);
	public final static Variable c = Expressions.makeVariable(C);
	public final static Variable u = Expressions.makeVariable(U);
	public final static Variable w = Expressions.makeVariable(W);
	public final static Variable r = Expressions.makeVariable(R);
	
	public final static Atom violation_triple_query = Expressions.makeAtom(violation_triple, s, i, p, v);
	public final static Atom violation_qualifier_query = Expressions.makeAtom(violation_qualifier, s, p, v);
	public final static Atom violation_reference_query = Expressions.makeAtom(violation_reference, s, p, v);
	
	public static final Predicate require = Expressions.makePredicate(REQUIRE, 2);
	public static final Predicate require_second = Expressions.makePredicate(REQUIRE_SECOND, 2);
	
	public static final Predicate first = Expressions.makePredicate(FIRST, 2);
	public static final Predicate next = Expressions.makePredicate(NEXT, 2);
	public static final Predicate last = Expressions.makePredicate(LAST, 2);
	
	public static final Predicate require_qualifier = Expressions.makePredicate(REQUIRE_QUALIFIER, 4);
	
	public static final Predicate first_qualifier = Expressions.makePredicate(FIRST_QUALIFIER, 3);
	public static final Predicate next_qualifier = Expressions.makePredicate(NEXT_QUALIFIER, 6);
	public static final Predicate last_qualifier = Expressions.makePredicate(LAST_QUALIFIER, 3);
	
	public final static Predicate item = Expressions.makePredicate(ITEM, 1);
	public final static Predicate property = Expressions.makePredicate(PROPERTY, 1);
	
	public final static Predicate unit = Expressions.makePredicate(UNIT, 2);
	
	public final static Predicate does_not_have = Expressions.makePredicate(DOES_NOT_HAVE, 2);
}
