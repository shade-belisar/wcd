package impl.PCC;

import static utility.SC.c;
import static utility.SC.does_not_have;
import static utility.SC.has_same;
import static utility.SC.i;
import static utility.SC.last_qualifier;
import static utility.SC.o;
import static utility.SC.p;
import static utility.SC.q;
import static utility.SC.qualifierEDB;
import static utility.SC.r;
import static utility.SC.referenceEDB;
import static utility.SC.require_qualifier;
import static utility.SC.s;
import static utility.SC.tripleEDB;
import static utility.SC.v;
import static utility.SC.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import com.google.common.collect.Sets;

import utility.InequalityHelper;
import utility.StatementNonExistenceHelper;

public class SingleValuePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SingleValuePCC.class);
	
	final Set<String> separators;

	public SingleValuePCC(String property_, Set<String> separators_) throws IOException {
		super(property_);
		separators = separators_;
	}

	@Override
	public List<Rule> rules() { 	
		List<Rule> rules = new ArrayList<Rule>();
		
		// tripleEDB(O, I, propertyConstant, X)
		Atom tripleEDB_OIpX = Expressions.makeAtom(tripleEDB, o, i, propertyConstant, x);
		
		// unequal(S, O)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, s, o);
		
		// qualifierEDB(S, propertyConstant, V)
		Atom qualifierEDB_SpV = Expressions.makeAtom(qualifierEDB, s, propertyConstant, v);
		
		// qualifierEDB(S, propertyConstant, X)
		Atom qualifierEDB_SpX = Expressions.makeAtom(qualifierEDB, s, propertyConstant, x);
		
		// unequal(V, X)
		Atom unequal_VX = Expressions.makeAtom(InequalityHelper.unequal, v, x);
		
		// referenceEDB(S, propertyConstant, X)
		Atom referenceEDB_SpX = Expressions.makeAtom(referenceEDB, s, propertyConstant, x);
		
		if (separators.size() == 0) {
			// violation_triple(S, I, propertyConstant, V) :-
			//	tripleEDB(S, I, propertyConstant, V),
			//	tripleEDB(O, I, propertyConstant, X),
			//	unequal (S, O)
			Rule violationTriple = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OIpX, unequal_SO);
			rules.add(violationTriple);
			
			// violation_qualifier(S, propertyConstant, V) :-
			//	qualifierEDB(S, propertyConstant, V),
			//	qualifierEDB(S, propertyConstant, X,
			// 	unequal(V, X)
			Rule violationQualifier = Expressions.makeRule(violation_qualifier_SpV, qualifierEDB_SpV, qualifierEDB_SpX, unequal_VX);
			rules.add(violationQualifier);
			
			// violation_reference(S, propertyConstant, V) :-
			//	referenceEDB(S, propertyConstant, V),
			//	referenceEDB(S, propertyConstant, X),
			// 	unequal(V, X)
			Rule violationReference = Expressions.makeRule(violation_reference_SpV, referenceEDB_SpV, referenceEDB_SpX, unequal_VX);
			rules.add(violationReference);
		} else {
			// tripleEDB(S, I, propertyConstant, C)
			Atom tripleEDB_SIpC = Expressions.makeAtom(tripleEDB, s, i, propertyConstant, c);
			
			// qualifierEDB(S, P, V)
			Atom qualifierEDB_SPV = Expressions.makeAtom(qualifierEDB, s, p, v);
			
			for (String separator : separators) {
				Constant requiredPropertyConstant = Expressions.makeConstant(separator);
				
				// unequal(P, requiredPropertyConstant)
				Atom unequal_Pr = Expressions.makeAtom(InequalityHelper.unequal, p, requiredPropertyConstant);
				
				rules.addAll(StatementNonExistenceHelper.initRequireQualifier(propertyConstant, requiredPropertyConstant, tripleEDB_SIpC, qualifierEDB_SPV, unequal_Pr));
			}
			
			// does_not_have(S, R)
			Atom does_not_have_SR = Expressions.makeAtom(does_not_have, s, r);
			
			// last_qualifier(S, P, V)
			Atom last_qualifier_SPV = Expressions.makeAtom(last_qualifier, s, p, v);
			
			// require_qualifier(S, P, V, propertyConstant, R)
			Atom require_qualifier_SPVpR = Expressions.makeAtom(require_qualifier, s, p, v, propertyConstant, r);
			
			// does_not_have(S, R) :-
			//	tripleEDB(S, I, propertyConstant, C),
			//	last_qualifier(S, P, V),
			//	require_qualifier(S, P, V, propertyConstant, R)
			Rule doesNotHave = Expressions.makeRule(does_not_have_SR, tripleEDB_SIpC, last_qualifier_SPV, require_qualifier_SPVpR);
			rules.add(doesNotHave);
			
			// has_same(S, O, Q)
			Atom has_same_SOQ = Expressions.makeAtom(has_same, s, o, q);
			
			// qualifierEDB(S, Q, V)
			Atom qualifierEDB_SQV = Expressions.makeAtom(qualifierEDB, s, q, v);
			
			// qualifierEDB(O, Q, V)
			Atom qualifierEDB_OQV = Expressions.makeAtom(qualifierEDB, o, q, v);
			
			// has_same(S, O, Q) :-
			//	tripleEDB(S, I, propertyConstant, C),
			//	tripleEDB(O, I, propertyConstant, X),
			//	qualifierEDB(S, Q, V),
			//	qualifierEDB(O, Q, V
			Rule hasSame = Expressions.makeRule(has_same_SOQ, tripleEDB_SIpC, tripleEDB_OIpX, qualifierEDB_SQV, qualifierEDB_OQV);
			rules.add(hasSame);
			
			// tripleEDB(O, I, propertyConstant, C)
			Atom tripleEDB_OIpC = Expressions.makeAtom(tripleEDB, o, i, propertyConstant, c);
			
			for (Set<String> has : Sets.powerSet(separators)) {
				Set<String> hasNot = Sets.difference(separators, has);
				
				List<Atom> conjunction = new ArrayList<>();
				conjunction.add(tripleEDB_SIpV);
				conjunction.add(tripleEDB_OIpC);
				conjunction.add(unequal_SO);
				for (String hasQualifier: has) {
					Constant hasQualifierConstant = Expressions.makeConstant(hasQualifier);
					
					// has_same(S, O, hasQualifierConstant)
					Atom has_same_SOh = Expressions.makeAtom(has_same, s, o, hasQualifierConstant);
					conjunction.add(has_same_SOh);
				}
				for (String hasNotQualifier: hasNot) {
					Constant hasNotQualifierConstant = Expressions.makeConstant(hasNotQualifier);
					
					// does_not_have(S, hasNotQualifierConstant)
					Atom does_not_have_Sh = Expressions.makeAtom(does_not_have, s, hasNotQualifierConstant);
					conjunction.add(does_not_have_Sh);
					
					// does_not_have(O, hasNotQualifierConstant)
					Atom does_not_have_Oh = Expressions.makeAtom(does_not_have, s, hasNotQualifierConstant);
					conjunction.add(does_not_have_Oh);
				}
				
				// violation_triple(S, I, propertyConstant, V :-
				//	tripleEDB(S, I, propertyConstant, V),
				//	tripleEDB(O, I, propertyConstant, C),
				//	unequal(S, O),
				//	has_same(S, O, {X}),
				//	does_not_have(S, {Y}),
				//	does_not_have(O, {Y})
				Rule violation = Expressions.makeRule(violation_triple_SIpV, conjunction.toArray(new Atom[conjunction.size()]));
				rules.add(violation);
			}
			
		}

		return rules;
	}
}