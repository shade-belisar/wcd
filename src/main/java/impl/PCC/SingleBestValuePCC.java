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
import static utility.SC.rank;
import static utility.SC.require_qualifier;
import static utility.SC.s;
import static utility.SC.statementEDB;
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
import org.wikidata.wdtk.datamodel.interfaces.StatementRank;

import com.google.common.collect.Sets;

import utility.InequalityHelper;
import utility.StatementNonExistenceHelper;

public class SingleBestValuePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SingleBestValuePCC.class);
	
	final Set<String> separators;

	public SingleBestValuePCC(String property_, Set<String> separators_) throws IOException {
		super(property_);
		separators = separators_;
	}

	@Override
	public List<Rule> rules() { 	
		List<Rule> rules = new ArrayList<Rule>();
		
		Constant preferredRankConstant = Expressions.makeConstant(StatementRank.PREFERRED.toString());
		
		// rank(S, preferredRankConstant)
		Atom rank_Sp = Expressions.makeAtom(rank, s, preferredRankConstant);
		
		// statementEDB(O, I, propertyConstant, X)
		Atom statementEDB_OIpX = Expressions.makeAtom(statementEDB, o, i, propertyConstant, x);
		
		// rank(O, preferredRankConstant)
		Atom rank_Op = Expressions.makeAtom(rank, o, preferredRankConstant);
		
		// unequal(S, O)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, s, o);
		
		if (separators.size() == 0) {
			// violation_statement(S, I, propertyConstant, V) :-
			//	statementEDB(S, I, propertyConstant, V),
			//	rank(S, preferredRankConstant),
			//	statementEDB(O, I, propertyConstant, X),
			//	rank(O, preferredRankConstant),
			//	unequal (S, O)
			Rule violationStatement = Expressions.makeRule(violation_statement_SIpV, statementEDB_SIpV, rank_Sp, statementEDB_OIpX, rank_Op, unequal_SO);
			rules.add(violationStatement);
		} else {
			// statementEDB(S, I, propertyConstant, C)
			Atom statementEDB_SIpC = Expressions.makeAtom(statementEDB, s, i, propertyConstant, c);
			
			// qualifierEDB(S, P, V)
			Atom qualifierEDB_SPV = Expressions.makeAtom(qualifierEDB, s, p, v);
			
			for (String separator : separators) {
				Constant requiredPropertyConstant = Expressions.makeConstant(separator);
				
				// unequal(P, requiredPropertyConstant)
				Atom unequal_Pr = Expressions.makeAtom(InequalityHelper.unequal, p, requiredPropertyConstant);
				
				rules.addAll(StatementNonExistenceHelper.initRequireQualifier(propertyConstant, requiredPropertyConstant, statementEDB_SIpC, qualifierEDB_SPV, unequal_Pr));
			}
			
			// does_not_have(S, R)
			Atom does_not_have_SR = Expressions.makeAtom(does_not_have, s, r);
			
			// last_qualifier(S, P, V)
			Atom last_qualifier_SPV = Expressions.makeAtom(last_qualifier, s, p, v);
			
			// require_qualifier(S, P, V, propertyConstant, R)
			Atom require_qualifier_SPVpR = Expressions.makeAtom(require_qualifier, s, p, v, propertyConstant, r);
			
			// does_not_have(S, R) :-
			//	statementEDB(S, I, propertyConstant, C),
			//	last_qualifier(S, P, V),
			//	require_qualifier(S, P, V, propertyConstant, R)
			Rule doesNotHave = Expressions.makeRule(does_not_have_SR, statementEDB_SIpC, last_qualifier_SPV, require_qualifier_SPVpR);
			rules.add(doesNotHave);
			
			// has_same(S, O, Q)
			Atom has_same_SOQ = Expressions.makeAtom(has_same, s, o, q);
			
			// qualifierEDB(S, Q, V)
			Atom qualifierEDB_SQV = Expressions.makeAtom(qualifierEDB, s, q, v);
			
			// qualifierEDB(O, Q, V)
			Atom qualifierEDB_OQV = Expressions.makeAtom(qualifierEDB, o, q, v);
			
			// has_same(S, O, Q) :-
			//	statementEDB(S, I, propertyConstant, C),
			//	statementEDB(O, I, propertyConstant, X),
			//	qualifierEDB(S, Q, V),
			//	qualifierEDB(O, Q, V
			Rule hasSame = Expressions.makeRule(has_same_SOQ, statementEDB_SIpC, statementEDB_OIpX, qualifierEDB_SQV, qualifierEDB_OQV);
			rules.add(hasSame);
			
			// statementEDB(O, I, propertyConstant, C)
			Atom statementEDB_OIpC = Expressions.makeAtom(statementEDB, o, i, propertyConstant, c);
			
			for (Set<String> has : Sets.powerSet(separators)) {
				Set<String> hasNot = Sets.difference(separators, has);
				
				List<Atom> conjunction = new ArrayList<>();
				conjunction.add(statementEDB_SIpV);
				conjunction.add(rank_Sp);
				conjunction.add(statementEDB_OIpC);
				conjunction.add(rank_Op);
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
				
				// violation_statement(S, I, propertyConstant, V :-
				//	statementEDB(S, I, propertyConstant, V),
				//	rank(S, preferredRankConstant),
				//	statementEDB(O, I, propertyConstant, C),
				//	rank(O, preferredRankConstant)
				//	unequal(S, O),
				//	has_same(S, O, {X}),
				//	does_not_have(S, {Y}),
				//	does_not_have(O, {Y})
				Rule violation = Expressions.makeRule(violation_statement_SIpV, conjunction.toArray(new Atom[conjunction.size()]));
				rules.add(violation);
			}
			
		}

		return rules;
	}
}