package impl.CC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;

import impl.PCC.ConflictsWithPCC;
import impl.PCC.PropertyConstraintChecker;
import utility.Utility;

public class ConflictsWithCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ConflictsWithCC.class);
	
	public static final String PROPERTY = "P2306";
	public static final String ITEM_OF_PROPERTY_CONSTRAINT = "P2305";
	
	Map<String, HashMap<String, HashSet<String>>> configuration = new HashMap<String, HashMap<String, HashSet<String>>>();

	public ConflictsWithCC() {
		super("Q21502838");
	}

	@Override
	protected Set<String> qualifiers() {
		return new HashSet<String>(Arrays.asList(PROPERTY));
	}

	@Override
	protected Set<String> concatQualifiers() {
		return new HashSet<String>(Arrays.asList(ITEM_OF_PROPERTY_CONSTRAINT));
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = solution.get("item").asResource().getLocalName();
		
		if (!configuration.containsKey(property))
			configuration.put(property, new HashMap<String, HashSet<String>>());

		String propQualifier = solution.get(PROPERTY).asResource().getLocalName();
		if (!configuration.get(property).containsKey(propQualifier))
			configuration.get(property).put(propQualifier, new HashSet<String>());
		RDFNode node = solution.get(ITEM_OF_PROPERTY_CONSTRAINT);
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
		for (Map.Entry<String, HashMap<String, HashSet<String>>> entry : configuration.entrySet()) {
			result.add(new ConflictsWithPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}
