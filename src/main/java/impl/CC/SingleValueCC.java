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

import impl.PCC.PropertyConstraintChecker;
import impl.PCC.SingleValuePCC;
import impl.TS.SingleValueTS;
import utility.InequalityHelper;
import utility.Utility;

import static utility.SC.violation_triple_query;

public class SingleValueCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SingleValueCC.class);
	
	public static final String SEPARATOR = "P4155";
	
	Map<String, Set<String>> propertiesAndSeparators;
	
	final SingleValueTS tripleSet;

	public SingleValueCC() throws IOException {
		super("Q19474404");
		tripleSet = new SingleValueTS(propertiesAndSeparators);
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
	void prepareFacts() throws ReasonerStateException, IOException {
		loadTripleSets(tripleSet);
		InequalityHelper.setOrReset(reasoner);
		InequalityHelper.addUnequalConstantsToReasoner(tripleSet.getStatementIDs());
		InequalityHelper.addUnequalConstantsToReasoner(tripleSet.getQualifierValues());
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
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, Set<String>> entry : propertiesAndSeparators.entrySet()) {
			result.add(new SingleValuePCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}
