package impl.PCC;

import static utility.SC.c;
import static utility.SC.last_qualifier;
import static utility.SC.p;
import static utility.SC.q;
import static utility.SC.qualifierEDB;
import static utility.SC.require_qualifier;
import static utility.SC.s;
import static utility.SC.v;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import utility.InequalityHelper;
import utility.StatementNonExistenceHelper;

public class MandatoryQualifierPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(MandatoryQualifierPCC.class);
	
	final Set<String> requiredQualifiers;

	public MandatoryQualifierPCC(String property_, Set<String> requiredQualifiers_) throws IOException {
		super(property_);
		requiredQualifiers = requiredQualifiers_;
	}

	@Override
	public List<Rule> rules() {
		List<Rule> rules = new ArrayList<Rule>();
		
		// qualifierEDB(S, P, V)
		Atom qualifierEDB_SPV = Expressions.makeAtom(qualifierEDB, s, p, v);
		
		for (String requiredQualifier : requiredQualifiers) {
			Constant requiredPropertyConstant = Expressions.makeConstant(requiredQualifier);
			
			// unequal(P, requiredPropertyConstant)
			Atom unequal_Pr = Expressions.makeAtom(InequalityHelper.unequal, p, requiredPropertyConstant);
			
			rules.addAll(StatementNonExistenceHelper.initRequireQualifier(requiredPropertyConstant, qualifierEDB_SPV, unequal_Pr));
			
			// last_qualifier(S, Q, C)
			Atom last_qualifier_SPV = Expressions.makeAtom(last_qualifier, s, q, c);
			
			// require_qualifier(S, Q, C, requiredPropertyConstant)
			Atom require_qualifier_SQCr = Expressions.makeAtom(require_qualifier, s, q, c, requiredPropertyConstant);
			
			// violation_triple(S, I, propertyConstant, V) :-
			//	tripleEDB(S, I, propertyConstant, V),
			//	last_qualifier(S, Q, C),
			//	require_qualifier(S, Q, C, requiredPropertyConstant)
			Rule violation = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, last_qualifier_SPV, require_qualifier_SQCr);
			rules.add(violation);
		}
		
		return rules;
	}

}
