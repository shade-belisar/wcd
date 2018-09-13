package impl.CC;

import static utility.SC.violation_statement_query;

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
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.DS.DataSet.DataSetPredicate;
import impl.PCC.PropertyConstraintChecker;
import impl.PCC.ValueRequiresStatementPCC;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class ValueRequiresStatementCC extends ConstraintChecker {

	final static Logger logger = Logger.getLogger(ValueRequiresStatementCC.class);
	
	public static final String REQUIRED_PROPERTY = "P2306";
	public static final String ALLOWED_VALUE = "P2305";
	
	Map<String, Map<String, Set<String>>> configuration;

	public ValueRequiresStatementCC() throws IOException {
		super("Q21510864");
	}

	@Override
	void initDataField() {
		configuration = new HashMap<String, Map<String, Set<String>>>();
	}

	@Override
	protected Set<Atom> queries() {
		return asSet(violation_statement_query);
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
	void prepareFacts() throws ReasonerStateException, IOException {
		Main.statementSet.loadFile(DataSetPredicate.STATEMENT, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.FIRST, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.NEXT, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.LAST, reasoner);
	}
	
	@Override
	public void registerInequalities() throws IOException {
		Set<String> inequalities = new HashSet<String>();
		for (Map<String, Set<String>> value : configuration.values()) {
			inequalities.addAll(value.keySet());
			for (Set<String> set : value.values()) {
				inequalities.addAll(set);
			}
		}
		InequalityHelper.getInequalityHelper(this)
		.registerInequality(inequalities)
		.registerInequality(Main.statementSet.getFile(DataSetPredicate.STATEMENT), 2)
		.registerInequality(Main.statementSet.getFile(DataSetPredicate.STATEMENT), 3);
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for(Map.Entry<String, Map<String, Set<String>>> entry : configuration.entrySet()) {
			result.add(new ValueRequiresStatementPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}

}
