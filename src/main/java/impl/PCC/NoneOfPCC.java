package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.TS.ScopeTS;
import impl.TS.TripleSet;
import utility.PrepareQueriesException;
import utility.Utility;

public class NoneOfPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(NoneOfPCC.class);
	
	final TripleSet tripleSet;
	
	final Set<String> qualifiers;

	public NoneOfPCC(String property_, Set<String> qualifiers_) throws IOException {
		super(property_);
		qualifiers = qualifiers_;
		tripleSet = new ScopeTS(property);
	}

	@Override
	public String violations() throws IOException {
		if (!tripleSet.notEmpty())
			return "";
		
		try {
			loadTripleSets(tripleSet);
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}

		List<Rule> rules = new ArrayList<Rule>();
		
		
		for (String notAllowedValue : qualifiers) {
			
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
			
			// violation_reference(S, propertyConstant, confValueConstant)
			Atom violation_reference_Spc = Expressions.makeAtom(violation_reference, s, propertyConstant, confValueConstant);
			
			// referenceEDB(S, propertyConstant, confValueConstant)
			Atom referenceEDB_Spc = Expressions.makeAtom(referenceEDB, s, propertyConstant, confValueConstant);
			
			// violation_reference(S, propertyConstant, confValueConstant) :- referenceEDB(S, propertyConstant, confValueConstant)
			Rule conflict_reference = Expressions.makeRule(violation_reference_Spc, referenceEDB_Spc);
			
			rules.add(conflict_reference);
		}
		
		try {
			return prepareAndExecuteQueries(rules, Arrays.asList(violation_triple_query, violation_qualifier_query, violation_reference_query));
		} catch (PrepareQueriesException e) {
			return e.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
