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
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;

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
		final DataSource firstEDBPath = new CsvFileDataSource(Main.tripleSet.getFirstFile());
		reasoner.addFactsFromDataSource(first, firstEDBPath);

		final DataSource nextEDBPath = new CsvFileDataSource(Main.tripleSet.getNextFile());
		reasoner.addFactsFromDataSource(next, nextEDBPath);

		final DataSource lastEDBPath = new CsvFileDataSource(Main.tripleSet.getLastFile());
		reasoner.addFactsFromDataSource(last, lastEDBPath);
		
		// Establishing inequality
		InequalityHelper.setOrReset(reasoner);
		
		InequalityHelper.establishInequality(Main.tripleSet.getTripleFile(), 2, properties);
	}
}
