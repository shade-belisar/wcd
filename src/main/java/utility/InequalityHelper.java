package utility;

import java.io.File;
import java.io.FileInputStream;
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
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.TS.TripleSetFile;

import static utility.SC.require_inequality;

public class InequalityHelper {
	
	public enum Mode {
		NAIVE,
		ENCODED,
		DEMANDED
	}
	
	final static int chunkSize = 7;
	
	final static String NONE = "none";
	
	final static String X = "x";
	final static String Y = "y";
	
	final static String CON1 = "con1";
	final static String CON2 = "con2";
	
	final static String UNEQUAL_EDB = "unequal_EDB";
	final static String UNEQUAL = "unequal";
	
	final static Variable x = Expressions.makeVariable(X);
	final static Variable y = Expressions.makeVariable(Y);
	
	final static Predicate unequal_EDB = Expressions.makePredicate(UNEQUAL_EDB, 2);
	public final static Predicate unequal = Expressions.makePredicate(UNEQUAL, 2);
	
	final static List<TripleSetFile> files = new ArrayList<>();
	
	// require_inequality(X, Y)
	final static Atom require_inequality_XY = Expressions.makeAtom(require_inequality, x, y);
	
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
		files.clear();
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
	
	public static void load() throws ReasonerStateException, IOException {
		for (TripleSetFile tripleSetFile : files) {
			tripleSetFile.loadFile(reasoner);
		}
	}
	
	public static void delete() throws IOException {
		for (TripleSetFile tripleSetFile : files) {
			tripleSetFile.getFile().delete();
		}
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
		
		Set<String> characters = new HashSet<String>();
		characters.add(NONE);
		
		iterator = new CombinedIterator(iteratorFromFile(inequalityFile1), inequalityIndex1, iteratorFromFile(inequalityFile2), inequalityIndex2, additionalValues.iterator());
		
		while (iterator.hasNext()) {
			String string = iterator.next();
			for (int i = 0; i < string.length(); i++) {
				characters.add(string.substring(i, i+1));
			}
			int i = 0;
			for (List<String> chunk : chunks(string)) {
				TripleSetFile file = getChunkFile(i, demand);
				List<String> line = new ArrayList<>();
				line.add(string);
				line.addAll(chunk);
				file.write(line);
				i++;
			}
		}
		List<Atom> facts = allCharactersUnequal(characters);
		reasoner.addFacts(facts);
	}
	
	static TripleSetFile getChunkFile(int chunk, boolean demand) throws IOException, ReasonerStateException {
		if (chunk < files.size())
			return files.get(chunk);
	
		int chunkAdress = chunk * chunkSize;
		String LETTER_I = "letter" + chunkAdress;
		Predicate letter_i = Expressions.makePredicate(LETTER_I, chunkSize + 1);
		TripleSetFile file = new TripleSetFile(LETTER_I, letter_i);
		file.forceWrite();
		files.add(file);
		
		List<Atom> inequalities = new ArrayList<>();
		
		// letter_i(X, Ai, Ai+1, ..., Ai+14)
		List<Term> xVariables = new ArrayList<>();
		xVariables.add(x);
		// letter_i(Y, Bi, Bi+1, ..., Bi+14)
		List<Term> yVariables = new ArrayList<>();
		yVariables.add(y);
		for (int j = chunkAdress; j <= chunkAdress + chunkSize - 1; j++) {
			Variable ai = Expressions.makeVariable("a" + j);
			Variable bi = Expressions.makeVariable("b" + j);
			xVariables.add(ai);
			yVariables.add(bi);
			
			// unequal(Ai, Bi)
			Atom unequal_AB = Expressions.makeAtom(unequal, ai, bi);
			inequalities.add(unequal_AB);
		}
		
		Atom letter_i_XA = Expressions.makeAtom(letter_i, xVariables);
		Atom letter_i_YB = Expressions.makeAtom(letter_i, yVariables);
		
		for (Atom inequality : inequalities) {
			List<Atom> body = new ArrayList<>();
			if (demand)
				body.add(require_inequality_XY);
			body.add(letter_i_XA);
			body.add(letter_i_YB);
			body.add(inequality);

			Rule unequal = Expressions.makeRule(unequal_XY, body.toArray(new Atom[body.size()]));
			reasoner.addRules(unequal);
		}
		
		return file;
	}
	
	static List<List<String>> chunks (String string) {
		List<List<String>> result = new ArrayList<>();
		
		List<String> list = toList(string);
		list.add(NONE);

		for (int i = 0; i < list.size(); i += chunkSize) {
			List<String> part = new ArrayList<>();
			for (int j = i; j < i + chunkSize; j++) {
				if (list.size() <= j)
					break;
				part.add(list.get(j));
			}
			fillUp(part);
			result.add(part);
		}
		return result;
	}
	
	static List<String> toList(String string) {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < string.length(); i++) {
			result.add(string.substring(i, i+1));
		}
		return result;
	}
	
	static void fillUp(List<String> list) {
		for (int i = list.size(); i < chunkSize; i++) {
			list.add(NONE);
		}
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
