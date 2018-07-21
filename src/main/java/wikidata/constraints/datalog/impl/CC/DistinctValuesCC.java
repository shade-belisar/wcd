package wikidata.constraints.datalog.impl.CC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.log4j.Logger;

import wikidata.constraints.datalog.impl.PCC.DistinctValuesPCC;
import wikidata.constraints.datalog.impl.PCC.PropertyConstraintChecker;

public class DistinctValuesCC extends ConstraintChecker {

	final static Logger logger = Logger.getLogger(DistinctValuesCC.class);
	
	final static Set<String> properties = new HashSet<String>();

	public DistinctValuesCC() {
		super("Q21502410");
	}

	@Override
	protected Set<String> qualifiers() {
		return new HashSet<String>();
	}

	@Override
	protected Set<String> concatQualifiers() {
		return new HashSet<String>();
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = solution.get("item").asResource().getLocalName();
		properties.add(property);
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
