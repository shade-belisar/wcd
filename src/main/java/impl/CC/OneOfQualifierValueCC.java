package impl.CC;

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
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.PCC.OneOfQualifierValuePCC;
import impl.PCC.PropertyConstraintChecker;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class OneOfQualifierValueCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SingleValueCC.class);
	
	public static final String QUALIFIER_PROPERTY = "P2306";
	public static final String QUALIFIER_VALUE = "P2305";
	
	Map<String, Map<String, Set<String>>> qualifiersAndValues;

	public OneOfQualifierValueCC() throws IOException {
		super("Q52712340");
		Map<String, Set<String>> temp = new HashMap<String, Set<String>>();
		for (Map.Entry<String, Map<String, Set<String>>> entry : qualifiersAndValues.entrySet()) {
			temp.put(entry.getKey(), entry.getValue().keySet());
		}
	}

	@Override
	void initDataField() {
		qualifiersAndValues = new HashMap<String, Map<String, Set<String>>>();
	}
	
	@Override
	protected Set<String> qualifiers() {
		return asSet(QUALIFIER_PROPERTY);
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(QUALIFIER_VALUE);
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		
		if (!qualifiersAndValues.containsKey(property))
			qualifiersAndValues.put(property, new HashMap<String, Set<String>>());

		String propQualifier = Utility.addBaseURI(solution.get(QUALIFIER_PROPERTY).asResource().getLocalName());
		if (!qualifiersAndValues.get(property).containsKey(propQualifier))
			qualifiersAndValues.get(property).put(propQualifier, new HashSet<String>());
		RDFNode node = solution.get(QUALIFIER_VALUE);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				qualifiersAndValues.get(property).get(propQualifier).add(value);				
			}
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}
	
	@Override
	protected Set<Atom> queries() {
		return asSet(violation_triple_query);
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		InequalityHelper.setOrReset(reasoner);
		Set<String> values = new HashSet<String>();
		for (Map.Entry<String, Map<String, Set<String>>> entry1 : qualifiersAndValues.entrySet()) {
			for (Map.Entry<String, Set<String>> entry2 : entry1.getValue().entrySet()) {
				values.addAll(entry2.getValue());
			}
		}
		InequalityHelper.establishInequality(Main.tripleSet.getQualifierFile(), 2, values);
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, Map<String, Set<String>>> entry : qualifiersAndValues.entrySet()) {
			result.add(new OneOfQualifierValuePCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}
