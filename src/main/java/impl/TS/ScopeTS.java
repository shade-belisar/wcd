package impl.TS;

import java.io.IOException;
import java.util.Set;

public class ScopeTS extends TripleSet {
	
	Set<String> properties;

	public ScopeTS(Set<String> properties_) throws IOException {
		properties = properties_;
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		if (properties.contains(predicate))
			super.triple(id, subject, predicate, object);
	}
	
	@Override
	protected void qualifier(String id, String predicate, String object) {
		if (properties.contains(predicate))
			super.qualifier(id, predicate, object);
	}
	
	@Override
	protected void reference(String id, String predicate, String object) {
		if (properties.contains(predicate))
			super.reference(id, predicate, object);
	}

	@Override
	protected String getTripleSetType() {
		return "Scope";
	}

}
