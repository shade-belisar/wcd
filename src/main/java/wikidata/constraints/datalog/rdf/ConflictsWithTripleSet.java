package wikidata.constraints.datalog.rdf;

import java.io.IOException;
import java.util.Map;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;

public class ConflictsWithTripleSet extends TripleSet {

	public ConflictsWithTripleSet(String property_, Map<String, String> quualifiers_) throws IOException {
		super(property_, quualifiers_);
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		boolean foundProperty = false;
		
		String subject = itemDocument.getEntityId().getIri();
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (foundProperty)
				break;
			for (Statement statement : sg) {
				String predicate = sg.getProperty().getIri();
				if (predicate.endsWith(property)) {
					foundProperty = true;
					break;
				}
			}
		}
		if (!foundProperty)
			return;
		for (StatementGroup	sg : itemDocument.getStatementGroups()) {
			String predicate = sg.getProperty().getIri();
			for (Statement statement : sg) {
				String id = statement.getStatementId();
				String object = statement.getValue().accept(new OutputValueVisitor());
				triple(id, subject, predicate, object);
			}
		}
	}

	@Override
	protected String getTripleSetType() {
		return "ConflictsWith";
	}

}
