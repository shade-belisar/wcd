package impl.PCC;

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
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;

import impl.CC.AllowedEntityTypesCC;
import impl.TS.AllowedEntityTypesTS;
import impl.TS.TripleSet;
import utility.PrepareQueriesException;

public class AllowedEntityTypesPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(AllowedEntityTypesPCC.class);
	
	final AllowedEntityTypesTS tripleSet;
	
	final Set<String> allowedEntityTypes;
	
	final static String ITEM = "item";
	// Weird name because we already have a property
	final static String IS_PROPERTY = "is_property";
	
	final static Predicate item = Expressions.makePredicate(ITEM, 1);
	final static Predicate is_property = Expressions.makePredicate(IS_PROPERTY, 1);

	public AllowedEntityTypesPCC(String property_, Set<String> allowedEntityTypes_) throws IOException {
		super(property_);
		allowedEntityTypes = allowedEntityTypes_;
		tripleSet = new AllowedEntityTypesTS(property);
	}

	@Override
	public String violations() throws IOException {
		if (!tripleSet.notEmpty())
			return "";
		
		try {
			loadTripleSets(tripleSet);
			if (tripleSet.itemsNotEmpty()) {
				final DataSource itemsEDBPath = new CsvFileDataSource(tripleSet.getItemsFile());
				reasoner.addFactsFromDataSource(item, itemsEDBPath);
			}
			if (tripleSet.propertiesNotEmpty()) {
				final DataSource propertiesEDBPath = new CsvFileDataSource(tripleSet.getPropertiesFile());
				reasoner.addFactsFromDataSource(is_property, propertiesEDBPath);
			}
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
			return internalError;
		}
		
		List<Rule> rules = new ArrayList<Rule>();
		
		// item(I)
		Atom item_I = Expressions.makeAtom(item, i);
		
		// violation_triple(S, I, propertyConstant, V ) :- tripleEDB(S, I, propertyConstant, V ), item(I)
		Rule notItem = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, item_I);
		
		// property(I)
		Atom property_I = Expressions.makeAtom(is_property, i);
		
		// violation_triple(S, I, propertyConstant, V ) :- tripleEDB(S, I, propertyConstant, V ), property(I)
		Rule notProperty = Expressions.makeRule(violation_triple_SIpV, tripleEDB_SIpV, property_I);
		
		if (!allowedEntityTypes.contains(AllowedEntityTypesCC.AS_ITEM))
			rules.add(notItem);
		
		if (!allowedEntityTypes.contains(AllowedEntityTypesCC.AS_PROPERTY))
			rules.add(notProperty);
		
		try {
			return prepareAndExecuteQueries(rules, violation_triple_query);
		} catch (PrepareQueriesException e1) {
			return e1.getMessage();
		}
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}
