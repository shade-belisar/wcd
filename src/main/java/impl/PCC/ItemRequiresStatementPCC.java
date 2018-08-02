package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Conjunction;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;

import impl.TS.ItemRequiresStatementTS;
import impl.TS.TripleSet;
import utility.InequalityHelper;
import utility.PrepareQueriesException;
import utility.Utility;

public class ItemRequiresStatementPCC extends PropertyConstraintChecker {
	
	final ItemRequiresStatementTS tripleSet;
	
	final Map<String, HashSet<String>> requirements;
	
	final String OTHER_PROPERTY = "otherProperty";
	final String PREVIOUS_STATEMENT = "previousStatement";
	
	final String REQUIRE = "require";
	
	final String FIRST = "first";
	final String NEXT = "next";
	final String LAST = "last";
	
	final String VIOLATION_ITEM_PROPERTY = "violation_item_property";
	
	final Variable otherProperty = Expressions.makeVariable(OTHER_PROPERTY);
	final Variable previousStatement = Expressions.makeVariable(PREVIOUS_STATEMENT);
	
	final Predicate require = Expressions.makePredicate(REQUIRE, 2);
	
	final Predicate first = Expressions.makePredicate(FIRST, 2);
	final Predicate next = Expressions.makePredicate(NEXT, 2);
	final Predicate last = Expressions.makePredicate(LAST, 2);
	
	final Predicate violation_item_property = Expressions.makePredicate(VIOLATION_ITEM_PROPERTY, 2);

	public ItemRequiresStatementPCC(String property_, Map<String, HashSet<String>> qualifiers) throws IOException {
		super(property_);
		tripleSet = new ItemRequiresStatementTS(property);
		requirements = qualifiers;
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
		
		// PREPARATION
		try {
			if (tripleSet.firstNotEmpty()) {
				final DataSource firstEDBPath = new CsvFileDataSource(tripleSet.getFirstFile());
				reasoner.addFactsFromDataSource(first, firstEDBPath);
			}
			if (tripleSet.nextNotEmpty()) {
				final DataSource nextEDBPath = new CsvFileDataSource(tripleSet.getNextFile());
				reasoner.addFactsFromDataSource(next, nextEDBPath);
			}
			if (tripleSet.lastNotEmpty()) {
				final DataSource lastEDBPath = new CsvFileDataSource(tripleSet.getLastFile());
				reasoner.addFactsFromDataSource(last, lastEDBPath);
			}
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
		}
		
		String inequalityError = "Trying to add unequal constants to reasoner in the wrong state for property " + property + ".";
		
		InequalityHelper.setOrReset(reasoner);
		
		for (Set<String> propertiesForOneItem : tripleSet.getProperties()) {
			for(String requiredProperty : requirements.keySet()) {
				propertiesForOneItem.add(Utility.BASE_URI + requiredProperty);
			}
			try {
				InequalityHelper.addUnequalConstantsToReasoner(propertiesForOneItem);
			} catch (ReasonerStateException e) {
				logger.error(inequalityError, e);
				return internalError;
			}
		}
		
		for (Map.Entry<String, HashSet<String>> entry : requirements.entrySet()) {
			
			String requiredProperty = entry.getKey();
			
			Set<String> allowedValues = entry.getValue();
			
			Constant requiredPropertyConstant = Utility.makeConstant(requiredProperty);
			
			// require(STATEMENT, requiredProperty)
			Atom require_Sr = Expressions.makeAtom(require, statement, requiredPropertyConstant);
			
			// first(STATEMENT, X)
			Atom first_Sx = Expressions.makeAtom(first, statement, x);
			
			// tripleEDB(STATEMENT, X, OTHER_PROPERTY, Y)
			Atom tripleEDB_SXOY = Expressions.makeAtom(tripleEDB, statement, x, otherProperty, y);
			
			// unequal(requiredProperty, OTHER_PROPERTY)
			Atom unequal_rO = Expressions.makeAtom(InequalityHelper.unequal, requiredPropertyConstant, otherProperty);
			
			// require(STATEMENT, requiredProperty) :- first(STATEMENT, X), tripleEDB(STATEMENT, X, OTHER_PROPERTY, Y), unequal(requiredProperty, OTHER_PROPERTY)
			Rule require_first = Expressions.makeRule(require_Sr, first_Sx, tripleEDB_SXOY, unequal_rO);
			
			rules.add(require_first);
			
			// next(PREVIOUS_STATEMENT, STATEMENT)
			Atom next_PS = Expressions.makeAtom(next, previousStatement, statement);
			
			// require(PREVIOUS_STATEMENT, requiredProperty)
			Atom require_Pr = Expressions.makeAtom(require, previousStatement, requiredPropertyConstant);
			
			// require(STATEMENT, requiredProperty) :-
			//	next(PREVIOUS_STATEMENT, STATEMENT,
			//	require(PREVIOUS_STATEMENT, requiredProperty),
			//	tripleEDB(STATEMENT, X, OTHER_PROPERTY, Y),
			//	unequal(requiredProperty, OTHER_PROPERTY)
			Rule require_next = Expressions.makeRule(require_Sr, next_PS, require_Pr, tripleEDB_SXOY, unequal_rO);
			
			rules.add(require_next);
			
			if (allowedValues.size() > 0) {
				Conjunction require_Sr_conjunction = Expressions.makeConjunction(require_Sr);

				// tripleEDB(STATEMENT, X, requiredProperty, Y)
				Atom tripleEDB_SXrY = Expressions.makeAtom(tripleEDB, statement, x, requiredPropertyConstant, y);
				
				// setup values inequality
				Set<String> valuesForRequiredProperty = tripleSet.getValues().get(Utility.BASE_URI + requiredProperty);
				if (valuesForRequiredProperty != null) {
					valuesForRequiredProperty.addAll(allowedValues);
					try {
						InequalityHelper.addUnequalConstantsToReasoner(valuesForRequiredProperty);
					} catch (ReasonerStateException e) {
						logger.error(inequalityError, e);
						return internalError;
					}
				}
				
				List<Atom> unequal_vY = new ArrayList<Atom>();
				// unequal(<values>, Y)
				
				for (String value : allowedValues) {
					Constant valueConstant = Expressions.makeConstant(value);
					unequal_vY.add(Expressions.makeAtom(InequalityHelper.unequal, valueConstant, y));
				}
				
				// require(STATEMENT, requiredProperty) :- first(STATEMENT, X), tripleEDB(STATEMENT, X, requiredProperty, Y), unequal(<values>, Y)
				List<Atom> bodyFirst = new ArrayList<Atom>(Arrays.asList(first_Sx, tripleEDB_SXrY));
				bodyFirst.addAll(unequal_vY);
				Rule require_first_values = Expressions.makeRule(require_Sr_conjunction, Expressions.makeConjunction(bodyFirst));
				
				rules.add(require_first_values);
				
				// require(STATEMENT, requiredProperty) :-
				//	next(PREVIOUS_STATEMENT, STATEMENT),
				//	require(PREVIOUS_STATEMENT, requiredProperty),
				//	tripleEDB(STATEMENT, X, requiredProperty, Y),
				//	unequal(<values>, Y)
				List<Atom> bodyNext = new ArrayList<Atom>(Arrays.asList(next_PS, require_Pr, tripleEDB_SXrY));
				bodyNext.addAll(unequal_vY);
				Rule require_next_values = Expressions.makeRule(require_Sr_conjunction, Expressions.makeConjunction(bodyNext));
				
				rules.add(require_next_values);
			}
			
			// violation_item_property(X, requiredProperty)
			Atom violation_Xr = Expressions.makeAtom(violation_item_property, x, requiredPropertyConstant);
			
			// last(STATEMENT, X)
			Atom last_SX = Expressions.makeAtom(last, statement, x);
			
			// violation(X, requiredProperty) :- require(STATEMENT, requiredProperty), last(STATEMENT, X)
			Rule violation = Expressions.makeRule(violation_Xr, require_Sr, last_SX);
			
			rules.add(violation);
		}
		
		Atom violation_query = Expressions.makeAtom(violation_item_property, x, y);
		
		Constant secondToLastStatement = Expressions.makeConstant("Q129$1101349A-38FB-45F0-B365-60DD8EEE0ACA");
		
		Constant lastStatement = Expressions.makeConstant("Q129$1101349A-38FB-45F0-B365-60DF8EEE0ACA");
		
		Constant conflictingValue = Utility.makeConstant("Q13");
		
		Constant nonconflictingValue = Utility.makeConstant("Q13789518");
		
		Atom query = Expressions.makeAtom(InequalityHelper.unequal, Utility.makeConstant("P131"), x);
		
		try {
			return prepareAndExecuteQueries(rules, Arrays.asList(violation_query));
		} catch (PrepareQueriesException e1) {
			return e1.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
