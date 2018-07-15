package wikidata.constraints.datalog.impl.CC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.QuerySolution;

import wikidata.constraints.datalog.impl.PCC.UsedForValuesOnlyPCC;
import wikidata.constraints.datalog.main.ConstraintChecker;
import wikidata.constraints.datalog.main.PropertyConstraintChecker;

public class UsedForValuesOnlyCC extends ConstraintChecker {
	
	Set<String> properties = new HashSet<String>();

	public UsedForValuesOnlyCC() {
		super("Q21528958");
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (String property : properties) {
			result.add(new UsedForValuesOnlyPCC(property));
		}
		return result;
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
		properties.add(solution.get("item").asResource().getLocalName());
	}

}
