package impl.CC;

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

import impl.PCC.PropertyConstraintChecker;
import impl.PCC.SingleBestValuePCC;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class SingleBestValueCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SingleBestValueCC.class);
	
	public static final String SEPARATOR = "P4155";
	
	Map<String, Set<String>> propertiesAndSeparators;

	public SingleBestValueCC() throws IOException {
		super("Q52060874");
	}

	@Override
	void initDataField() {
		propertiesAndSeparators = new HashMap<String, Set<String>>();
	}
	
	@Override
	protected Set<String> qualifiers() {
		return asSet();
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(SEPARATOR);
	}
	
	@Override
	protected Set<Atom> queries() {
		return asSet(violation_triple_query);
	}
	
	@Override
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		
		if (!propertiesAndSeparators.containsKey(property))
			propertiesAndSeparators.put(property, new HashSet<String>());

		RDFNode node = solution.get(SEPARATOR);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				propertiesAndSeparators.get(property).add(value);				
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
		Main.tripleSet.loadRanksFile(reasoner);

		InequalityHelper.setOrReset(reasoner);
		InequalityHelper.establishInequality(Main.tripleSet.getTripleFile(), 0);
		Set<String> values = new HashSet<String>();
		for (Set<String> valuesSet : propertiesAndSeparators.values()) {
			values.addAll(valuesSet);
		}
		InequalityHelper.establishInequality(Main.tripleSet.getQualifierFile(), 2, values);
	}
	
	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, Set<String>> entry : propertiesAndSeparators.entrySet()) {
			result.add(new SingleBestValuePCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}

