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

import impl.PCC.AllowedEntityTypesPCC;
import impl.PCC.AllowedUnitsPCC;
import impl.PCC.PropertyConstraintChecker;

public class AllowedUnitsCC extends ConstraintChecker {
	
	public static final String ALLOWED_UNIT =  "P2305";
	
	Map<String, HashSet<String>> allowedUnits = new HashMap<String, HashSet<String>>();

	public AllowedUnitsCC() {
		super("Q21514353");
	}

	@Override
	protected Set<String> qualifiers() {
		return asSet();
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(ALLOWED_UNIT);
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = solution.get("item").asResource().getLocalName();
		
		if (!allowedUnits.containsKey(property))
			allowedUnits.put(property, new HashSet<String>());

		RDFNode node = solution.get(ALLOWED_UNIT);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				allowedUnits.get(property).add(value);				
			}
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, HashSet<String>> entry : allowedUnits.entrySet()) {
			result.add(new AllowedUnitsPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}

}
