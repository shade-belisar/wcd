package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.TS.StatementsItemTS;
import impl.TS.TripleSet;
import utility.PrepareQueriesException;
import utility.Utility;

public class ConflictsWithPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ConflictsWithPCC.class);
	
	final TripleSet tripleSet;
	
	final Map<String, HashSet<String>> conflicts;

	public ConflictsWithPCC(String property_, Map<String, HashSet<String>> qualifiers_) throws IOException {
		super(property_);
		conflicts = qualifiers_;
		tripleSet = new StatementsItemTS(property);
	}

	@Override
	public String violations() throws IOException {	
		if (!tripleSet.notEmpty())
			return "";
		
		try {
			loadTripleSets(tripleSet);
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		List<Rule> rules = new ArrayList<Rule>();
		
		// violation_long(STATEMENT, X, propertyConstant, Y)
		Atom violation_long_SXpY = Expressions.makeAtom(violation_long, statement, x, propertyConstant, y);
		
		// tripleEDB(STATEMENT, X, propertyConstant, Y)
		Atom tripleEDB_SXpY = Expressions.makeAtom(tripleEDB, statement, x, propertyConstant, y);
		
		for (Map.Entry<String, HashSet<String>> entry : conflicts.entrySet()) {
			String confProperty = entry.getKey();
			Constant confPropertyConstant = Utility.makeConstant(confProperty);
			HashSet<String> confValues = entry.getValue();
			
			if (confValues.size() == 0) {
				// tripleEDB(OTHER_STATEMENT, X, confPropertyConstant, Z)
				Atom tripleEDB_OXcZ = Expressions.makeAtom(tripleEDB, otherStatement, x, confPropertyConstant, z);
				
				// violation_long(STATEMENT, X, propertyConstant, Y) :- tripleEDB(STATEMENT, X, propertyConstant, Y), tripleEDB(OTHER_STATEMENT, X, confPropertyConstant, Z)
				Rule conflicting = Expressions.makeRule(violation_long_SXpY, tripleEDB_SXpY, tripleEDB_OXcZ);
				
				rules.add(conflicting);
			} else {
				for (String value : confValues) {
					Constant confValueConstant =  Expressions.makeConstant(value);
					// tripleEDB(OTHER_STATEMENT, X, confPropertyConstant, confValueConstant)
					Atom tripleEDB_OXcc = Expressions.makeAtom(tripleEDB, otherStatement, x, confPropertyConstant, confValueConstant);
					
					// violation_long(STATEMENT, X, propertyConstant, Y) :-
					//	tripleEDB(STATEMENT, X, propertyConstant, Y),
					//	tripleEDB(OTHER_STATEMENT, X, confPropertyConstant, confValueConstant)
					Rule conflicting = Expressions.makeRule(violation_long_SXpY, tripleEDB_SXpY, tripleEDB_OXcc);
					
					rules.add(conflicting);
				}
			}
		}
		
		try {
			return prepareAndExecuteQueries(rules, Arrays.asList(violation_long_query));
		} catch (PrepareQueriesException e) {
			return e.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
