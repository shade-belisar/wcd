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
import static utility.SC.tripleEDB;
import static utility.SC.v;
import static utility.SC.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	public List<Rule> rules() {
		List<Rule> rules = new ArrayList<Rule>();
		
		// tripleEDB(Q, I, propertyConstant, X)
		Atom tripleEDB_QIpX = Expressions.makeAtom(tripleEDB, q, i, propertyConstant, x);
		
		// tripleEDB(S, I, P, V)
		Atom tripleEDB_SIPV = Expressions.makeAtom(tripleEDB, s, i, p, v);
		
		// unequal(propertyConstant, P)
		Atom unequal_pP = Expressions.makeAtom(InequalityHelper.unequal, propertyConstant, p);
		
		rules.addAll(StatementNonExistenceHelper.initRequireTriple(propertyConstant, propertyConstant, tripleEDB_QIpX, tripleEDB_SIPV, unequal_pP));
		
		// require_second(S, propertyConstant, propertyConstant)
		Atom require_second_Spp = Expressions.makeAtom(require_second, s, propertyConstant, propertyConstant);
		
		// next(T, R)
		Atom next_TR = Expressions.makeAtom(next, t, r);
		
		// require(T, propertyConstant, propertyConstant)
		Atom require_Tpp = Expressions.makeAtom(require, t, propertyConstant, propertyConstant);
		
		// next(R, S)
		Atom next_RS = Expressions.makeAtom(next, r, s);

		// tripleEDB(R, I, propertyConstant, C)
		Atom tripleEDB_RIpC = Expressions.makeAtom(tripleEDB, r, i, propertyConstant, c);
		
		// require_second(S, propertyConstant, propertyConstant) :-
		//	tripleEDB(Q, I, propertyConstant, X),
		//	tripleEDB(S, I, P, V),
		//	unequal(propertyConstant, P)
		//	next(R, S),
		//	tripleEDB(R, I, propertyConstant, C),
		//	next(T, R),
		//	require(T, propertyConstant, propertyConstant)
		Rule require1 = Expressions.makeRule(require_second_Spp, tripleEDB_QIpX, tripleEDB_SIPV, unequal_pP, next_RS, tripleEDB_RIpC, next_TR, require_Tpp);
		rules.add(require1);
		
		// require_second(R, propertyConstant, propertyConstant)
		Atom require_second_Rpp = Expressions.makeAtom(require_second, r, propertyConstant, propertyConstant);
		
		// require_second(S, propertyConstant, propertyConstant) :-
		//	tripleEDB(Q, I, propertyConstant, X),
		//	tripleEDB(S, I, P, V),
		//	unequal(propertyConstant, P),
		//	next(R, S),
		//	require_second(R, propertyConstant, propertyConstant)
		Rule require2 = Expressions.makeRule(require_second_Spp, tripleEDB_QIpX, tripleEDB_SIPV, unequal_pP, next_RS, require_second_Rpp);
		rules.add(require2);
		
		// tripleEDB(O, I, P, X)
		Atom tripleEDB_OIPX = Expressions.makeAtom(tripleEDB, o, i, p, x);
				
		// last(O, I)
		Atom last_OI = Expressions.makeAtom(last, o, i);
		
		// require_second(O, propertyConstant, propertyConstant)
		Atom require_second_Opp = Expressions.makeAtom(require_second, o, propertyConstant, propertyConstant);
		
		// violation_triple(S, I, propertyConstant, V) :-
		//	tripleEDB(S, I, propertyConstant, V),
		//	tripleEDB(O, I, P, X), last(O, I),
		//	require_second(O, propertyConstant, propertyConstant)
		Rule violation = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OIPX, last_OI, require_second_Opp);
		rules.add(violation);
		
		return rules;
	}

}
