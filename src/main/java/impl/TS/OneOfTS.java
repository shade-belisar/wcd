package impl.TS;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import main.Main;

public class OneOfTS extends TripleSet {
	
	Set<String> values = new HashSet<String>();
	
	Set<String> properties;

	public OneOfTS(Set<String> properties_) throws IOException {
		properties = properties_;
	}
	
	public Set<String> getValues() throws IOException {
		if (Main.getExtract())
			return values;
		else {
			Set<String> result = triple.getEntrySet(3);
			result.addAll(qualifier.getEntrySet(2));
			result.addAll(reference.getEntrySet(2));
			return result;
		}
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		if (properties.contains(predicate)) {
			super.triple(id, subject, predicate, object);
			values.add(object);
		}
		
	}
	
	@Override
	protected void qualifier(String id, String predicate, String object) {
		if (properties.contains(predicate)) {
			super.qualifier(id, predicate, object);
			values.add(object);
		}
		
	}
	
	@Override
	protected void reference(String id, String predicate, String object) {
		if (properties.contains(predicate)) {
			super.reference(id, predicate, object);
			values.add(object);
		}
	}

	@Override
	protected String getTripleSetType() {
		return "OneOf";
	}

}
