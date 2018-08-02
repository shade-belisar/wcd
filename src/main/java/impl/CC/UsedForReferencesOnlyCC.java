package impl.CC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.QuerySolution;

import impl.PCC.PropertyConstraintChecker;
import impl.PCC.UsedForReferencesOnlyPCC;
import impl.PCC.UsedForValuesOnlyPCC;

public class UsedForReferencesOnlyCC extends ConstraintChecker {
	
	Set<String> properties = new HashSet<String>();

	public UsedForReferencesOnlyCC() {
		super("Q21528959");
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
	
	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (String property : properties) {
			result.add(new UsedForValuesOnlyPCC(property));
		}
		return result;
	}
}
