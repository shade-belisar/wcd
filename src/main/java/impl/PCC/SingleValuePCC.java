package impl.PCC;

import static utility.SC.c;
import static utility.SC.g;
import static utility.SC.i;
import static utility.SC.last_qualifier;
import static utility.SC.o;
import static utility.SC.p;
import static utility.SC.possible_violation;
import static utility.SC.q;
import static utility.SC.qualifierEDB;
import static utility.SC.r;
import static utility.SC.referenceEDB;
import static utility.SC.require_qualifier;
import static utility.SC.s;
import static utility.SC.same_or_non_existent;
import static utility.SC.statementEDB;
import static utility.SC.v;
import static utility.SC.x;
import static utility.SC.y;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

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
	public Set<Rule> rules() {
		Set<Rule> rules = new HashSet<Rule>();
		
		// statementEDB(O, I, propertyConstant, X)
		Atom statementEDB_OIpX = Expressions.makeAtom(statementEDB, o, i, propertyConstant, x);
		
		// unequal(S, O)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, s, o);
		
		// qualifierEDB(S, propertyConstant, V)
		Atom qualifierEDB_SpV = Expressions.makeAtom(qualifierEDB, s, propertyConstant, v);
		
		// qualifierEDB(S, propertyConstant, X)
		Atom qualifierEDB_SpX = Expressions.makeAtom(qualifierEDB, s, propertyConstant, x);
		
		// unequal(V, X)
		Atom unequal_VX = Expressions.makeAtom(InequalityHelper.unequal, v, x);
		
		// referenceEDB(S, G, propertyConstant, X)
		Atom referenceEDB_SGpX = Expressions.makeAtom(referenceEDB, s, g, propertyConstant, x);
		
		// possible_violation(S, O)
		Atom possible_violations_SO = Expressions.makeAtom(possible_violation, s, o);
		
		// possible_violation(S, O) :-
		//	statementEDB(S, I, propertyConstant, V),
		//	statementEDB(O, I, propertyConstant, X),
		//	unequal(S, O)
		Rule possibleViolations = Expressions.makeRule(possible_violations_SO, statementEDB_SIpV, statementEDB_OIpX, unequal_SO);
		rules.add(possibleViolations);
		
		if (separators.size() == 0) {
			// violation_statement(S, I, propertyConstant, V) :-
			//	possible_violation(S, O),
			//	statementEDB(S, I, propertyConstant, V)
			Rule violationStatement = Expressions.makeRule(violation_statement_SIpV, possible_violations_SO, statementEDB_SIpV);
			rules.add(violationStatement);
			
			// violation_qualifier(S, propertyConstant, V) :-
			//	qualifierEDB(S, propertyConstant, V),
			//	qualifierEDB(S, propertyConstant, X),
			// 	unequal(V, X)
			Rule violationQualifier = Expressions.makeRule(violation_qualifier_SpV, qualifierEDB_SpV, qualifierEDB_SpX, unequal_VX);
			rules.add(violationQualifier);
			
			// violation_reference(S, H, propertyConstant, V) :-
			//	referenceEDB(S, H, propertyConstant, V),
			//	referenceEDB(S, G, propertyConstant, X),
			// 	unequal(V, X)
			Rule violationReference = Expressions.makeRule(violation_reference_SHpV, referenceEDB_SHpV, referenceEDB_SGpX, unequal_VX);
			rules.add(violationReference);
		} else {			
			// statementEDB(S, I, propertyConstant, C)
			Atom statementEDB_SIpC = Expressions.makeAtom(statementEDB, s, i, propertyConstant, c);
			
			// qualifierEDB(S, P, V)
			Atom qualifierEDB_SPV = Expressions.makeAtom(qualifierEDB, s, p, v);
			
			for (String separator : separators) {
				Constant requiredSeparatorConstant = Expressions.makeConstant(separator);
				
				// unequal(P, requiredPropertyConstant)
				Atom unequal_Pr = Expressions.makeAtom(InequalityHelper.unequal, p, requiredSeparatorConstant);
				
				rules.addAll(StatementNonExistenceHelper.initRequireQualifier(requiredSeparatorConstant, statementEDB_SIpC, qualifierEDB_SPV, unequal_Pr));
			}
			
			// same_or_non_existent(S, O, Q)
			Atom same_or_non_existent_SOQ = Expressions.makeAtom(same_or_non_existent, s, o, q);
						
			// qualifierEDB(S, Q, V)
			Atom qualifierEDB_SQV = Expressions.makeAtom(qualifierEDB, s, q, v);
			
			// qualifierEDB(O, Q, V)
			Atom qualifierEDB_OQV = Expressions.makeAtom(qualifierEDB, o, q, v);
			
			// same_or_non_existent(S, O, Q) :-
			//	possible_violation(S, O),
			//	qualifierEDB(S, Q, V), qualifierEDB(O, Q, V)
			Rule same = Expressions.makeRule(same_or_non_existent_SOQ, possible_violations_SO, qualifierEDB_SQV, qualifierEDB_OQV);
			rules.add(same);
			
			// last_qualifier(S, P, V)
			Atom last_qualifier_SPV = Expressions.makeAtom(last_qualifier, s, p, v);
			
			// last_qualifier(O, R, C)
			Atom last_qualifier_ORC = Expressions.makeAtom(last_qualifier, o, r, c);
			
			// require_qualifier(S, P, V, Q)
			Atom require_SPVQ = Expressions.makeAtom(require_qualifier, s, p, v, q);
			
			// require_qualifier(O, R, C, Q)
			Atom require_ORCQ = Expressions.makeAtom(require_qualifier, o, r, c, q);
			
			// same_or_non_existent(S, O, Q) :-
			//	possible_violation(S, O),
			//	last_qualifier(S, P, V),
			//	require_qualifier(S, P, V, Q),
			//	last_qualifier(O, R, C),
			//	require_qualifier(O, R, C, Q)
			Rule non_existent = Expressions.makeRule(same_or_non_existent_SOQ, possible_violations_SO, last_qualifier_SPV, require_SPVQ, last_qualifier_ORC, require_ORCQ);
			rules.add(non_existent);
			
			List<Atom> body = new ArrayList<>();
			
			body.add(statementEDB_SIpV);
			body.add(statementEDB_OIpX);
			body.add(unequal_SO);
			
			
			for (String separator : separators) {
				Constant separatorConstant = Expressions.makeConstant(separator);
				 
				Atom same_or_non_existent_SOs = Expressions.makeAtom(same_or_non_existent, s, o, separatorConstant);
				body.add(same_or_non_existent_SOs);
			}
			
			Rule violation = Expressions.makeRule(violation_statement_SIpV, body.toArray(new Atom[body.size()]));
			rules.add(violation);			
		}

		return rules;
	}
}