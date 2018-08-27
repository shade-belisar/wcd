package impl.PCC;

import static utility.SC.i;
import static utility.SC.last;
import static utility.SC.o;
import static utility.SC.p;
import static utility.SC.q;
import static utility.SC.r;
import static utility.SC.require;
import static utility.SC.s;
import static utility.SC.statementEDB;
import static utility.SC.v;
import static utility.SC.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import utility.InequalityHelper;
import utility.StatementNonExistenceHelper;
import utility.Utility;

public class InversePCC extends PropertyConstraintChecker {
	
	final Set<String> configuration;

	public InversePCC(String property_, Set<String> configuration_) throws IOException {
		super(property_);
		configuration = configuration_;
	}

	@Override
	public List<Rule> rules() {		
		List<Rule> rules = new ArrayList<Rule>();
		
		// statementEDB(Q, R, propertyConstant, I)
		Atom statementEDB_QRpI = Expressions.makeAtom(statementEDB, q, r, propertyConstant, i);
	
		// statementEDB(S, I, P, V)
		Atom statementEDB_SIPV = Expressions.makeAtom(statementEDB, s, i, p, v);
		
		for (String inverseProperty : configuration) {
			Term inversePropertyConstant = Utility.makeConstant(inverseProperty);
		
			// unequal(inversePropertyConstant, P)
			Atom unequal_iP = Expressions.makeAtom(InequalityHelper.unequal, inversePropertyConstant, p);
		
			rules.addAll(StatementNonExistenceHelper.initRequireStatement(inversePropertyConstant, r, statementEDB_QRpI, statementEDB_SIPV, unequal_iP));
			
			// statementEDB(S, I, inversePropertyConstant, V)
			Atom statementEDB_SIiV = Expressions.makeAtom(statementEDB, s, i, inversePropertyConstant, v);
			
			// unequal(R, V)
			Atom unequal_RV = Expressions.makeAtom(InequalityHelper.unequal, r, v);
			
			rules.addAll(StatementNonExistenceHelper.initRequireStatement(inversePropertyConstant, r, statementEDB_QRpI, statementEDB_SIiV, unequal_RV));
		}
		
		for (String inverseProperty : configuration) {
			Term inversePropertyConstant = Utility.makeConstant(inverseProperty);
			
			// statementEDB(O, V, P, X)
			Atom statementEDB_OVPX = Expressions.makeAtom(statementEDB, o, v, p, x);
			
			// last(O, V)
			Atom last_OV = Expressions.makeAtom(last, o, v);
			
			// require(O, inverseProperty, r)
			Atom require_OiR = Expressions.makeAtom(require, o, inversePropertyConstant, r);
			
			// violation_statement(S, I, propertyConstant, V) :-
			//	statementEDB(S, I, propertyConstant, V),
			//	statementEDB(O, V, P, X), last(O, V),
			//	require(O, inversePropertyConstant, R)
			Rule violation = Expressions.makeRule(violation_statement_SIpV, statementEDB_SIpV, statementEDB_OVPX, last_OV, require_OiR);
			rules.add(violation);
		}
		
		return rules;
	}
}
