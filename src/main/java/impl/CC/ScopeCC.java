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

import impl.PCC.PropertyConstraintChecker;
import impl.PCC.ScopePCC;
import utility.Utility;

public class ScopeCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ScopeCC.class);
	
	public static final String SCOPE = "P5314";
	
	public static final String AS_MAIN_VALUE = Utility.BASE_URI + "Q54828448";
	public static final String AS_QUALIFIER = Utility.BASE_URI + "Q54828449";
	public static final String AS_REFERENCE = Utility.BASE_URI + "Q54828450";
	
	Map<String, HashSet<String>> result = new HashMap<String, HashSet<String>>();

	public ScopeCC() {
		super("Q53869507");
	}

	protected Set<String> qualifiers() {
		return new HashSet<String>();
	}

	protected Set<String> concatQualifiers() {
		return new HashSet<String>(Arrays.asList(SCOPE));
	}
	
	protected void process(QuerySolution solution) {
		String property = solution.get("item").asResource().getLocalName();
		HashSet<String> qualifiers = new HashSet<String>();
		
		RDFNode node = solution.get(ScopeCC.SCOPE);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			for (String qualifier : literal.getString().split(",")) {
				qualifiers.add(qualifier);
			}
			result.put(property, qualifiers);
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}
	
	public List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> checkers = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, HashSet<String>> entry : result.entrySet()) {
			checkers.add(new ScopePCC(entry.getKey(), entry.getValue()));
		}
		return checkers;
	}
}
