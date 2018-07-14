package wikidata.constraints.datalog.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wikidata.constraints.datalog.impl.PCC.UsedForValuesOnlyPCC;
import wikidata.constraints.datalog.main.ConstraintChecker;
import wikidata.constraints.datalog.main.PropertyConstraintChecker;

public class UsedForValuesOnlyCC extends ConstraintChecker {

	public UsedForValuesOnlyCC() throws IOException {
		super("Q21528958");
	}

	@Override
	protected Set<String> additionalQualifiers() {
		return new HashSet<String>();
	}

	@Override
	protected PropertyConstraintChecker getPropertyChecker(String property, Map<String, String> qualifiers) throws IOException {
		return new UsedForValuesOnlyPCC(property, qualifiers);
	}

}
