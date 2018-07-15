package wikidata.constraints.datalog.rdf;

import java.io.IOException;
import java.util.Map;

import wikidata.constraints.datalog.impl.CC.ScopeCC;
import wikidata.constraints.datalog.main.Main;

public class ScopeTripleSet extends TripleSet {

	public ScopeTripleSet(String property_) throws IOException {
		super(property_);
	}
	
	@Override
	protected String getTripleSetType() {
		return "property";
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		if (predicate.endsWith(property))
			write(id, subject, predicate, object);
	}
	
	@Override
	protected void qualifier(String id, String predicate, String object) {
		if (predicate.endsWith(property))
			writeQualifier(id, predicate, object);
	}

	@Override
	protected void reference(String id, String predicate, String object) {
		if (predicate.endsWith(property))
			writeReference(id, predicate, object);
	}
}
