package impl.TS;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import utility.OutputValueVisitor;

public class OneOfQualifierValueTS extends TripleSet {
	
	Set<String> values = new HashSet<String>();
	
	Map<String, Set<String>> qualifierProperties;

	public OneOfQualifierValueTS(Map<String, Set<String>> qualifierProperties_) throws IOException {
		qualifierProperties = qualifierProperties_;
	}
	
	public Set<String> getValues() {
		return values;
	}
	
	@Override
	public void processStatementDocument(StatementDocument statementDocument) {
		String subject = statementDocument.getEntityId().getIri();
		for (StatementGroup	sg : statementDocument.getStatementGroups()) {
			String predicate = sg.getProperty().getIri();
			if (!qualifierProperties.containsKey(predicate))
				continue;
			for (Statement statement : sg) {
				String id = statement.getStatementId();
				Value value = statement.getValue();
				String object = "";
				if (value != null) {
					object = value.accept(new OutputValueVisitor());
				}
				triple(id, subject, predicate, object);
				
				for (SnakGroup qualifier : statement.getClaim().getQualifiers()) {
					String qualifier_predicate = qualifier.getProperty().getIri();
					if (!qualifierProperties.get(predicate).contains(qualifier_predicate))
						continue;
					for (Snak snak : qualifier) {
						String qualifier_object = snak.getValue().accept(new OutputValueVisitor());
						qualifier(id, qualifier_predicate, qualifier_object);
						values.add(qualifier_object);
					}
				}
			}
		}
	}

	@Override
	protected String getTripleSetType() {
		return "OneOfQualifierValue";
	}

}
