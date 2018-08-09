package impl.PCC;

import static utility.SC.c;
import static utility.SC.does_not_have;
import static utility.SC.i;
import static utility.SC.last_qualifier;
import static utility.SC.o;
import static utility.SC.p;
import static utility.SC.qualifierEDB;
import static utility.SC.r;
import static utility.SC.referenceEDB;
import static utility.SC.require_qualifier;
import static utility.SC.s;
import static utility.SC.tripleEDB;
import static utility.SC.v;
import static utility.SC.w;
import static utility.SC.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.api.Variable;
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
		
		// tripleEDB(O, I, propertyConstant, C)
		Atom tripleEDB_OIpC = Expressions.makeAtom(tripleEDB, o, i, propertyConstant, c);
		
		// unequal(S, O)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, s, o);
		
		// qualifierEDB(S, propertyConstant, X)
		Atom qualifierEDB_SpX = Expressions.makeAtom(qualifierEDB, o, propertyConstant, x);
		
		// unequal(V, X)
		Atom unequal_VX = Expressions.makeAtom(InequalityHelper.unequal, v, x);
		
		// referenceEDB(S, propertyConstant, X)
		Atom referenceEDB_SpX = Expressions.makeAtom(referenceEDB, s, propertyConstant, x);

		if (separators.size() == 0) {
			// violation_triple(S, I, propertyConstant, V) :-
			//	tripleEDB(S, I, propertyConstant, V),
			//	tripleEDB(O, I, propertyConstant, C),
			//	unequal (S, O)
			Rule violationTriple = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OIpC, unequal_SO);
			rules.add(violationTriple);
			
			// violation_qualifier(S, propertyConstant, V) :-
			//	qualifierEDB(S, propertyConstant, V),
			//	qualifierEDB(S, propertyConstant, X,
			// 	unequal(V, X)
			Rule violationQualifier = Expressions.makeRule(violation_qualifier_SpV, qualifierEDB_SpV, qualifierEDB_SpX, unequal_VX);
			rules.add(violationQualifier);
			
			// violation_reference(S, propertyConstant, V) :-
			//	referenceEDB(S, propertyConstant, V),
			//	referenceEDB(S, propertyConstant, X,
			// 	unequal(V, X)
			Rule violationReference = Expressions.makeRule(violation_reference_SpV, referenceEDB_SpV, referenceEDB_SpX, unequal_VX);
			rules.add(violationReference);
			
		} else {
			// qualifierEDB(S, P, V)
			Atom qualifierEDB_SPV = Expressions.makeAtom(qualifierEDB, s, p, v);
			
			for (String separator : separators) {
				Term requiredPropertyConstant = Expressions.makeConstant(separator);

				// unequal(P, requiredPropertyConstant
				Atom unequal_Pr = Expressions.makeAtom(InequalityHelper.unequal, p, requiredPropertyConstant);
				
				rules.addAll(StatementNonExistenceHelper.initRequireQualifier(requiredPropertyConstant, qualifierEDB_SPV, unequal_Pr));
			}
			
			// does_not_have(S, R)
			Atom does_not_have_SR = Expressions.makeAtom(does_not_have, s, r);
			
			// last_qualifier(S, P, V)
			Atom last_qualifier_SPV = Expressions.makeAtom(last_qualifier, s, p, v);
			
			// require_qualifer(S, P, V, R)
			Atom require_qualifier_SPVR = Expressions.makeAtom(require_qualifier, s, p, v, r);
			
			// does_not_have(S, R) :- last_qualifier(S, P, V), require_qualifer(S, P, V, R)
			Rule doesNotHave = Expressions.makeRule(does_not_have_SR, last_qualifier_SPV, require_qualifier_SPVR);
			rules.add(doesNotHave);
			
			// tripleEDB(O, I, propertyConstant, W)
			Atom tripleEDB_OIpW = Expressions.makeAtom(tripleEDB, o, i, propertyConstant, w);
			
			for (Set<String> has : Sets.powerSet(separators)) {
				Set<String> hasNot = Sets.difference(separators, has);
				
				List<Atom> conjunction = new ArrayList<Atom>();
				conjunction.add(tripleEDB_SIpV);
				conjunction.add(tripleEDB_OIpW);
				conjunction.add(unequal_SO);
				int i = 0;
				for (String hasQualifier : has) {
					Constant hasQualifierConstant = Expressions.makeConstant(hasQualifier);
					Variable hasQualifierVariable = Expressions.makeVariable("variable" + i);
					i++;
					
					// qualifierEDB(S, {X}, [X]}
					Atom qualifierEBD_SXX = Expressions.makeAtom(qualifierEDB, s, hasQualifierConstant, hasQualifierVariable);
					conjunction.add(qualifierEBD_SXX);
					
					// qualifierEDB(O, {X}, [X]}
					Atom qualifierEBD_OXX = Expressions.makeAtom(qualifierEDB, o, hasQualifierConstant, hasQualifierVariable);
					//conjunction.add(qualifierEBD_OXX);
				}
				for (String hasNotQualifier : hasNot) {
					
					Constant hasNotQualifierConstant = Expressions.makeConstant(hasNotQualifier);
					
					// does_not_have(S, {Y})
					Atom does_not_have_SY = Expressions.makeAtom(does_not_have, s, hasNotQualifierConstant);
					conjunction.add(does_not_have_SY);
					
					// does_not_have(O, {Y})
					Atom does_not_have_OY = Expressions.makeAtom(does_not_have, o, hasNotQualifierConstant);
					conjunction.add(does_not_have_OY);
				}
				// violation_triple(S, I, propertyConstant, V) :-
				//	tripleEDB(S, I, propertyConstant, V),
				//	tripleEDB(O, I, propertyConstant, W),
				//	unequal(S, O),
				//	qualifierEDB(S, {X}, [X]),
				//	qualifierEDB(O, {X}, [X]),
				//	does_not_have(S, {Y}),
				//	does_not_have(O, {Y})
				Rule violation = Expressions.makeRule(violation_triple_SIpV, toArray(conjunction));
				if (hasNot.size() > 0)
					continue;
				rules.add(violation);
			}
		}

		return rules;
	}
}