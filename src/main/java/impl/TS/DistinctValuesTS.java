package impl.TS;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DistinctValuesTS extends TripleSet {

	Set<String> statements = new HashSet<String>();

	public DistinctValuesTS(String property_) throws IOException {
		super(property_);
	}
	
	public Set<String> getStatements() {
		return statements;
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		if (predicate.endsWith(property)) {
			super.triple(id, subject, predicate, object);
			statements.add(id);
		}
			
	}

	@Override
	protected String getTripleSetType() {
		return "DistinctValues";
	}

}
