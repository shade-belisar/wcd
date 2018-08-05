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
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.TS.ConflictsWithTS;
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
		tripleSet = new ConflictsWithTS(property);
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
		
		for (Map.Entry<String, HashSet<String>> entry : conflicts.entrySet()) {
			String confProperty = entry.getKey();
			Constant confPropertyConstant = Utility.makeConstant(confProperty);
			HashSet<String> confValues = entry.getValue();
			
			if (confValues.size() == 0) {
				// tripleEDB(O, I, confPropertyConstant, C)
				Atom tripleEDB_OIcC = Expressions.makeAtom(tripleEDB, o, i, confPropertyConstant, c);
				
				// violation_triple(S, I, propertyConstant, V) :- tripleEDB(S, I, propertyConstant, V), tripleEDB(O, I, confPropertyConstant, C)
				Rule conflicting = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OIcC);
				
				rules.add(conflicting);
			} else {
				for (String value : confValues) {
					Constant confValueConstant =  Utility.makeConstant(value);
					// tripleEDB(O, I, confPropertyConstant, confValueConstant)
					Atom tripleEDB_OIcc = Expressions.makeAtom(tripleEDB, o, i, confPropertyConstant, confValueConstant);
					
					// violation_triple(S, I, propertyConstant, V) :-
					//	tripleEDB(S, I, propertyConstant, V),
					//	tripleEDB(O, I, confPropertyConstant, confValueConstant)
					Rule conflicting = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OIcc);
					
					rules.add(conflicting);
				}
			}
		}
		
		try {
			return prepareAndExecuteQueries(rules, Arrays.asList(violation_triple_query));
		} catch (PrepareQueriesException e) {
			return e.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
