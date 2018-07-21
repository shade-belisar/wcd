package wikidata.constraints.datalog.impl.PCC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import wikidata.constraints.datalog.impl.TS.DistinctValuesTS;
import wikidata.constraints.datalog.impl.TS.TripleSet;
import wikidata.constraints.datalog.utility.InequalityHelper;
import wikidata.constraints.datalog.utility.PrepareQueriesException;

public class DistinctValuesPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(DistinctValuesPCC.class);
	
	protected final static String EQUAL_VALUES = "equalValues";
	
	protected final static String A = "a";
	protected final static String B = "b";
	protected final static String C = "c";
	
	protected final static Predicate equalValues = Expressions.makePredicate(EQUAL_VALUES, 8);
	
	protected final static Variable a = Expressions.makeVariable(A);
	protected final static Variable b = Expressions.makeVariable(B);
	protected final static Variable c = Expressions.makeVariable(C);
	
	final DistinctValuesTS tripleSet;

	public DistinctValuesPCC(String property_) throws IOException {
		super(property_);
		tripleSet = new DistinctValuesTS(property);
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
		
		InequalityHelper.setOrReset(reasoner);
		
		try {
			InequalityHelper.addUnequalConstantsToReasoner(tripleSet.getStatements());
		} catch (ReasonerStateException e) {
			logger.error("Trying to add unequal constants to reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		// equalValues(STATEMENT, X, propertyConstant, Z, OTHER_STATEMENT, A, propertyConstant, Z)
		Atom equalValues_SXpZOApZ = Expressions.makeAtom(equalValues, statement, x, propertyConstant, z, otherStatement, a, propertyConstant, z);
		
		// tripleEDB(STATEMENT, X, propertyConstant, Z)
		Atom tripleEDB_SXpZ = Expressions.makeAtom(tripleEDB, statement, x, propertyConstant, z);
		
		// tripleEDB(OTHER_STATEMENT, A, propertyConstant, Z)
		Atom tripleEDB_OApZ = Expressions.makeAtom(tripleEDB, otherStatement, a, propertyConstant, z);
		
		// unequal(STATEMENT, OTHER_STATEMENT)
		Atom unequal_SO = Expressions.makeAtom(InequalityHelper.unequal, statement, otherStatement);
		
		// equalValues(STATEMENT, X, propertyConstant, Z, OTHER_STATEMENT, A, propertyConstant, Z) :-
		//	tripleEDB(STATEMENT, X, propertyConstant, Z),
		//	tripleEDB(OTHER_STATEMENT, A, propertyConstant, Z),
		//	unequal(STATEMENT, OTHER_STATEMENT)
		Rule violation = Expressions.makeRule(equalValues_SXpZOApZ, tripleEDB_SXpZ, tripleEDB_OApZ, unequal_SO);
		
		rules.add(violation);
		
		Atom query = Expressions.makeAtom(equalValues, statement, x, y, z, otherStatement, a, b, c);
		
		try {
			return prepareAndExecuteQueries(rules, Arrays.asList(query));
		} catch (PrepareQueriesException e) {
			return e.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
