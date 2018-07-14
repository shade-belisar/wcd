package wikidata.constraints.datalog.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import wikidata.constraints.datalog.main.ConstraintChecker;
import wikidata.constraints.datalog.main.PropertyConstraintChecker;

public class ConflictsWithCC extends ConstraintChecker {
	
	public static final String PROPERTY = "property";
	public static final String ITEM_OF_PROPERTY_CONSTRAINT = "itemOfPropertyConstraint";

	public ConflictsWithCC() throws IOException {
		super("Q21502838");
	}

	@Override
	protected Map<String, String> additionalQualifiers() {
		Map<String, String> result = new HashMap<String, String>();
		result.put(PROPERTY, "P2306");
		result.put(ITEM_OF_PROPERTY_CONSTRAINT, "P2305");
		return result;	}

	@Override
	protected PropertyConstraintChecker getPropertyChecker(String property, Map<String, String> qualifiers) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
