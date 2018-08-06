package impl.CC;

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
import impl.TS.DistinctValuesTS;
import utility.InequalityHelper;
import utility.Utility;

import static utility.SC.violation_triple_query;

public class DistinctValuesCC extends ConstraintChecker {

	final static Logger logger = Logger.getLogger(DistinctValuesCC.class);
	
	final static Set<String> properties = new HashSet<String>();
	
	final DistinctValuesTS tripleSet;

	public DistinctValuesCC() throws IOException {
		super("Q21502410");
		tripleSet = new DistinctValuesTS(properties);
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
	protected Set<Atom> queries() {
		return asSet(violation_triple_query);
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		loadTripleSets(tripleSet);
		InequalityHelper.setOrReset(reasoner);
		InequalityHelper.addUnequalConstantsToReasoner(tripleSet.getStatements());
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
		for (String property : properties) {
			result.add(new DistinctValuesPCC(property));
		}
		return result;
	}
}
