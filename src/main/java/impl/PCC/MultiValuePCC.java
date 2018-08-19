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
		
		// tripleEDB(S, I, P, V)
		Atom tripleEDB_SIPV = Expressions.makeAtom(tripleEDB, s, i, p, v);
		
		// unequal(propertyConstant, P)
		Atom unequal_pP = Expressions.makeAtom(InequalityHelper.unequal, propertyConstant, p);
		
		rules.addAll(StatementNonExistenceHelper.initRequireTriple(propertyConstant, propertyConstant, tripleEDB_SIPV, unequal_pP));
		
		// require_second(S, propertyConstant, propertyConstant)
		Atom require_second_Spp = Expressions.makeAtom(require_second, s, propertyConstant, propertyConstant);
		
		// next(Q, R)
		Atom next_QR = Expressions.makeAtom(next, q, r);
		
		// next(R, S)
		Atom next_RS = Expressions.makeAtom(next, r, s);
		
		// require(Q, propertyConstant, propertyConstant)
		Atom require_Qpp = Expressions.makeAtom(require, q, propertyConstant, propertyConstant);
		
		// tripleEDB(R, I, propertyConstant, C)
		Atom tripleEDB_RIpC = Expressions.makeAtom(tripleEDB, r, i, propertyConstant, c);
		
		// require_second(S, propertyConstant, propertyConstant) :-
		//	next(Q, R), next(R, S),
		//	require(Q, propertyConstant, propertyConstant),
		//	tripleEDB(R, I, propertyConstant, C),
		//	tripleEDB(S, I, P, V),
		//	unequal(propertyConstant, P)
		Rule require1 = Expressions.makeRule(require_second_Spp, next_QR, next_RS, require_Qpp, tripleEDB_RIpC, tripleEDB_SIPV, unequal_pP);
		rules.add(require1);
		
		// require_second(R, propertyConstant, propertyConstant)
		Atom require_second_Rpp = Expressions.makeAtom(require_second, r, propertyConstant, propertyConstant);
		
		// require_second(S, propertyConstant, propertyConstant) :-
		//	next(R, S),
		//	require_second(R, propertyConstant, propertyConstant),
		//	tripleEDB(S, I, P, V),
		//	unequal(propertyConstant, P)
		Rule require2 = Expressions.makeRule(require_second_Spp, next_RS, require_second_Rpp, tripleEDB_SIPV, unequal_pP);
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
