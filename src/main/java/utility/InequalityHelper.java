package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
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
	
	public static void establishInequality(File inequalityFile, int inequalityIndex) throws ReasonerStateException, IOException {
		establishInequality(inequalityFile, inequalityIndex, new HashSet<String>());
	}
	
	public static void establishInequality(File inequalityFile1, int inequalityIndex1, File inequalityFile2, int inequalityIndex2) throws ReasonerStateException, IOException {
		establishInequality(inequalityFile1, inequalityIndex1, inequalityFile2, inequalityIndex2, new HashSet<String>());
	}
	
	public static void establishInequality(File inequalityFile, int inequalityIndex, Set<String> additionalValues) throws ReasonerStateException, IOException {
		establishInequality(inequalityFile, inequalityIndex, null, 0, new HashSet<String>());
	}
	
	static void establishInequality(File inequalityFile1, int inequalityIndex1, File inequalityFile2, int inequalityIndex2, Set<String> additionalValues) throws ReasonerStateException, IOException {
		reasoner.addRules(unequal_IDB_EDB, inverse);
		
		switch (mode) {
		case NAIVE:
			naive(inequalityFile1, inequalityIndex1, inequalityFile2, inequalityIndex2, additionalValues);
			break;
		case ENCODED:
			encoded(inequalityFile1, inequalityIndex1, inequalityFile2, inequalityIndex2, additionalValues, false);
			break;
		case DEMANDED:
			encoded(inequalityFile1, inequalityIndex1, inequalityFile2, inequalityIndex2, additionalValues, true);
			break;
		}
	}

	static Iterator<CSVRecord> iteratorFromFile(File file) throws IOException {
		if (file == null)
			return null;
		return CSVFormat.DEFAULT.parse(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))).iterator();
	}

	static void naive (File inequalityFile1, int inequalityIndex1, File inequalityFile2, int inequalityIndex2, Set<String> additionalValues) throws ReasonerStateException, IOException {
		List<String> fixedOrderValues = new ArrayList<String>(additionalValues);
		
		Iterator<String> firstIterator = new CombinedIterator(iteratorFromFile(inequalityFile1), inequalityIndex1, iteratorFromFile(inequalityFile2), inequalityIndex2, fixedOrderValues.iterator());
		
		if (additionalValues.contains("http://www.wikidata.org/entity/Q7269"))
			System.out.println("Contains");
		
		int first = 0;
		while(firstIterator.hasNext()) {
			String firstEntry = firstIterator.next();
			
			Iterator<String> secondIterator = new CombinedIterator(iteratorFromFile(inequalityFile1), inequalityIndex1, iteratorFromFile(inequalityFile2), inequalityIndex2, fixedOrderValues.iterator());
			
			for (int second = 0; second <= first; second++) {
				if (secondIterator.hasNext())
					secondIterator.next();
			}
			
			while (secondIterator.hasNext()) {
				String secondEntry = secondIterator.next();
				if (secondEntry.equals(firstEntry))
					continue;
				
				Constant firstConstant = Expressions.makeConstant(firstEntry);
				Constant secondConstant = Expressions.makeConstant(secondEntry);
				
				reasoner.addFacts(Expressions.makeAtom(unequal_EDB, firstConstant, secondConstant));
			}
			
			first++;
		}
	}

	static void encoded(File inequalityFile1, int inequalityIndex1, File inequalityFile2, int inequalityIndex2, Set<String> additionalValues, boolean demand) throws ReasonerStateException, IOException {
		Iterator<String> iterator = new CombinedIterator(iteratorFromFile(inequalityFile1), inequalityIndex1, iteratorFromFile(inequalityFile2), inequalityIndex2, additionalValues.iterator());
		
		int maxLength = 0;
		while(iterator.hasNext()) {
			int length = iterator.next().length();
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
		
		iterator = new CombinedIterator(iteratorFromFile(inequalityFile1), inequalityIndex1, iteratorFromFile(inequalityFile2), inequalityIndex2, additionalValues.iterator());
		
		while (iterator.hasNext()) {
			String string = iterator.next();
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
