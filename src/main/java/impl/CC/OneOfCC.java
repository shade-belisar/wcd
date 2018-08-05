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

import impl.PCC.OneOfPCC;
import impl.PCC.PropertyConstraintChecker;

public class OneOfCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(OneOfCC.class);
	
	public static final String ALLOWED_VALUES = "P2305";
	
	Map<String, HashSet<String>> allowedValues = new HashMap<String, HashSet<String>>();

	public OneOfCC() {
		super("Q21510859");
	}

	@Override
	protected Set<String> qualifiers() {
		return asSet();
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(ALLOWED_VALUES);
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = solution.get("item").asResource().getLocalName();
		
		if (!allowedValues.containsKey(property))
			allowedValues.put(property, new HashSet<String>());

		RDFNode node = solution.get(ALLOWED_VALUES);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				allowedValues.get(property).add(value);				
			}
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, HashSet<String>> entry : allowedValues.entrySet()) {
			result.add(new OneOfPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}

}
