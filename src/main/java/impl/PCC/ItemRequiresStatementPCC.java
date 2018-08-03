package impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;

import impl.TS.ItemRequiresStatementTS;
import impl.TS.TripleSet;
import utility.InequalityHelper;
import utility.PrepareQueriesException;
import utility.StatementNonExistenceHelper;
import utility.Utility;

public class ItemRequiresStatementPCC extends PropertyConstraintChecker {
	
	final Map<String, HashSet<String>> configuration;
	
	final ItemRequiresStatementTS tripleSet;

	public ItemRequiresStatementPCC(String property_, Map<String, HashSet<String>> configuration_) throws IOException {
		super(property_);
		configuration = configuration_;
		tripleSet = new ItemRequiresStatementTS(property);
	}

	@Override
	public String violations() throws IOException {
		if (!tripleSet.notEmpty())
			return "";
		
		// Loading EDB-facts
		try {
			loadTripleSets(tripleSet);
			if (tripleSet.firstNotEmpty()) {
				final DataSource firstEDBPath = new CsvFileDataSource(tripleSet.getFirstFile());
				reasoner.addFactsFromDataSource(StatementNonExistenceHelper.first, firstEDBPath);
			}
			if (tripleSet.nextNotEmpty()) {
				final DataSource nextEDBPath = new CsvFileDataSource(tripleSet.getNextFile());
				reasoner.addFactsFromDataSource(StatementNonExistenceHelper.next, nextEDBPath);
			}
			if (tripleSet.lastNotEmpty()) {
				final DataSource lastEDBPath = new CsvFileDataSource(tripleSet.getLastFile());
				reasoner.addFactsFromDataSource(StatementNonExistenceHelper.last, lastEDBPath);
			}
				
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		// Establishing inequality
		InequalityHelper.setOrReset(reasoner);
		
		try {
			InequalityHelper.addUnequalConstantsToReasoner(tripleSet.allProperties());
			InequalityHelper.addUnequalConstantsToReasoner(tripleSet.allValues());
		} catch (ReasonerStateException e) {
			logger.error("Trying to add unequal constants to reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		// Establishing require
		StatementNonExistenceHelper.setOrReset(reasoner);
		
		try {
			for (Map.Entry<String, HashSet<String>> entry : configuration.entrySet()) {
				Term requiredPropertyConstant = Utility.makeConstant(entry.getKey());
			
				// tripleEDB(S, I, P, V)
				Atom tripleEDB_SIPV = Expressions.makeAtom(tripleEDB, s, i, p, v);
			
				// unequal(requiredPropertyConstant, P)
				Atom unequal_rP = Expressions.makeAtom(InequalityHelper.unequal, requiredPropertyConstant, p);
			
				StatementNonExistenceHelper.initRequireTriple(requiredPropertyConstant, tripleEDB_SIPV, unequal_rP);
				
				Set<String> allowedValues = entry.getValue();				
				if (allowedValues.size() != 0) {
					// tripleEDB(S, I, requiredPropertyConstant, V)
					Atom tripleEDB_SIrV = Expressions.makeAtom(tripleEDB, s, i, requiredPropertyConstant, v);
					
					List<Atom> conjunction = new ArrayList<Atom>();
					conjunction.add(tripleEDB_SIrV);
					
					for (String allowedValue : allowedValues) {
						Constant allowedValueConstant = Utility.makeConstant(allowedValue);
						conjunction.add(Expressions.makeAtom(InequalityHelper.unequal, allowedValueConstant, v));
					}
					
					StatementNonExistenceHelper.initRequireTriple(requiredPropertyConstant, conjunction);
				}
			
				
			}
		} catch (ReasonerStateException e) {
			logger.error("Trying to add require-pattern rules to reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		List<Rule> rules = new ArrayList<Rule>();
		
		for (String requiredProperty : configuration.keySet()) {
			Term requiredPropertyConstant = Utility.makeConstant(requiredProperty);
			
			// violation_triple(S, I, propertyConstant, V)
			Atom violation_triple_SIpV = Expressions.makeAtom(violation_triple, s, i, propertyConstant, v);
			
			// tripleEDB(S, I, propertyConstant, V)
			Atom tripleEDB_SIpV = Expressions.makeAtom(tripleEDB, s, i, propertyConstant, v);
			
			// tripleEDB(O, I, P, X)
			Atom tripleEDB_OIPX = Expressions.makeAtom(tripleEDB, o, i, p, x);
			
			// last(O, I)
			Atom last_OI = Expressions.makeAtom(StatementNonExistenceHelper.last, o, i);
			
			// require(O, requiredPropertyConstant)
			Atom require_Or = Expressions.makeAtom(StatementNonExistenceHelper.require, o, requiredPropertyConstant);
			
			// violation_triple(S, I, propertyConstant, V) :-
			//	tripleEDB(S, I, propertyConstant, V),
			//	tripleEDB(O, I, P, X), last(O, I),
			//	require(O, requiredPropertyConstant)
			Rule violation = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, tripleEDB_OIPX, last_OI, require_Or);
			rules.add(violation);
		}
		
		try {
			return prepareAndExecuteQueries(rules, Arrays.asList(violation_triple_query));
		} catch (PrepareQueriesException e1) {
			return e1.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}
}
