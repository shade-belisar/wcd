package wikidata.constraints.datalog.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wikidata.constraints.datalog.main.ConstraintChecker;
import wikidata.constraints.datalog.main.PropertyConstraintChecker;

public class ConflictsWithCC extends ConstraintChecker {
	
	public static final String PROPERTY = "P2306";
	public static final String ITEM_OF_PROPERTY_CONSTRAINT = "P2305";

	public ConflictsWithCC() throws IOException {
		super("Q21502838");
	}

	@Override
	protected Set<String> additionalQualifiers() {
		Set<String> result = new HashSet<String>();
		result.add(PROPERTY);
		result.add(ITEM_OF_PROPERTY_CONSTRAINT);
		return result;	}

	@Override
	protected PropertyConstraintChecker getPropertyChecker(String property, Map<String, String> qualifiers) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
