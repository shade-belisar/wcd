package wikidata.constraints.datalog.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import wikidata.constraints.datalog.impl.PCC.UsedForReferencesOnlyPCC;
import wikidata.constraints.datalog.main.ConstraintChecker;
import wikidata.constraints.datalog.main.PropertyConstraintChecker;

public class UsedForReferencesOnlyCC extends ConstraintChecker {

	public UsedForReferencesOnlyCC() throws IOException {
		super("Q21528959");
	}

	@Override
	protected Map<String, String> additionalQualifiers() {
		return new HashMap<String, String>();
	}

	@Override
	protected PropertyConstraintChecker getPropertyChecker(String property, Map<String, String> qualifiers) throws IOException {
		return new UsedForReferencesOnlyPCC(property, qualifiers);
	}

}
