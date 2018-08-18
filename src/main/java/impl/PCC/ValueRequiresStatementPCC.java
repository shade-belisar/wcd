package impl.PCC;

import static utility.SC.i;
import static utility.SC.last;
import static utility.SC.o;
import static utility.SC.p;
import static utility.SC.require;
import static utility.SC.s;
import static utility.SC.tripleEDB;
import static utility.SC.v;
import static utility.SC.x;

import java.io.IOException;
import java.util.ArrayList;
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

public class ValueRequiresStatementPCC extends PropertyConstraintChecker {
	
	final Map<String, Set<String>> configuration;

	public ValueRequiresStatementPCC(String property_, Map<String, Set<String>> configuration_) throws IOException {
		super(property_);
		configuration = configuration_;
	}

	@Override
	public List<Rule> rules() {
		List<Rule> rules = new ArrayList<Rule>();
		
		for (Map.Entry<String, Set<String>> entry : configuration.entrySet()) {
			Term requiredPropertyConstant = Utility.makeConstant(entry.getKey());
		
			// tripleEDB(S, I, P, V)
			Atom tripleEDB_SIPV = Expressions.makeAtom(tripleEDB, s, i, p, v);
		
			// unequal(requiredPropertyConstant, P)
			Atom unequal_rP = Expressions.makeAtom(InequalityHelper.unequal, requiredPropertyConstant, p);
		
			rules.addAll(StatementNonExistenceHelper.initRequireTriple(requiredPropertyConstant, tripleEDB_SIPV, unequal_rP));
			
			Set<String> allowedValues = entry.getValue();				
			if (allowedValues.size() != 0) {
				// tripleEDB(S, I, requiredPropertyConstant, V)
				Atom tripleEDB_SIrV = Expressions.makeAtom(tripleEDB, s, i, requiredPropertyConstant, v);
				
				List<Atom> conjunction = new ArrayList<Atom>();
				conjunction.add(tripleEDB_SIrV);
				
				for (String allowedValue : allowedValues) {
					Constant allowedValueConstant = Utility.makeConstant(allowedValue);
					conjunction.add(Expressions.makeAtom(InequalityHelper.unequal, allowedValueConstant, v));
				}
				
				rules.addAll(StatementNonExistenceHelper.initRequireTriple(requiredPropertyConstant, conjunction));
			}
		}
		
		for (String requiredProperty : configuration.keySet()) {
			Term requiredPropertyConstant = Utility.makeConstant(requiredProperty);
			
			// tripleEDB(O, V, P, X)
			Atom tripleEDB_OVPX = Expressions.makeAtom(tripleEDB, o, v, p, x);
			
			// last(O, V)
			Atom last_OV = Expressions.makeAtom(last, o, v);
			
			// require(O, requiredPropertyConstant)
			Atom require_Or = Expressions.makeAtom(require, o, requiredPropertyConstant);
			
			// violation_triple(S, I, propertyConstant, V) :-
			//	tripleEDB(S, I, propertyConstant, V),
			//	tripleEDB(O, V, P, X), last(O, V),
			//	require(O, requiredPropertyConstant)
			Rule violation = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OVPX, last_OV, require_Or);
			rules.add(violation);
		}
		
		return rules;
	}

}
