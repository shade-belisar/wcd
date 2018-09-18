package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.CC.ConstraintChecker;
import impl.DS.DataSet.DataSetPredicate;
import impl.DS.DataSetFile;
import main.Main;
import positionVLog4J.PositionPredicate;

import static utility.SC.require_inequality;

public class InequalityHelper {
	
	final static Logger logger = Logger.getLogger(InequalityHelper.class);
	
	public enum Mode {
		ENCODED,
		ON_DEMAND
	}

	static Mode mode = Mode.ON_DEMAND;
	
	static boolean demand = false;
	
	final static int chunkSize = 7;
	
	final static String NONE = "none";
	final static Map<String, String> replacementNames = replacementNameMap();
	static Map<String, String> replacementNameMap() {
		Map<String, String> result = new HashMap<>();
		result.put(" ", "SPACE");
		result.put(Character.toString((char)12288), "IDEOGRAPHIC_SPACE");
		result.put(Character.toString((char)8194), "EN_SPACE");
		result.put(Character.toString((char)8195), "EM_SPACE");
		result.put(Character.toString((char)8197), "FOUR-PER-EM_SPACE");
		result.put(Character.toString((char)8201), "THIN SPACE");
		return result;
	}
	
	
	final static String X = "x";
	final static String Y = "y";
	
	final static String CON1 = "con1";
	final static String CON2 = "con2";
	
	final static String UNEQUAL_EDB = "unequal_EDB";
	final static String UNEQUAL = "unequal";
	final static String UNEQUAL_NAIVE = "unequal_naive";
	
	final static String UNIQUE_CHARACTERS = "unique_characters.csv.gz";
	
	final static Variable x = Expressions.makeVariable(X);
	final static Variable y = Expressions.makeVariable(Y);
	
	final static Predicate unequal_EDB = Expressions.makePredicate(UNEQUAL_EDB, 2);
	public final static Predicate unequal = Expressions.makePredicate(UNEQUAL, 2);
	
	// require_inequality(X, Y)
	final static Atom require_inequality_XY = Expressions.makeAtom(require_inequality, x, y);
	
	// unequal(X, Y)
	final static Atom unequal_XY = Expressions.makeAtom(unequal, x, y);
	
	// unequal_IDB(X, Y) :- unequal_EDB(X, Y)
	final static Rule unequal_IDB_EDB = Expressions.makeRule(Expressions.makeAtom(unequal, x, y), Expressions.makeAtom(unequal_EDB, x, y));
			
	// unequal_IDB(X, Y) :- unequal_IDB(Y, X)
	final static Rule inverse = Expressions.makeRule(Expressions.makeAtom(unequal, x, y), Expressions.makeAtom(unequal, y, x));
	
	final static Map<ConstraintChecker, InequalityHandler> helpers = new HashMap<>();
	
	public static void setMode(Mode mode_) {
		mode = mode_;
		switch (mode) {
		case ENCODED:
			demand = false;
			break;
		case ON_DEMAND:
			demand = true;
			break;
		}
	}
	
	public static Mode getMode() {
		return mode;
	}
	
	public static InequalityHandler getInequalityHelper(ConstraintChecker checker) {
		String folder = normalize(checker.getConstraint());
		if (!helpers.containsKey(checker))
			helpers.put(checker, new InequalityHandler(folder));
		return helpers.get(checker);
	}
	
	static String normalize(String string) {
		string = string.toLowerCase();
		if (!string.endsWith("/"))
			string += "/";
		return string;
	}
	
	public static void load(ConstraintChecker checker) throws ReasonerStateException, IOException {
		if (!helpers.containsKey(checker))
			return;
		InequalityHandler helper = helpers.get(checker);
		Reasoner reasoner = checker.getReasoner();
		if (Main.getReload())
			helper.reload(reasoner);
		else
			helper.load(reasoner);
			
	}
	
	public static void encoded(DataSetPredicate predicate, String...entries) throws IOException {
		List<String> entriesList = Arrays.asList(entries);
		
		for (InequalityHandler handler : helpers.values()) {
			handler.encoded(predicate, entriesList);
		}
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
				String toAdd = list.get(j);
				if (replacementNames.containsKey(toAdd))
					toAdd = replacementNames.get(toAdd);
				part.add(toAdd);
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
			try {
				Constant allowedCharacterConstant = Expressions.makeConstant(allowedCharacter); 
				indentifierParts.add(allowedCharacterConstant);
			} catch (IllegalArgumentException e) {
				logger.error("Character '" + allowedCharacter + "' is not allowed since it is an empty string. " + allowedCharacter.length());
				throw e;
			}
			
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
	
	public static class InequalityHandler {
		final String FOLDER;
		
		final List<Rule> rules = new ArrayList<>();
		
		final Set<String> characters = new HashSet<String>();
		
		final List<DataSetFile> files = new ArrayList<>();
		
		final Map<DataSetPredicate, Set<Integer>> columns = new HashMap<>();
		
		final Set<String> additionalInequalities = new HashSet<String>();
		
		private InequalityHandler(String folder_) {
			if (!folder_.endsWith("/"))
				folder_ += "/";
			FOLDER = folder_;
		}
		
		
		public InequalityHandler registerInequality(Set<String> inequalities) {
			additionalInequalities.addAll(inequalities);
			return this;
		}
		
		public InequalityHandler registerInequality(DataSetPredicate dataSetPredicate, int index) throws IOException {			
			if (!columns.containsKey(dataSetPredicate))
				columns.put(dataSetPredicate, new HashSet<>());
			
			columns.get(dataSetPredicate).add(index);
			return this;
		}

		void close() throws IOException {
			for (DataSetFile file : files) {
				file.close();
			}
		}

		void load(Reasoner reasoner) throws ReasonerStateException, IOException {
			encodedAdditional();
			
			File uniqueCharacters = new File(DataSetFile.BASE_LOCATION + FOLDER + UNIQUE_CHARACTERS);
			FileOutputStream output = new FileOutputStream(uniqueCharacters, false);
			GZIPOutputStream gzip = new GZIPOutputStream(output);
			OutputStreamWriter writer = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
			for (String character : characters) {
				writer.write(character + "\n");
			}		
			writer.close();
			
			commonLoad(reasoner);
		}
		
		void reload(Reasoner reasoner) throws ReasonerStateException, IOException {
			boolean demand = false;
			
			switch (mode) {
			case ON_DEMAND:
				demand = true;
			default:
			}
			int chunk = 0;
			while (getChunkFile(chunk, demand).didExist())
				chunk++;
			DataSetFile lastFile = files.get(files.size() - 1);
			files.remove(lastFile);
			lastFile.getFile().delete();
			
			File uniqueCharacters = new File(DataSetFile.BASE_LOCATION + FOLDER + UNIQUE_CHARACTERS);
			FileInputStream input = new FileInputStream(uniqueCharacters);
			GZIPInputStream gzip = new GZIPInputStream(input);
			InputStreamReader reader = new InputStreamReader(gzip, StandardCharsets.UTF_8);
			BufferedReader buffer = new BufferedReader(reader);
			String line;
			while((line = buffer.readLine()) != null) {
				characters.add(line);
			}
			buffer.close();
			
			commonLoad(reasoner);
		}

		void commonLoad(Reasoner reasoner) throws ReasonerStateException, IOException {
			reasoner.addRules(unequal_IDB_EDB, inverse);
			
			reasoner.addRules(rules);
			
			List<Atom> facts = allCharactersUnequal(characters);
			reasoner.addFacts(facts);
			
			for (DataSetFile dataSetFile : files) {
				dataSetFile.loadFile(reasoner);
			}
		}

		void encodedAdditional() throws IOException {
			for (String string : additionalInequalities) {
				encoded(string);
			}
		}
		
		public void encoded(DataSetPredicate predicate, List<String> entries) throws IOException {
			if (!columns.containsKey(predicate))
				return;
			for (Integer i : columns.get(predicate)) {
				encoded(entries.get(i));
			}
		}

		void encoded(String string) throws IOException {		
			int i = 0;
			for (List<String> chunk : chunks(string)) {
				characters.addAll(chunk);
				
				DataSetFile file = getChunkFile(i, demand);
				
				List<String> line = new ArrayList<>();
				line.add(string);
				line.addAll(chunk);
				file.write(line);
				i++;
			}
		}
		
		DataSetFile getChunkFile(int chunk, boolean demand) throws IOException {
			if (chunk < files.size())
				return files.get(chunk);
		
			int chunkAdress = chunk * chunkSize;
			String LETTER_I = "letter" + chunkAdress;
			Predicate letter_i = Expressions.makePredicate(LETTER_I, chunkSize + 1);
			DataSetFile file = new DataSetFile(FOLDER, LETTER_I, letter_i, !Main.getReload());
			files.add(file);
			
			List<Atom> inequalities = new ArrayList<>();
			
			// letter_i(X, Ai, Ai+1, ..., Ai+6)
			List<Term> xVariables = new ArrayList<>();
			xVariables.add(x);
			// letter_i(Y, Bi, Bi+1, ..., Bi+6)
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
				rules.add(unequal);
			}
			
			return file;
		}
	}
}
