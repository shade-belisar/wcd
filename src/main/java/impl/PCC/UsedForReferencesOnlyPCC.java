package impl.PCC;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import impl.CC.ScopeCC;

public class UsedForReferencesOnlyPCC extends ScopePCC {

	public UsedForReferencesOnlyPCC(String property_) throws IOException {
		super(property_, new HashSet<String>());
	}
	
	@Override
	protected boolean allowedAs(String qualifier) {
		if (qualifier.equals(ScopeCC.AS_REFERENCE))
			return true;
		return false;
	}
}
