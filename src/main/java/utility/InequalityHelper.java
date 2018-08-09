package utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import static utility.SC.require_inequality;

public class InequalityHelper {
	
	public enum Mode {
		NAIVE,
		ENCODED,
		DEMANDED
	}
	
	final static String NONE = "none";
	
	final static String X = "x";
	final static String Y = "y";
	
	final static String CON1 = "con1";
	final static String CON2 = "con2";
	
	final static String UNEQUAL_EDB = "unequal_EDB";
	final static String UNEQUAL = "unequal";
	final static List<String> ITH_LETTER = new ArrayList<String>();
	
	final static Variable x = Expressions.makeVariable(X);
	final static Variable y = Expressions.makeVariable(Y);
	
	final static Variable con1 = Expressions.makeVariable(CON1);
	final static Variable con2 = Expressions.makeVariable(CON2);
	
	final static Predicate unequal_EDB = Expressions.makePredicate(UNEQUAL_EDB, 2);
	public final static Predicate unequal = Expressions.makePredicate(UNEQUAL, 2);
	final static List<Predicate> ith_letter = new ArrayList<Predicate>();
	
	// unequal(CON1, CON2)
	final static Atom unequal_CC = Expressions.makeAtom(unequal, con1, con2);
	
	// require_inequality(CON1, CON2)
	final static Atom require_inequality_CC = Expressions.makeAtom(require_inequality, con1, con2);
	
	// unequal(X, Y)
	final static Atom unequal_XY = Expressions.makeAtom(unequal, x, y);
	
	// unequal_IDB(X, Y) :- unequal_EDB(X, Y)
	final static Rule unequal_IDB_EDB = Expressions.makeRule(Expressions.makeAtom(unequal, x, y), Expressions.makeAtom(unequal_EDB, x, y));
			
	// unequal_IDB(X, Y) :- unequal_IDB(Y, X)
	final static Rule inverse = Expressions.makeRule(Expressions.makeAtom(unequal, x, y), Expressions.makeAtom(unequal, y, x));
	
	static Reasoner reasoner;
	
	public static Mode mode = Mode.ENCODED;
	
	public static void setOrReset(Reasoner reasoner_) {
		reasoner = reasoner_;
		ITH_LETTER.clear();
		ith_letter.clear();
	}
	
	public static void establishInequality(String...unequalConstants) throws ReasonerStateException {
		Set<String> unequalConstantsSet = new HashSet<String>();
		for (int i = 0; i < unequalConstants.length; i++) {
			unequalConstantsSet.add(unequalConstants[i]);
		}
		establishInequality(unequalConstantsSet);
	}
	
	public static void establishInequality(Set<String> unequalConstants) throws ReasonerStateException {
		reasoner.addRules(unequal_IDB_EDB, inverse);
		
		switch (mode) {
		case NAIVE:
			naive(unequalConstants);
			break;
		case ENCODED:
			encoded(unequalConstants, false);
			break;
		case DEMANDED:
			encoded(unequalConstants, true);
			break;
		}
	}
	
	static void naive (Set<String> unequalConstants) throws ReasonerStateException {
		List<Constant> unequals = new ArrayList<Constant>();
		for (String string : unequalConstants) {
			unequals.add(Expressions.makeConstant(string));
		}
		for (int first = 0; first < unequals.size(); first++) {
			Constant firstConstant = unequals.get(first);
			for (int second = first + 1; second < unequals.size(); second++) {
				Constant secondConstant = unequals.get(second);
				
				reasoner.addFacts(Expressions.makeAtom(unequal_EDB, firstConstant, secondConstant));
			}
		}
	}
	
	static void encoded(Set<String> unequalConstants, boolean demand) throws ReasonerStateException {
		int maxLength = 0;
		for (String	string : unequalConstants) {
			int length = string.length();
			if (length > maxLength)
				maxLength = length;
		}
		
		for (int i = ITH_LETTER.size(); i < maxLength; i++) {
			
			ITH_LETTER.add("letter" + i);
			ith_letter.add(Expressions.makePredicate(ITH_LETTER.get(i), 2));
			
			Predicate letteri = ith_letter.get(i);
			
			// letteri(CON1, X)
			Atom letteri_CX = Expressions.makeAtom(letteri, con1, x);
			
			// letteri(CON2, Y)
			Atom letteri_CY = Expressions.makeAtom(letteri, con2, y);

			Rule unequal;
			if (demand)
				// unequal(CON1, CON2) :- require_inequality(CON1, CON2), letteri(CON1, X), letteri(CON2, Y), unequal(X, Y)
				unequal = Expressions.makeRule(unequal_CC, require_inequality_CC, letteri_CX, letteri_CY, unequal_XY);
			else
				// unequal(CON1, CON2) :- letteri(CON1, X), letteri(CON2, Y), unequal(X, Y)
				unequal = Expressions.makeRule(unequal_CC, letteri_CX, letteri_CY, unequal_XY);
			
			reasoner.addRules(unequal);
		}
		
		List<Atom> letters = new ArrayList<Atom>();
		
		Set<String> characters = new HashSet<String>();
		
		for (String string : unequalConstants) {
			Constant constant = Expressions.makeConstant(string);
			for (int i = 0; i < maxLength; i++) {
				String character;
				if (i < string.length()) {
					character = string.substring(i, i+1);
				} else {
					character = NONE;
				}
				
				characters.add(character);
				Constant characterConstant = Expressions.makeConstant(character);
				Atom toAdd = Expressions.makeAtom(ith_letter.get(i), constant, characterConstant);
				letters.add(toAdd);
			}
		}
		
		reasoner.addFacts(allCharactersUnequal(characters));
		
		reasoner.addFacts(letters);
	}
	
	static List<Atom> allCharactersUnequal(Set<String> characters) {
		List<Atom> atoms = new ArrayList<Atom>();
		
		List<Constant> indentifierParts = new ArrayList<Constant>();
		for (String allowedCharacter : characters) {
			indentifierParts.add(Expressions.makeConstant(allowedCharacter));
		}
		
		for (int first = 0; first < indentifierParts.size(); first++) {
			Constant firstConstant = indentifierParts.get(first);
			for (int second = first + 1; second < indentifierParts.size(); second++) {
				Constant secondConstant = indentifierParts.get(second);
				
				atoms.add(Expressions.makeAtom(unequal_EDB, firstConstant, secondConstant));
			}
		}
		
		return atoms;
	}
	
	
}
