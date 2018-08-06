package impl.CC;

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

import impl.PCC.OneOfQualifierValuePCC;
import impl.PCC.PropertyConstraintChecker;
import utility.Utility;

public class OneOfQualifierValueCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SingleValueCC.class);
	
	public static final String QUALIFIER_PROPERTY = "P2306";
	public static final String QUALIFIER_VALUE = "P2305";
	
	Map<String, HashMap<String, HashSet<String>>> qualifiersAndValues = new HashMap<String, HashMap<String, HashSet<String>>>();

	public OneOfQualifierValueCC() {
		super("Q52712340");
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
		String property = solution.get("item").asResource().getLocalName();
		
		if (!qualifiersAndValues.containsKey(property))
			qualifiersAndValues.put(property, new HashMap<String, HashSet<String>>());

		String propQualifier = Utility.BASE_URI + solution.get(QUALIFIER_PROPERTY).asResource().getLocalName();
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
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, HashMap<String, HashSet<String>>> entry : qualifiersAndValues.entrySet()) {
			result.add(new OneOfQualifierValuePCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}
