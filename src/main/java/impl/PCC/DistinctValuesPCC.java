package impl.PCC;

import static utility.SC.o;
import static utility.SC.s;
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

public class DistinctValuesPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(DistinctValuesPCC.class);

	public DistinctValuesPCC(String property_) throws IOException {
		super(property_);
	}

	@Override
	public Set<Rule> rules() {
		Set<Rule> rules = new HashSet<Rule>();
		
		// statementEDB(O, X, propertyConstant, V)
		Atom statementEDB_OXpV = Expressions.makeAtom(statementEDB, o, x, propertyConstant, v);
		
		// unequal(S, O)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, s, o);
		
		//violation_statement(S, I, propertyConstant, V) :-
		//	statementEDB(S, I, propertyConstant, V),
		//	statementEDB(O, X, propertyConstant, V),
		//	unequal(S, O)
		Rule violation = Expressions.makeRule(violation_statement_SIpV, statementEDB_SIpV, statementEDB_OXpV, unequal_SO);
		
		rules.add(violation);
		
		return rules;
	}
}
