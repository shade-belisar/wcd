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
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.PCC.AllowedEntityTypesPCC;
import impl.PCC.PropertyConstraintChecker;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class AllowedEntityTypesCC extends ConstraintChecker {
	
	public static final String ALLOWED_ENTITY_TYPE =  "P2305";
	
	public static final String AS_ITEM = Utility.BASE_URI + "Q29934200";
	public static final String AS_PROPERTY = Utility.BASE_URI + " Q29934218";
	
	Map<String, HashSet<String>> allowedEntityTypes;

	public AllowedEntityTypesCC() throws IOException {
		super("Q52004125");
	}
	
	@Override
	void initDataField() {
		allowedEntityTypes = new HashMap<String, HashSet<String>>();
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
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		
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
	protected Set<Atom> queries() {
		return asSet(violation_statement_query);
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		Main.statementSet.loadItemsFile(reasoner);
		Main.statementSet.loadPropertiesFile(reasoner);	
	}
	
	@Override
	public void registerInequalities() {
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

