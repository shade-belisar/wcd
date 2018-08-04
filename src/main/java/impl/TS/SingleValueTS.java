package impl.TS;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import utility.OutputValueVisitor;

public class SingleValueTS extends TripleSet {
	
	Set<String> qualifiers = new HashSet<String>();
	
	Set<String> statementIDs = new HashSet<String>();

	public SingleValueTS(String property_, Set<String> qualifiers_) throws IOException {
		super(property_);
		qualifiers = qualifiers_;
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		String subject = itemDocument.getEntityId().getIri();
		for (StatementGroup	sg : itemDocument.getStatementGroups()) {
			String predicate = sg.getProperty().getIri();
			if (!predicate.endsWith(property))
				continue;
			for (Statement statement : sg) {
				String id = statement.getStatementId();
				statementIDs.add(id);
				Value value = statement.getValue();
				String object = "";
				if (value != null) {
					object = value.accept(new OutputValueVisitor());
				}
				triple(id, subject, predicate, object);
				
				for (SnakGroup qualifier : statement.getClaim().getQualifiers()) {
					String qualifier_predicate = qualifier.getProperty().getIri();
					if (!qualifiers.contains(qualifier_predicate))
						continue;
					for (Snak snak : qualifier) {
						String qualifier_object = snak.getValue().accept(new OutputValueVisitor());
						qualifier(id, qualifier_predicate, qualifier_object);
					}
				}
			}
		}
	}
	
	public Set<String> getStatementIDs() {
		return statementIDs;
	}

	@Override
	protected String getTripleSetType() {
		return "SingleValue";
	}

}
