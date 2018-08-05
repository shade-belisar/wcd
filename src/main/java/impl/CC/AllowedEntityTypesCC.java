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

import impl.PCC.AllowedEntityTypesPCC;
import impl.PCC.PropertyConstraintChecker;
import utility.Utility;

public class AllowedEntityTypesCC extends ConstraintChecker {
	
	public static final String ALLOWED_ENTITY_TYPE =  "P2305";
	
	public static final String AS_ITEM = Utility.BASE_URI + "Q29934200";
	public static final String AS_PROPERTY = Utility.BASE_URI + " Q29934218";
	
	Map<String, HashSet<String>> allowedEntityTypes = new HashMap<String, HashSet<String>>();

	public AllowedEntityTypesCC() {
		super("Q52004125");
	}

	@Override
	protected Set<String> qualifiers() {
		return asSet();
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(ALLOWED_ENTITY_TYPE);
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = solution.get("item").asResource().getLocalName();
		
		if (!allowedEntityTypes.containsKey(property))
			allowedEntityTypes.put(property, new HashSet<String>());

		RDFNode node = solution.get(ALLOWED_ENTITY_TYPE);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				allowedEntityTypes.get(property).add(value);				
			}
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, HashSet<String>> entry : allowedEntityTypes.entrySet()) {
			result.add(new AllowedEntityTypesPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}
