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
import static utility.SC.qualifierEDB;
import static utility.SC.referenceEDB;

import static utility.SC.s;
import static utility.SC.o;
import static utility.SC.i;
import static utility.SC.v;
import static utility.SC.c;
import static utility.SC.x;

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
			
			// Missing: separators
		}

		return rules;
	}
}