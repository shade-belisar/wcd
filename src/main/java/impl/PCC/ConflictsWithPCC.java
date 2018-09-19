package impl.PCC;

import static utility.SC.c;
import static utility.SC.i;
import static utility.SC.o;
import static utility.SC.statementEDB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import utility.Utility;

public class ConflictsWithPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ConflictsWithPCC.class);
	
	final Map<String, HashSet<String>> conflicts;

	public ConflictsWithPCC(String property_, Map<String, HashSet<String>> qualifiers_) throws IOException {
		super(property_);
		conflicts = qualifiers_;
	}

	@Override
	public Set<Rule> rules() {
		Set<Rule> rules = new HashSet<Rule>();
		
		for (Map.Entry<String, HashSet<String>> entry : conflicts.entrySet()) {
			String confProperty = entry.getKey();
			Constant confPropertyConstant = Utility.makeConstant(confProperty);
			HashSet<String> confValues = entry.getValue();
			
			if (confValues.size() == 0) {
				// statementEDB(O, I, confPropertyConstant, C)
				Atom statementEDB_OIcC = Expressions.makeAtom(statementEDB, o, i, confPropertyConstant, c);
				
				// violation_statement(S, I, propertyConstant, V) :- statementEDB(S, I, propertyConstant, V), statementEDB(O, I, confPropertyConstant, C)
				Rule conflicting = Expressions.makeRule(violation_statement_SIpV, statementEDB_SIpV, statementEDB_OIcC);
				
				rules.add(conflicting);
			} else {
				for (String value : confValues) {
					Constant confValueConstant =  Utility.makeConstant(value);
					// statementEDB(O, I, confPropertyConstant, confValueConstant)
					Atom statementEDB_OIcc = Expressions.makeAtom(statementEDB, o, i, confPropertyConstant, confValueConstant);
					
					// violation_statement(S, I, propertyConstant, V) :-
					//	statementEDB(S, I, propertyConstant, V),
					//	statementEDB(O, I, confPropertyConstant, confValueConstant)
					Rule conflicting = Expressions.makeRule(violation_statement_SIpV, statementEDB_SIpV, statementEDB_OIcc);
					
					rules.add(conflicting);
				}
			}
		}
		
		return rules;
	}
}
