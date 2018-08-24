package impl.CC;

import static utility.SC.first_qualifier;
import static utility.SC.last_qualifier;
import static utility.SC.next_qualifier;
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
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.PCC.MandatoryQualifierPCC;
import impl.PCC.PropertyConstraintChecker;
import main.Main;
import utility.CsvGzFileDataSource;
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
		return asSet(violation_triple_query);		
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
		Main.tripleSet.loadFirstQualifierFile(reasoner);
		Main.tripleSet.loadNextQualifierFile(reasoner);
		Main.tripleSet.loadLastQualifierFile(reasoner);

		InequalityHelper.setOrReset(reasoner);
		Set<String> qualifierProperties = new HashSet<String>();
		for (Set<String> propertySet : propertiesAndQualifiers.values()) {
			qualifierProperties.addAll(propertySet);
		}
		InequalityHelper.establishInequality(Main.tripleSet.getQualifierFile(), 1, qualifierProperties);
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
