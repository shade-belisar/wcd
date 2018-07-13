package wikidata.constraints.datalog.impl.PCC;

import java.io.IOException;
import java.util.Map;

import wikidata.constraints.datalog.impl.ScopeConstraintChecker;

public class UsedForValuesOnlyPCC extends ScopePCC {

	public UsedForValuesOnlyPCC(String property_, Map<String, String> qualifiers_) throws IOException {
		super(property_, qualifiers_);
	}
	
	@Override
	protected boolean allowedAs(String qualifier) {
		if (qualifier.equals(ScopeConstraintChecker.AS_MAIN_VALUE))
			return true;
		return false;
	}
}
