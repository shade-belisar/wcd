package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import utility.InequalityHelper;
import static utility.SC.tripleEDB;

import static utility.SC.s;
import static utility.SC.o;
import static utility.SC.i;
import static utility.SC.c;

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
		
		// unequal (S, O)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, s, o);

		if (separators.size() == 0) {
			// violation_triple(S, I, propertyConstant, V) :-
			//	tripleEDB(S, I, propertyConstant, V),
			//	tripleEDB(O, I, propertyConstant, C),
			//	unequal (S, O)
			Rule conflict = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OIpC, unequal_SO);
			
			rules.add(conflict);
			
		}
		
		// Missing: separators
		
		return rules;
	}
}