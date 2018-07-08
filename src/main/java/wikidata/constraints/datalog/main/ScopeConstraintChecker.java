package wikidata.constraints.datalog.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

public class ScopeConstraintChecker extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ScopeConstraintChecker.class);
	
	public final static String SCOPE = "scope";
	
	public static final String AS_MAIN_VALUE = "Q54828448";
	public static final String AS_QUALIFIER = "Q54828449";
	public static final String AS_REFERENCE = "Q54828450";

	public ScopeConstraintChecker() throws IOException {
		super("Q53869507");
	}

	@Override
	public String violations() throws ReasonerStateException, IOException {
		String result = "";
		for (PropertyConstraintChecker propertyConstraintChecker : propertyCheckers) {
			result += propertyConstraintChecker.violations() + "\n";
		}
		return result;
	}

	@Override
	PropertyConstraintChecker getPropertyChecker(String property, Map<String, String> qualifiers) throws IOException {
		return new ScopePropertyConstraintChecker(property, qualifiers);
	}

	@Override
	Map<String, String> additionalQualifiers() {
		Map<String, String> result = new HashMap<String, String>();
		result.put(SCOPE, "P5314");
		return result;
	}
}
