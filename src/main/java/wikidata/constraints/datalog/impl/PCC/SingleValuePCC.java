package wikidata.constraints.datalog.impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Conjunction;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import wikidata.constraints.datalog.impl.TS.PropertyAsPredicateWithQualifiersTS;
import wikidata.constraints.datalog.impl.TS.TripleSet;
import wikidata.constraints.datalog.utility.InequalityHelper;
import wikidata.constraints.datalog.utility.PrepareQueriesException;
import wikidata.constraints.datalog.utility.Utility;

public class SingleValuePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SingleValuePCC.class);
	
	final PropertyAsPredicateWithQualifiersTS tripleSet;
	
	final Set<String> separators;

	public SingleValuePCC(String property_, Set<String> separators_) throws IOException {
		super(property_);
		separators = separators_;
		tripleSet = new PropertyAsPredicateWithQualifiersTS(property, separators);
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
		
		try {
			InequalityHelper.addUnequalConstantsToReasoner(reasoner, tripleSet.getStatementIDs());
		} catch (ReasonerStateException e) {
			logger.error("Trying to add unequal constants to reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		List<Rule> rules = new ArrayList<Rule>();
		
		// violation_long(STATEMENT, X, propertyConstant, Y)
		Atom violation_long_SXpY = Expressions.makeAtom(violation_long, statement, x, propertyConstant, y);
		
		// tripleEDB(STATEMENT, X, propertyConstant, Y)
		Atom tripleEDB_SXpY = Expressions.makeAtom(tripleEDB, statement, x, propertyConstant, y);
		
		// tripleEDB(OTHER_STATEMENT, X, propertyConstant, z)
		Atom tripleEDB_PXpZ = Expressions.makeAtom(tripleEDB, otherStatement, x, propertyConstant, z);
		
		// unequal (STATEMENT, OTHER_STATEMENT)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal_IDB, statement, otherStatement);
		
		if (separators.size() == 0) {
			/*
			 * violation_long(STATEMENT, X, propertyConstant, Y) :-
			 * 	tripleEDB(STATEMENT, X, propertyConstant, Y),
			 * 	tripleEDB(OTHER_STATEMENT, X, propertyConstant, z),
			 * 	unequal (STATEMENT, OTHER_STATEMENT)
			 */
			Rule conflict = Expressions.makeRule(violation_long_SXpY, tripleEDB_SXpY, tripleEDB_PXpZ, unequal_SO);
			
			rules.add(conflict);
			
		} else {
			int i = 0;
			
			// violation_long(STATEMENT, X, propertyConstant, Y)
			Conjunction head = Expressions.makeConjunction(violation_long_SXpY);
			
			List<Atom> forBody = new ArrayList<Atom>();
			
			// tripleEDB(STATEMENT, X, propertyConstant, Y)
			forBody.add(tripleEDB_SXpY);
			
			// tripleEDB(OTHER_STATEMENT, X, propertyConstant, Z)
			forBody.add(tripleEDB_PXpZ);
			
			// unequal (STATEMENT, OTHER_STATEMENT)
			forBody.add(unequal_SO);
			
			for (String separator : separators) {
				Constant separatorConstant = Expressions.makeConstant(separator);
				Variable separatorValueVariable = Expressions.makeVariable("separatorValue" + i);
				
				// qualifierEDB(STATEMENT, <separatorConstant>, <separatorValueVariable>)
				Atom qualifierEDB_Ssv = Expressions.makeAtom(qualifierEDB, statement, separatorConstant, separatorValueVariable);
				
				forBody.add(qualifierEDB_Ssv);
				
				// qualifierEDB(OTHER_STATEMENT, <separator>, <separatorValue>)
				Atom qualifierEDB_Osv = Expressions.makeAtom(qualifierEDB, statement, separatorConstant, separatorValueVariable);
				
				forBody.add(qualifierEDB_Osv);
				
				i++;
			}
			
			Conjunction body = Expressions.makeConjunction(forBody);

			/*
			 * violation_long(STATEMENT, X, propertyConstant, Y) :-
			 * 	tripleEDB(STATEMENT, X, propertyConstant, Y),
			 * 	tripleEDB(OTHER_STATEMENT, X, propertyConstant, Z),
			 * 	unequal (STATEMENT, OTHER_STATEMENT),
			 * 	qualifierEDB(STATEMENT, <separator>, <separatorValue>),
			 * 	qualifierEDB(OTHER_STATEMENT, <separator>, <separatorValue>) 
			 */
			Rule conflict = Expressions.makeRule(head, body);
			
			rules.add(conflict);
		}
		
		try {
			return prepareAndExecuteQueries(rules);
		} catch (PrepareQueriesException e) {
			return e.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
