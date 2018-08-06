package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.TS.DistinctValuesTS;
import impl.TS.TripleSet;
import utility.InequalityHelper;
import utility.PrepareQueriesException;

import static utility.SC.tripleEDB;

import static utility.SC.s;
import static utility.SC.o;
import static utility.SC.x;
import static utility.SC.v;

public class DistinctValuesPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(DistinctValuesPCC.class);

	public DistinctValuesPCC(String property_) throws IOException {
		super(property_);
	}

	@Override
	public List<Rule> rules() {
		List<Rule> rules = new ArrayList<Rule>();
		
		// tripleEDB(O, X, propertyConstant, V)
		Atom tripleEDB_OXpV = Expressions.makeAtom(tripleEDB, o, x, propertyConstant, v);
		
		// unequal(S, O)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, s, o);
		
		//violation_triple(S, I, propertyConstant, V) :-
		//	tripleEDB(S, I, propertyConstant, V),
		//	tripleEDB(O, X, propertyConstant, V),
		//	unequal(S, O)
		Rule violation = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OXpV, unequal_SO);
		
		rules.add(violation);
		
		return rules;
	}
}
