package impl.CC;

import static utility.SC.violation_qualifier_query;
import static utility.SC.violation_reference_query;
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

import impl.PCC.NoneOfPCC;
import impl.PCC.PropertyConstraintChecker;
import utility.Utility;

public class NoneOfCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(NoneOfCC.class);
	
	public static final String ITEM_OF_PROPERTY_CONSTRAINT = "P2305";
	
	Map<String, HashSet<String>> notAllowedValues;

	public NoneOfCC() throws IOException {
		super("Q52558054");
	}

	@Override
	void initDataField() {
		notAllowedValues = new HashMap<String, HashSet<String>>();
	}
	
	@Override
	protected Set<String> qualifiers() {
		return asSet();
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(ITEM_OF_PROPERTY_CONSTRAINT);
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		
		if (!notAllowedValues.containsKey(property))
			notAllowedValues.put(property, new HashSet<String>());
		
		RDFNode node = solution.get(ITEM_OF_PROPERTY_CONSTRAINT);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				notAllowedValues.get(property).add(value);				
			}
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}
	
	@Override
	protected Set<Atom> queries() {
		return asSet(violation_triple_query, violation_qualifier_query, violation_reference_query);
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, HashSet<String>> entry : notAllowedValues.entrySet()) {
			result.add(new NoneOfPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}
