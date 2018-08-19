package impl.PCC;

import static utility.SC.i;
import static utility.SC.last;
import static utility.SC.o;
import static utility.SC.r;
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

public class InversePCC extends PropertyConstraintChecker {
	
	final Set<String> configuration;

	public InversePCC(String property_, Set<String> configuration_) throws IOException {
		super(property_);
		configuration = configuration_;
	}

	@Override
	public List<Rule> rules() {		
		List<Rule> rules = new ArrayList<Rule>();
		
		for (String inverseProperty : configuration) {
			Term inversePropertyConstant = Utility.makeConstant(inverseProperty);
			
			// tripleEDB(O, R, propertyConstant, I)
			Atom tripleEDB_ORpI = Expressions.makeAtom(tripleEDB, o, r, propertyConstant, i);
		
			// tripleEDB(S, I, P, V)
			Atom tripleEDB_SIPV = Expressions.makeAtom(tripleEDB, s, i, p, v);
		
			// unequal(inversePropertyConstant, P)
			Atom unequal_iP = Expressions.makeAtom(InequalityHelper.unequal, inversePropertyConstant, p);
		
			rules.addAll(StatementNonExistenceHelper.initRequireTriple(inversePropertyConstant, r, tripleEDB_ORpI, tripleEDB_SIPV, unequal_iP));
			
			// tripleEDB(S, I, inversePropertyConstant, V)
			Atom tripleEDB_SIiV = Expressions.makeAtom(tripleEDB, s, i, inversePropertyConstant, v);
			
			// unequal(R, V)
			Atom unequal_RV = Expressions.makeAtom(InequalityHelper.unequal, r, v);
			
			rules.addAll(StatementNonExistenceHelper.initRequireTriple(inversePropertyConstant, r, tripleEDB_ORpI, tripleEDB_SIiV, unequal_RV));
		}
		
		for (String inverseProperty : configuration) {
			Term inversePropertyConstant = Utility.makeConstant(inverseProperty);
			
			// tripleEDB(O, V, P, X)
			Atom tripleEDB_OVPX = Expressions.makeAtom(tripleEDB, o, v, p, x);
			
			// last(O, V)
			Atom last_OV = Expressions.makeAtom(last, o, v);
			
			// require(O, inverseProperty, r)
			Atom require_OiR = Expressions.makeAtom(require, o, inversePropertyConstant, r);
			
			// violation_triple(S, I, propertyConstant, V) :-
			//	tripleEDB(S, I, propertyConstant, V),
			//	tripleEDB(O, V, P, X), last(O, V),
			//	require(O, inversePropertyConstant, R)
			Rule violation = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OVPX, last_OV, require_OiR);
			rules.add(violation);
		}
		
		return rules;
	}
}
