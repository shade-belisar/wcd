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
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.PCC.ConflictsWithPCC;
import impl.PCC.PropertyConstraintChecker;
import impl.TS.ConflictsWithTS;
import utility.Utility;

import static utility.SC.violation_triple_query;

public class ConflictsWithCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ConflictsWithCC.class);
	
	public static final String PROPERTY = "P2306";
	public static final String ITEM_OF_PROPERTY_CONSTRAINT = "P2305";
	
	Map<String, HashMap<String, HashSet<String>>> configuration = new HashMap<String, HashMap<String, HashSet<String>>>();
	
	final ConflictsWithTS tripleSet;

	public ConflictsWithCC() throws IOException {
		super("Q21502838");
		tripleSet = new ConflictsWithTS(configuration.keySet());
	}

	@Override
	protected Set<String> qualifiers() {
		return asSet(PROPERTY);
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(ITEM_OF_PROPERTY_CONSTRAINT);
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		
		if (!configuration.containsKey(property))
			configuration.put(property, new HashMap<String, HashSet<String>>());

		String propQualifier = Utility.addBaseURI(solution.get(PROPERTY).asResource().getLocalName());
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
	protected Set<Atom> queries() {
		return asSet(violation_triple_query);
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		loadTripleSets(tripleSet);
	}

	@Override
	void delete() throws IOException {
		tripleSet.delete();
	}

	@Override
	void close() throws IOException {
		tripleSet.close();
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
