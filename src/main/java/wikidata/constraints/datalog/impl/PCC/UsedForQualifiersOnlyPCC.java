package wikidata.constraints.datalog.impl.PCC;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import wikidata.constraints.datalog.impl.CC.ScopeCC;

public class UsedForQualifiersOnlyPCC extends ScopePCC {

	public UsedForQualifiersOnlyPCC(String property_) throws IOException {
		super(property_, new HashSet<String>());
	}
	
	@Override
	protected boolean allowedAs(String qualifier) {
		if (qualifier.equals(ScopeCC.AS_QUALIFIER))
			return true;
		return false;
	}
}
