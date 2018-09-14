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

import impl.DS.DataSet.DataSetPredicate;
import impl.PCC.MultiValuePCC;
import impl.PCC.PropertyConstraintChecker;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class MultiValueCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(MultiValueCC.class);
	
	Set<String> properties;
	
	public MultiValueCC() throws IOException {
		super("Q21510857");
	}

	@Override
	void initDataField() {
		properties = new HashSet<String>();
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
		return asSet();
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		properties.add(property);
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> checkers = new ArrayList<PropertyConstraintChecker>();
		for (String entry : properties) {
			checkers.add(new MultiValuePCC(entry));
		}
		return checkers;
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		Main.statementSet.loadFile(DataSetPredicate.STATEMENT, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.FIRST, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.NEXT, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.LAST, reasoner);
	}
	
	@Override
	public void registerInequalities() throws IOException {
		InequalityHelper.getInequalityHelper(this)
		.registerInequality(DataSetPredicate.STATEMENT, 2);
	}
}
