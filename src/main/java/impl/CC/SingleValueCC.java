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
import impl.PCC.SingleValuePCC;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class SingleValueCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SingleValueCC.class);
	
	public static final String SEPARATOR = "P4155";
	
	Map<String, Set<String>> propertiesAndSeparators;

	public SingleValueCC() throws IOException {
		super("Q19474404");
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
		return asSet(violation_statement_query, violation_qualifier_query, violation_reference_query);
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
		Main.statementSet.loadFile(DataSetPredicate.STATEMENT, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.QUALIFIER, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.REFERENCE, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.FIRST_QUALIFIER, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.NEXT_QUALIFIER, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.LAST_QUALIFIER, reasoner);
	}
	
	@Override
	public void registerInequalities() throws IOException {
		Set<String> values = new HashSet<String>();
		for (Set<String> valuesSet : propertiesAndSeparators.values()) {
			values.addAll(valuesSet);
		}
		InequalityHelper.getInequalityHelper(this)
		.registerInequality(values)
		.registerInequality(DataSetPredicate.STATEMENT, 0)
		.registerInequality(DataSetPredicate.QUALIFIER, 2);
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

