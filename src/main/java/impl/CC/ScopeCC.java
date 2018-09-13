package impl.CC;

import static utility.SC.violation_qualifier_query;
import static utility.SC.violation_reference_query;
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

import impl.DS.DataSet.DataSetPredicate;
import impl.PCC.PropertyConstraintChecker;
import impl.PCC.ScopePCC;
import main.Main;
import utility.Utility;

public class ScopeCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ScopeCC.class);
	
	public static final String SCOPE = "P5314";
	
	public static final String AS_MAIN_VALUE = Utility.BASE_URI + "Q54828448";
	public static final String AS_QUALIFIER = Utility.BASE_URI + "Q54828449";
	public static final String AS_REFERENCE = Utility.BASE_URI + "Q54828450";
	
	Map<String, HashSet<String>> propertiesAndScopes;

	public ScopeCC() throws IOException {
		super("Q53869507");
	}

	@Override
	void initDataField() {
		propertiesAndScopes = new HashMap<String, HashSet<String>>();
	}
	
	protected Set<String> qualifiers() {
		return asSet();
	}

	protected Set<String> concatQualifiers() {
		return asSet(SCOPE);
	}
	
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		HashSet<String> qualifiers = new HashSet<String>();
		
		RDFNode node = solution.get(ScopeCC.SCOPE);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			for (String qualifier : literal.getString().split(",")) {
				qualifiers.add(Utility.addBaseURI(qualifier));
			}
			propertiesAndScopes.put(property, qualifiers);
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}
	
	public List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> checkers = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, HashSet<String>> entry : propertiesAndScopes.entrySet()) {
			checkers.add(new ScopePCC(entry.getKey(), entry.getValue()));
		}
		return checkers;
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		Main.statementSet.loadFile(DataSetPredicate.STATEMENT, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.QUALIFIER, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.REFERENCE, reasoner);
	}
	
	@Override
	public void registerInequalities() throws IOException {
	}

	@Override
	protected Set<Atom> queries() {
		return asSet(violation_statement_query, violation_qualifier_query, violation_reference_query);
	}
}
