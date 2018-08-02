package impl.TS;

import java.io.IOException;
import java.util.Map;

import impl.CC.ScopeCC;
import main.Main;

/**
 * A triple set limited to all triples containing the property as predicate.
 * @author adrian
 *
 */
public class ScopeTS extends TripleSet {

	public ScopeTS(String property_) throws IOException {
		super(property_);
	}
	
	@Override
	protected String getTripleSetType() {
		return "PropertyAsPredicate";
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
