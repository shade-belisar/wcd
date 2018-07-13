package wikidata.constraints.datalog.impl.PCC;

import java.io.IOException;
import java.util.Map;

import wikidata.constraints.datalog.impl.ScopeConstraintChecker;

public class UsedForReferencesOnlyPCC extends ScopePCC {

	public UsedForReferencesOnlyPCC(String property_, Map<String, String> qualifiers_) throws IOException {
		super(property_, qualifiers_);
	}
	
	@Override
	protected boolean allowedAs(String qualifier) {
		if (qualifier.equals(ScopeConstraintChecker.AS_REFERENCE))
			return true;
		return false;
	}
}
