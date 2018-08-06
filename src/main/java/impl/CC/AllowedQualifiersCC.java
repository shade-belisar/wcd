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

import impl.PCC.AllowedQualifiersPCC;
import impl.PCC.PropertyConstraintChecker;
import impl.TS.AllowedQualifiersTS;
import utility.InequalityHelper;
import utility.Utility;

import static utility.SC.violation_triple_query;

public class AllowedQualifiersCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(AllowedQualifiersCC.class);
	
	public static final String ALLOWED_QUALIFIER = "P2306";
	
	Map<String, HashSet<String>> allowedQualifiers;
	
	final AllowedQualifiersTS tripleSet;
	
	public AllowedQualifiersCC() throws IOException {
		super("Q21510851");
		tripleSet = new AllowedQualifiersTS(allowedQualifiers.keySet());
	}

	@Override
	protected Set<String> qualifiers() {
		return asSet();
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(ALLOWED_QUALIFIER);
	}

	@Override
	protected void process(QuerySolution solution) {
		allowedQualifiers = new HashMap<String, HashSet<String>>();
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		
		if (!allowedQualifiers.containsKey(property))
			allowedQualifiers.put(property, new HashSet<String>());

		RDFNode node = solution.get(ALLOWED_QUALIFIER);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				allowedQualifiers.get(property).add(value);				
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
		InequalityHelper.setOrReset(reasoner);
		Set<String> qualifiers = tripleSet.getQualifierProperties();
		for (Set<String> qualifierSet : allowedQualifiers.values()) {
			qualifiers.addAll(qualifierSet);
		}
		InequalityHelper.addUnequalConstantsToReasoner(qualifiers);
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
		for (Map.Entry<String, HashSet<String>> entry : allowedQualifiers.entrySet()) {
			result.add(new AllowedQualifiersPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}
