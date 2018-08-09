package impl.CC;

import static utility.SC.first;
import static utility.SC.last;
import static utility.SC.next;
import static utility.SC.violation_triple_query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;

import impl.PCC.ItemRequiresStatementPCC;
import impl.PCC.PropertyConstraintChecker;
import impl.TS.ItemRequiresStatementTS;
import utility.InequalityHelper;
import utility.Utility;

public class ItemRequiresStatementCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ItemRequiresStatementCC.class);
	
	public static final String REQUIRED_PROPERTY = "P2306";
	public static final String ALLOWED_VALUE = "P2305";
	
	Map<String, Map<String, Set<String>>> configuration;
	
	final ItemRequiresStatementTS tripleSet;

	public ItemRequiresStatementCC() throws IOException {
		super("Q21503247");
		tripleSet = new ItemRequiresStatementTS(configuration.keySet());
	}

	@Override
	void initDataField() {
		configuration = new HashMap<String, Map<String, Set<String>>>();
	}
	
	@Override
	protected Set<String> qualifiers() {
		return asSet(REQUIRED_PROPERTY);
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(ALLOWED_VALUE);
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		
		if (!configuration.containsKey(property))
			configuration.put(property, new HashMap<String, Set<String>>());
		
		String propQualifier = Utility.addBaseURI(solution.get(REQUIRED_PROPERTY).asResource().getLocalName());
		if (!configuration.get(property).containsKey(propQualifier))
			configuration.get(property).put(propQualifier, new HashSet<String>());
		
		RDFNode node = solution.get(ALLOWED_VALUE);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				configuration.get(property).get(propQualifier).add(value);				
			}
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for(Map.Entry<String, Map<String, Set<String>>> entry : configuration.entrySet()) {
			result.add(new ItemRequiresStatementPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}

	@Override
	protected Set<Atom> queries() {
		return asSet(violation_triple_query);
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		loadTripleSets(tripleSet);
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
		// Establishing inequality
		InequalityHelper.setOrReset(reasoner);

		Set<String> properties = tripleSet.allProperties();
		Set<String> values = tripleSet.allValues();
		for (Map<String, Set<String>> value : configuration.values()) {
			properties.addAll(value.keySet());
			for (Set<String> set : value.values()) {
				values.addAll(set);
			}
		}
		InequalityHelper.establishInequality(properties);
		InequalityHelper.establishInequality(values);
		
	}

	@Override
	void delete() throws IOException {
		tripleSet.delete();
	}

	@Override
	void close() throws IOException {
		tripleSet.close();
	}

}
