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
import impl.PCC.InversePCC;
import impl.PCC.PropertyConstraintChecker;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class SymmetricCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(SymmetricCC.class);
	
	Set<String> configuration;

	public SymmetricCC() throws IOException {
		super("Q21510862");
	}

	@Override
	void initDataField() {
		configuration = new HashSet<String>();
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
		
		configuration.add(property);
	}

	@Override
	public void prepareFacts() throws ReasonerStateException, IOException {
		Main.statementSet.loadFile(DataSetPredicate.STATEMENT, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.FIRST, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.NEXT, reasoner);
		Main.statementSet.loadFile(DataSetPredicate.LAST, reasoner);
	}
	
	@Override
	public void registerInequalities() throws IOException {
		InequalityHelper.getInequalityHelper(this)
		.registerInequality(Main.statementSet.getFile(DataSetPredicate.STATEMENT), 1)
		.registerInequality(Main.statementSet.getFile(DataSetPredicate.STATEMENT), 2)
		.registerInequality(Main.statementSet.getFile(DataSetPredicate.STATEMENT), 3);
	}
	
	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for(String entry : configuration) {
			result.add(new InversePCC(entry, asSet(entry)));
		}
		return result;
	}
}
