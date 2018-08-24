package impl.CC;

import static utility.SC.first;
import static utility.SC.last;
import static utility.SC.next;
import static utility.SC.violation_triple_query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.PCC.InversePCC;
import impl.PCC.PropertyConstraintChecker;
import main.Main;
import utility.CsvGzFileDataSource;
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
		return asSet(violation_triple_query);
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
	void prepareFacts() throws ReasonerStateException, IOException {
		Main.tripleSet.loadFirstFile(reasoner);
		Main.tripleSet.loadNextFile(reasoner);
		Main.tripleSet.loadLastFile(reasoner);

		// Establishing inequality
		InequalityHelper.establishInequality(Main.tripleSet.getTripleFile(), 2, configuration);
		InequalityHelper.establishInequality(Main.tripleSet.getTripleFile(), 1, Main.tripleSet.getTripleFile(), 3);
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
