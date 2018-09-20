package impl.PCC;

import static utility.SC.i;
import static utility.SC.last;
import static utility.SC.o;
import static utility.SC.p;
import static utility.SC.q;
import static utility.SC.require;
import static utility.SC.s;
import static utility.SC.statementEDB;
import static utility.SC.v;
import static utility.SC.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import utility.InequalityHelper;
import utility.StatementNonExistenceHelper;
import utility.Utility;

public class ItemRequiresStatementPCC extends PropertyConstraintChecker {
	
	final Map<String, Set<String>> configuration;

	public ItemRequiresStatementPCC(String property_, Map<String, Set<String>> configuration_) throws IOException {
		super(property_);
		configuration = configuration_;
	}

	@Override
	public Set<Rule> rules() {
		Set<Rule> rules = new HashSet<Rule>();

		// statementEDB(Q, I, propertyConstant, X)
		Atom statementEDB_QIpX = Expressions.makeAtom(statementEDB, q, i, propertyConstant, x);
	
		// statementEDB(S, I, P, V)
		Atom statementEDB_SIPV = Expressions.makeAtom(statementEDB, s, i, p, v);
		
		int uniqueID = 0;
		for (Map.Entry<String, Set<String>> entry : configuration.entrySet()) {
			Constant requiredPropertyConstant = Utility.makeConstant(entry.getKey());
			
			Constant uniqueIDConstant = Expressions.makeConstant(property + String.valueOf(uniqueID));

			// unequal(requiredPropertyConstant, P)
			Atom unequal_rP = Expressions.makeAtom(InequalityHelper.unequal, requiredPropertyConstant, p);
		
			rules.addAll(StatementNonExistenceHelper.initRequireStatement(uniqueIDConstant, statementEDB_QIpX, statementEDB_SIPV, unequal_rP));
			
			Set<String> allowedValues = entry.getValue();				
			if (allowedValues.size() != 0) {
				// statementEDB(S, I, requiredPropertyConstant, V)
				Atom statementEDB_SIrV = Expressions.makeAtom(statementEDB, s, i, requiredPropertyConstant, v);
				
				List<Atom> conjunction = new ArrayList<Atom>();
				conjunction.add(statementEDB_QIpX);
				conjunction.add(statementEDB_SIrV);
				
				for (String allowedValue : allowedValues) {
					Constant allowedValueConstant = Utility.makeConstant(allowedValue);
					conjunction.add(Expressions.makeAtom(InequalityHelper.unequal, allowedValueConstant, v));
				}
				
				rules.addAll(StatementNonExistenceHelper.initRequireStatement(uniqueIDConstant, conjunction));
			}
			
			// last(O, I)
			Atom last_OI = Expressions.makeAtom(last, o, i);
			
			// require(O, uniqueIDConstant)
			Atom require_Ou = Expressions.makeAtom(require, o, uniqueIDConstant);
			
			// violation_statement(S, I, propertyConstant, V) :-
			//	statementEDB(S, I, propertyConstant, V),
			//	last(O, I),
			//	require(O, uniqueIDConstant)
			Rule violation = Expressions.makeRule(violation_statement_SIpV, statementEDB_SIpV, last_OI, require_Ou);
			rules.add(violation);
			uniqueID++;
		}
		
		return rules;
	}
}
