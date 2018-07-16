package wikidata.constraints.datalog.utility;

import java.util.ArrayList;
import java.util.Arrays;
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

public class InequalityHelper {
	
	final static Set<String> ALLOWED_CHARACTERS = new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "Q", "P"));
	
	final static String X = "x";
	final static String Y = "y";
	
	final static String CON1 = "con1";
	final static String CON2 = "con2";
	
	final static String UNEQUAL_EDB = "unequal_EDB";
	final static String UNEQUAL_IDB = "unequal_IDB";
	final static List<String> ITH_LETTER = new ArrayList<String>();
	
	final static Variable x = Expressions.makeVariable(X);
	final static Variable y = Expressions.makeVariable(Y);
	
	final static Variable con1 = Expressions.makeVariable(CON1);
	final static Variable con2 = Expressions.makeVariable(CON2);
	
	public final static Predicate unequal_EDB = Expressions.makePredicate(UNEQUAL_EDB, 2);
	public final static Predicate unequal_IDB = Expressions.makePredicate(UNEQUAL_IDB, 2);
	final static List<Predicate> ith_letter = new ArrayList<Predicate>();
	
	public static void addUnequalConstantsToReasoner(Reasoner reasoner, String...unequalConstants) throws CharacterNotAllowedException, ReasonerStateException {
		Set<String> unequalConstantsSet = new HashSet<String>();
		for (int i = 0; i < unequalConstants.length; i++) {
			unequalConstantsSet.add(unequalConstants[i]);
		}
		addUnequalConstantsToReasoner(reasoner, unequalConstantsSet);
	}
	
	public static void addUnequalConstantsToReasoner(Reasoner reasoner, Set<String> unequalConstants) throws CharacterNotAllowedException, ReasonerStateException {
		int maxLength = 0;
		for (String	string : unequalConstants) {
			int length = string.length();
			if (length > maxLength)
				maxLength = length;
		}
		
		for (int i = 0; i < maxLength; i++) {
			ITH_LETTER.add("letter" + i);
			ith_letter.add(Expressions.makePredicate(ITH_LETTER.get(i), 2));
		}
		
		List<Atom> letters = new ArrayList<Atom>();
		
		for (String string : unequalConstants) {
			Constant constant = Utility.makeConstant(string);
			for (int i = 0; i < string.length(); i++) {
				String character = string.substring(i, i+1);
				Constant characterConstant = Expressions.makeConstant(character);;
				if (!ALLOWED_CHARACTERS.contains(character))
					throw new CharacterNotAllowedException("Character " + character + " is not allowed.");
				Atom toAdd = Expressions.makeAtom(ith_letter.get(i), constant, characterConstant);
				letters.add(toAdd);
			}
		}
		
		reasoner.addFacts(allAllowedCharactersInequal());
		
		reasoner.addFacts(letters);
		
		// unequal_IDB(X, Y) :- unequal_EDB(X, Y)
		Rule unequal_IDB_EDB = Expressions.makeRule(Expressions.makeAtom(unequal_IDB, x, y), Expressions.makeAtom(unequal_EDB, x, y));
		
		// unequal_IDB(X, Y) :- unequal_IDB(Y, X)
		Rule inverse = Expressions.makeRule(Expressions.makeAtom(unequal_IDB, x, y), Expressions.makeAtom(unequal_IDB, y, x));
		
		reasoner.addRules(unequal_IDB_EDB, inverse);
		
		// unequal(CON1, CON2)
		Atom unequal_CC = Expressions.makeAtom(unequal_IDB, con1, con2);
		
		// unequal(X, Y)
		Atom unequal_XY = Expressions.makeAtom(unequal_IDB, x, y);
		
		for (int i = 0; i < maxLength; i++) {
			Predicate letteri = ith_letter.get(i);
			
			// letteri(CON1, X)
			Atom letteri_CX = Expressions.makeAtom(letteri, con1, x);
			
			// letteri(CON2, Y)
			Atom letteri_CY = Expressions.makeAtom(letteri, con2, y);
			
			// unequal(CON1, CON2) :- letteri(CON1, X), letteri(CON2, Y), unequal(X, Y)
			Rule unequal = Expressions.makeRule(unequal_CC, letteri_CX, letteri_CY, unequal_XY);
			
			reasoner.addRules(unequal);
		}		
	}
	
	static List<Atom> allAllowedCharactersInequal() {
		List<Atom> atoms = new ArrayList<Atom>();
		
		List<Constant> indentifierParts = new ArrayList<Constant>();
		for (String allowedCharacter : ALLOWED_CHARACTERS) {
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
