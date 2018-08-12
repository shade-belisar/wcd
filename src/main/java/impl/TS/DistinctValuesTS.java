package impl.TS;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import main.Main;

public class DistinctValuesTS extends TripleSet {

	Set<String> statements = new HashSet<String>();
	
	Set<String> properties;

	public DistinctValuesTS(Set<String> properties_) throws IOException {
		properties = properties_;
	}
	
	public Set<String> getStatements() throws IOException {
		if (Main.getExtract())
			return statements;
		else
			return triple.getEntrySet(0);
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		if (properties.contains(predicate)) {
			super.triple(id, subject, predicate, object);
			statements.add(id);
		}
	}
	
	@Override
	protected void qualifier(String id, String predicate, String object) {
	}
	
	@Override
	protected void reference(String id, String predicate, String object) {
	}

	@Override
	protected String getTripleSetType() {
		return "DistinctValues";
	}

}
