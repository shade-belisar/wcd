package impl.PCC;

import static utility.SC.o;
import static utility.SC.q;
import static utility.SC.qualifierEDB;
import static utility.SC.s;

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
import utility.Utility;

public class AllowedQualifiersPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(AllowedQualifiersPCC.class);
	
	final Set<String> allowedQualifiers;

	public AllowedQualifiersPCC(String property_, Set<String> allowedQualifiers_) throws IOException {
		super(property_);
		allowedQualifiers = allowedQualifiers_;
	}

	@Override
	public Set<Rule> rules() {
		Set<Rule> rules = new HashSet<Rule>();
		
		// qualifierEDB(S, Q, O)
		Atom qualfierEDB_SQO = Expressions.makeAtom(qualifierEDB, s, q, o);
		
		List<Atom> violation_conjunction = new ArrayList<Atom>();
		violation_conjunction.add(statementEDB_SIpV);
		violation_conjunction.add(qualfierEDB_SQO);
		for (String allowedQualifier : allowedQualifiers) {
			Constant allowedQualifierConstant = Expressions.makeConstant(allowedQualifier);
			
			// unequal({A}, Q)
			Atom unequal_AQ = Expressions.makeAtom(InequalityHelper.unequal, allowedQualifierConstant, q);
			violation_conjunction.add(unequal_AQ);
		}
		
		// violation_statement(S, I, propertyConstant, V) :- statementEDB(S, I, propertyConstant, V), qualifierEDB(S, Q, O), unequal({A}, Q)
		Rule violation = Expressions.makeRule(violation_statement_SIpV, Utility.toArray(violation_conjunction));
		rules.add(violation);

		return rules;
	}
}
