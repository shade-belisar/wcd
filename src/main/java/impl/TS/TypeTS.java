package impl.TS;

import java.io.IOException;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;

import utility.OutputValueVisitor;

public class TypeTS extends TripleSet {

	public TypeTS(String property_) throws IOException {
		super(property_);
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		boolean foundProperty = false;
		
		String subject = itemDocument.getEntityId().getIri();
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			if (foundProperty)
				break;
			String predicate = sg.getProperty().getIri();
			if (predicate.endsWith(property)) {
				foundProperty = true;
				break;
			}
		}
		if (!foundProperty)
			return;
		for (StatementGroup	sg : itemDocument.getStatementGroups()) {
			String predicate = sg.getProperty().getIri();
			if (!predicate.endsWith("P31"))
				return;
			for (Statement statement : sg) {
				String id = statement.getStatementId();
				String object = statement.getValue().accept(new OutputValueVisitor());
				triple(id, subject, predicate, object);
			}
		}
	}

	@Override
	protected String getTripleSetType() {
		return "Type";
	}

}
