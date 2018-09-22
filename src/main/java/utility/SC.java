package utility;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import positionVLog4J.PositionPredicate;

public class SC {
	
	public final static String H = "h";
	public final static String G = "G";
	public final static String S = "s";
	public final static String T = "t";
	public final static String O = "o";
	public final static String I = "i";
	public final static String X = "x";
	public final static String Y = "y";
	public final static String P = "p";
	public final static String Q = "q";
	public final static String V = "v";
	public final static String C = "c";
	public final static String U = "u";
	public final static String W = "w";
	public final static String R = "r";
	
	public final static String STATEMENT = "statementEDB";
	public final static String QUALIFIER = "qualifierEDB";
	public final static String REFERENCE = "referenceEDB";
	
	public final static String VIOLATION_STATEMENT = "violation_statement";
	public final static String VIOLATION_QUALIFIER = "violation_qualifier";
	public final static String VIOLATION_REFERENCE = "violation_reference";
	
	public final static String REQUIRE_INEQUALITY = "require_inequality";
	
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
	public final static String RANK = "rank";
	
	public final static String DOES_NOT_HAVE = "does_not_have";
	public final static String SAME_OR_NON_EXISTENT = "same_or_non_existent";
	public final static String POSSIBLE_VIOLATION = "possible_violation";
	
	public final static Variable h = Expressions.makeVariable(H);
	public final static Variable g = Expressions.makeVariable(G);
	public final static Variable s = Expressions.makeVariable(S);
	public final static Variable t = Expressions.makeVariable(T);
	public final static Variable o = Expressions.makeVariable(O);
	public final static Variable i = Expressions.makeVariable(I);
	public final static Variable x = Expressions.makeVariable(X);
	public final static Variable y = Expressions.makeVariable(Y);
	public final static Variable p = Expressions.makeVariable(P);
	public final static Variable q = Expressions.makeVariable(Q);
	public final static Variable v = Expressions.makeVariable(V);
	public final static Variable c = Expressions.makeVariable(C);
	public final static Variable u = Expressions.makeVariable(U);
	public final static Variable w = Expressions.makeVariable(W);
	public final static Variable r = Expressions.makeVariable(R);

	// id, entity, property, value -> property, entity, value, id
	public final static Predicate statementEDB = new PositionPredicate(STATEMENT, 4, 3, 1, 0, 2);
	// id, property, value -> property, value, id
	public final static Predicate qualifierEDB = new PositionPredicate(QUALIFIER, 3, 2, 0, 1);
	// id, hash, property, value -> property, value, id, hash
	public final static Predicate referenceEDB = new PositionPredicate(REFERENCE, 4, 2, 3, 0, 1);

	// id, entity, property, value
	public final static Predicate violation_statement = Expressions.makePredicate(VIOLATION_STATEMENT, 4);
	// id, property, value
	public final static Predicate violation_qualifier = Expressions.makePredicate(VIOLATION_QUALIFIER, 3);
	// id, hash, property, value
	public final static Predicate violation_reference = Expressions.makePredicate(VIOLATION_REFERENCE, 4);
	
	// inequality1, inequality2
	public final static Predicate require_inequality = Expressions.makePredicate(REQUIRE_INEQUALITY, 2);
	
	// id, uniqueId -> uniqueId, id
	public static final Predicate require = new PositionPredicate(REQUIRE, 2, 1, 0);
	// id, uniqueId -> uniqueId, id
	public static final Predicate require_second = new PositionPredicate(REQUIRE_SECOND, 2, 1, 0);
	
	// id, entity
	public static final Predicate first = Expressions.makePredicate(FIRST, 2);
	// id1, id2
	public static final Predicate next = Expressions.makePredicate(NEXT, 2);
	// id, entity
	public static final Predicate last = Expressions.makePredicate(LAST, 2);
	
	// id, property, value, uniqueId -> property, uniqueId, value, id
	public static final Predicate require_qualifier = new PositionPredicate(REQUIRE_QUALIFIER, 4, 3, 0, 2, 1);
	
	// id, property, value -> property, value, id
	public static final Predicate first_qualifier = new PositionPredicate(FIRST_QUALIFIER, 3, 2, 0, 1);
	// id1, property1, value1, id2, property2, value2 -> property1, property2, value1, value2, id1, id2
	public static final Predicate next_qualifier = new PositionPredicate(NEXT_QUALIFIER, 6, 4, 0, 2, 5, 1, 3);
	// id, property, value -> property, value, id
	public static final Predicate last_qualifier = new PositionPredicate(LAST_QUALIFIER, 3, 2, 0, 1);
	
	// entity
	public final static Predicate item = Expressions.makePredicate(ITEM, 1);
	// entity
	public final static Predicate property = Expressions.makePredicate(PROPERTY, 1);
	
	// value, unit -> unit, value
	public final static Predicate unit = new PositionPredicate(UNIT, 2, 1, 0);
	// id, rank -> rank, id
	public final static Predicate rank = new PositionPredicate(RANK, 2, 1, 0);
	
	// id, property -> property, id
	public final static Predicate does_not_have = new PositionPredicate(DOES_NOT_HAVE, 2, 1, 0);
	
	// id1, id2, qualifier -> qualifier, id1, id2
	public final static Predicate same_or_non_existent = new PositionPredicate(SAME_OR_NON_EXISTENT, 3, 1, 2, 0);
	
	// id1, id2
	public final static Predicate possible_violation = Expressions.makePredicate(POSSIBLE_VIOLATION, 2);
	
	public final static Atom violation_statement_query = Expressions.makeAtom(violation_statement, s, i, p, v);
	public final static Atom violation_qualifier_query = Expressions.makeAtom(violation_qualifier, s, p, v);
	public final static Atom violation_reference_query = Expressions.makeAtom(violation_reference, s, h, p, v);
}
