package impl.PCC;

import static utility.SC.c;
import static utility.SC.i;
import static utility.SC.last;
import static utility.SC.next;
import static utility.SC.o;
import static utility.SC.p;
import static utility.SC.q;
import static utility.SC.r;
import static utility.SC.require;
import static utility.SC.require_second;
import static utility.SC.s;
import static utility.SC.t;
import static utility.SC.statementEDB;
import static utility.SC.v;
import static utility.SC.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import utility.InequalityHelper;
import utility.StatementNonExistenceHelper;

public class MultiValuePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(MultiValuePCC.class);

	public MultiValuePCC(String property_) throws IOException {
		super(property_);
	}

	@Override
	public Set<Rule> rules() {
		Set<Rule> rules = new HashSet<Rule>();
		
		// statementEDB(Q, I, propertyConstant, X)
		Atom statementEDB_QIpX = Expressions.makeAtom(statementEDB, q, i, propertyConstant, x);
		
		// statementEDB(S, I, P, V)
		Atom statementEDB_SIPV = Expressions.makeAtom(statementEDB, s, i, p, v);
		
		// unequal(propertyConstant, P)
		Atom unequal_pP = Expressions.makeAtom(InequalityHelper.unequal, propertyConstant, p);
		
		rules.addAll(StatementNonExistenceHelper.initRequireStatement(propertyConstant, statementEDB_QIpX, statementEDB_SIPV, unequal_pP));
		
		// require_second(S, propertyConstant)
		Atom require_second_Sp = Expressions.makeAtom(require_second, s, propertyConstant);
		
		// next(T, R)
		Atom next_TR = Expressions.makeAtom(next, t, r);
		
		// require(T, propertyConstant)
		Atom require_Tp = Expressions.makeAtom(require, t, propertyConstant);
		
		// next(R, S)
		Atom next_RS = Expressions.makeAtom(next, r, s);

		// statementEDB(R, I, propertyConstant, C)
		Atom statementEDB_RIpC = Expressions.makeAtom(statementEDB, r, i, propertyConstant, c);
		
		// require_second(S, propertyConstant) :-
		//	statementEDB(Q, I, propertyConstant, X),
		//	statementEDB(S, I, P, V),
		//	unequal(propertyConstant, P)
		//	next(R, S),
		//	statementEDB(R, I, propertyConstant, C),
		//	next(T, R),
		//	require(T, propertyConstant)
		Rule require1 = Expressions.makeRule(require_second_Sp, statementEDB_QIpX, statementEDB_SIPV, unequal_pP, next_RS, statementEDB_RIpC, next_TR, require_Tp);
		rules.add(require1);
		
		// require_second(R, propertyConstant)
		Atom require_second_Rp = Expressions.makeAtom(require_second, r, propertyConstant);
		
		// require_second(S, propertyConstant) :-
		//	statementEDB(Q, I, propertyConstant, X),
		//	statementEDB(S, I, P, V),
		//	unequal(propertyConstant, P),
		//	next(R, S),
		//	require_second(R, propertyConstant)
		Rule require2 = Expressions.makeRule(require_second_Sp, statementEDB_QIpX, statementEDB_SIPV, unequal_pP, next_RS, require_second_Rp);
		rules.add(require2);
		
		// statementEDB(O, I, P, X)
		Atom statementEDB_OIPX = Expressions.makeAtom(statementEDB, o, i, p, x);
				
		// last(O, I)
		Atom last_OI = Expressions.makeAtom(last, o, i);
		
		// require_second(O, propertyConstant)
		Atom require_second_Op = Expressions.makeAtom(require_second, o, propertyConstant);
		
		// violation_statement(S, I, propertyConstant, V) :-
		//	statementEDB(S, I, propertyConstant, V),
		//	statementEDB(O, I, P, X), last(O, I),
		//	require_second(O, propertyConstant)
		Rule violation = Expressions.makeRule(violation_statement_SIpV, statementEDB_SIpV, statementEDB_OIPX, last_OI, require_second_Op);
		rules.add(violation);
		
		return rules;
	}

}
