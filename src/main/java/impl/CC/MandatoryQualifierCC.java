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
import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.PCC.MandatoryQualifierPCC;
import impl.PCC.PropertyConstraintChecker;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class MandatoryQualifierCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(MandatoryQualifierCC.class);
	
	public static final String REQUIRED_PROPERTY = "P2306";
	
	Map<String, Set<String>> propertiesAndQualifiers;

	public MandatoryQualifierCC() throws IOException {
		super("Q21510856");
	}

	@Override
	void initDataField() {
		propertiesAndQualifiers = new HashMap<String, Set<String>>();
	}

	@Override
	protected Set<Atom> queries() {
		return asSet(violation_statement_query);		
	}

	@Override
	protected Set<String> qualifiers() {
		return asSet();
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(REQUIRED_PROPERTY);
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		
		if (!propertiesAndQualifiers.containsKey(property))
			propertiesAndQualifiers.put(property, new HashSet<String>());

		RDFNode node = solution.get(REQUIRED_PROPERTY);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				propertiesAndQualifiers.get(property).add(value);				
			}
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		Main.statementSet.loadFirstQualifierFile(reasoner);
		Main.statementSet.loadNextQualifierFile(reasoner);
		Main.statementSet.loadLastQualifierFile(reasoner);
	}
	
	@Override
	public void registerInequalities() throws IOException {
		Set<String> qualifierProperties = new HashSet<String>();
		for (Set<String> propertySet : propertiesAndQualifiers.values()) {
			qualifierProperties.addAll(propertySet);
		}
		InequalityHelper.registerInequality(qualifierProperties);
		InequalityHelper.registerInequality(Main.statementSet.getQualifierFile(), 1);
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, Set<String>> entry : propertiesAndQualifiers.entrySet()) {
			result.add(new MandatoryQualifierPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}
