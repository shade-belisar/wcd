package impl.PCC;

import static utility.SC.h;
import static utility.SC.i;
import static utility.SC.qualifierEDB;
import static utility.SC.referenceEDB;
import static utility.SC.s;
import static utility.SC.tripleEDB;
import static utility.SC.violation_qualifier;
import static utility.SC.violation_reference;
import static utility.SC.violation_triple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import utility.Utility;

public class NoneOfPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(NoneOfPCC.class);
	
	final Set<String> forbiddenValues;

	public NoneOfPCC(String property_, Set<String> qualifiers_) throws IOException {
		super(property_);
		forbiddenValues = qualifiers_;
	}

	@Override
	public List<Rule> rules() {
		List<Rule> rules = new ArrayList<Rule>();
		
		
		for (String notAllowedValue : forbiddenValues) {
			
			Constant confValueConstant =  Utility.makeConstant(notAllowedValue);
			
			// violation_triple(S, I, propertyConstant, confValueConstant)
			Atom violation_triple_SIpc = Expressions.makeAtom(violation_triple, s, i, propertyConstant, confValueConstant);
			
			// tripleEDB(S, I, propertyConstant, confValueConstant)
			Atom tripleEDB_SIpc = Expressions.makeAtom(tripleEDB, s, i, propertyConstant, confValueConstant);
			
			// violation_triple(S, I, propertyConstant, confValueConstant) :- tripleEDB(S, I, propertyConstant, confValueConstant)
			Rule conflict_triple = Expressions.makeRule(violation_triple_SIpc, tripleEDB_SIpc);
			
			rules.add(conflict_triple);
			
			// violation_qualifier(S, propertyConstant, confValueConstant)
			Atom violation_qualifier_Spc = Expressions.makeAtom(violation_qualifier, s, propertyConstant, confValueConstant);
			
			// qualifierEDB(S, propertyConstant, confValueConstant)
			Atom qualifierEDB_Spc = Expressions.makeAtom(qualifierEDB, s, propertyConstant, confValueConstant);
			
			// violation_qualifier(S, propertyConstant, confValueConstant) :- qualifierEDB(S, propertyConstant, confValueConstant)
			Rule conflict_qualifier = Expressions.makeRule(violation_qualifier_Spc, qualifierEDB_Spc);
			
			rules.add(conflict_qualifier);
			
			// violation_reference(S, H, propertyConstant, confValueConstant)
			Atom violation_reference_SHpc = Expressions.makeAtom(violation_reference, s, h, propertyConstant, confValueConstant);
			
			// referenceEDB(S, H, propertyConstant, confValueConstant)
			Atom referenceEDB_SHpc = Expressions.makeAtom(referenceEDB, s, h, propertyConstant, confValueConstant);
			
			// violation_reference(S, H, propertyConstant, confValueConstant) :- referenceEDB(S, H, propertyConstant, confValueConstant)
			Rule conflict_reference = Expressions.makeRule(violation_reference_SHpc, referenceEDB_SHpc);
			
			rules.add(conflict_reference);
		}
		
		return rules;
	}
}