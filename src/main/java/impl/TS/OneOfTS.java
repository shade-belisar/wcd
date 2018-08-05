package impl.TS;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class OneOfTS extends TripleSet {
	
	Set<String> values = new HashSet<String>();

	public OneOfTS(String property_) throws IOException {
		super(property_);
	}
	
	public Set<String> getValues() {
		return values;
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		if (predicate.endsWith(property)) {
			super.triple(id, subject, predicate, object);
			values.add(object);
		}
		
	}
	
	@Override
	protected void qualifier(String id, String predicate, String object) {
		if (predicate.endsWith(property)) {
			super.qualifier(id, predicate, object);
			values.add(object);
		}
		
	}
	
	@Override
	protected void reference(String id, String predicate, String object) {
		if (predicate.endsWith(property)) {
			super.reference(id, predicate, object);
			values.add(object);
		}
	}

	@Override
	protected String getTripleSetType() {
		return "One-of";
	}

}
