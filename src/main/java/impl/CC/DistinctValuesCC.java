package impl.CC;

import static utility.SC.violation_statement_query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.PCC.DistinctValuesPCC;
import impl.PCC.PropertyConstraintChecker;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class DistinctValuesCC extends ConstraintChecker {

	final static Logger logger = Logger.getLogger(DistinctValuesCC.class);
	
	Set<String> properties;

	public DistinctValuesCC() throws IOException {
		super("Q21502410");
	}

	@Override
	protected Set<String> qualifiers() {
		return asSet();
	}

	@Override
	void initDataField() {
		properties = new HashSet<String>();
	}
	
	@Override
	protected Set<String> concatQualifiers() {
		return asSet();
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		properties.add(property);
	}

	@Override
	protected Set<Atom> queries() {
		return asSet(violation_statement_query);
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		Main.statementSet.loadStatementFile(reasoner);
	}
	
	@Override
	public void registerInequalities() throws IOException {
		InequalityHelper.getInequalityHelper(this)
		.registerInequality(Main.statementSet.getStatementFile(), 0);
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (String property : properties) {
			result.add(new DistinctValuesPCC(property));
		}
		return result;
	}
}
