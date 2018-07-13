package wikidata.constraints.datalog.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import wikidata.constraints.datalog.impl.PCC.UsedForValuesOnlyPCC;
import wikidata.constraints.datalog.main.ConstraintChecker;
import wikidata.constraints.datalog.main.PropertyConstraintChecker;

public class UsedForValuesOnlyCC extends ConstraintChecker {

	public UsedForValuesOnlyCC(String constraint) throws IOException {
		super("Q21528958");
	}

	@Override
	protected Map<String, String> additionalQualifiers() {
		return new HashMap<String, String>();
	}

	@Override
	protected PropertyConstraintChecker getPropertyChecker(String property, Map<String, String> qualifiers) throws IOException {
		return new UsedForValuesOnlyPCC(property, qualifiers);
	}

}
