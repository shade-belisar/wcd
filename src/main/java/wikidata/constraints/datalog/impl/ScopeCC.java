package wikidata.constraints.datalog.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import wikidata.constraints.datalog.impl.PCC.ScopePCC;
import wikidata.constraints.datalog.main.ConstraintChecker;
import wikidata.constraints.datalog.main.PropertyConstraintChecker;

public class ScopeCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ScopeCC.class);
	
	public static final String SCOPE = "P5314";
	
	public static final String AS_MAIN_VALUE = "Q54828448";
	public static final String AS_QUALIFIER = "Q54828449";
	public static final String AS_REFERENCE = "Q54828450";

	public ScopeCC() throws IOException {
		super("Q53869507");
	}

	@Override
	protected Set<String> additionalQualifiers() {
		Set<String> result = new HashSet<String>();
		result.add(SCOPE);
		return result;
	}
	
	@Override
	protected PropertyConstraintChecker getPropertyChecker(String property, Map<String, String> qualifiers) throws IOException {
		return new ScopePCC(property, qualifiers);
	}
}
