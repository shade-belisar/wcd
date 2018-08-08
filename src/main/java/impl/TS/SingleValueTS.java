package impl.TS;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import utility.OutputValueVisitor;

public class SingleValueTS extends TripleSet {
	
	Map<String, Set<String>> qualifiers;
	
	Set<String> statementIDs = new HashSet<String>();
	
	Set<String> qualifierValues = new HashSet<String>();
	
	Set<String> referenceValues = new HashSet<String>();
	
	TripleSetFile firstQualifier = new TripleSetFile(getTripleSetType(), "firstQualifier");
	TripleSetFile nextQualifier = new TripleSetFile(getTripleSetType(), "nextQualifier");
	TripleSetFile lastQualifier = new TripleSetFile(getTripleSetType(), "lastQualifier");
	
	String lastID;
	String lastPredicate;
	String lastValue;

	public SingleValueTS(Map<String, Set<String>> qualifiers_) throws IOException {
		qualifiers = qualifiers_;
	}
	
	@Override
	public boolean notEmpty() {
		return super.notEmpty();
	}
	
	public boolean firstQualifierNotEmpty() {
		return firstQualifier.notEmpty();
	}
	
	public boolean nextQualifierNotEmpty() {
		return nextQualifier.notEmpty();
	}
	
	public boolean lastQualifierNotEmpty() {
		return lastQualifier.notEmpty() || lastID != null;
	}
	
	public File getFirstQualifierFile() throws IOException {
		return firstQualifier.getFile();
	}
	
	public File getNextQualifierFile() throws IOException {
		return nextQualifier.getFile();
	}
	
	public File getLastQualifierFile() throws IOException {
		if (!lastQualifier.isClosed() && lastID != null)
			lastQualifier.write(lastID, lastPredicate, lastValue);
		return lastQualifier.getFile();
	}
	
	@Override
	public void processStatementDocument(StatementDocument statementDocument) {
		String subject = statementDocument.getEntityId().getIri();
		for (StatementGroup	sg : statementDocument.getStatementGroups()) {
			String predicate = sg.getProperty().getIri();
			for (Statement statement : sg) {
				String id = statement.getStatementId();
				Value value = statement.getValue();
				String object = "";
				if (value != null) {
					object = value.accept(new OutputValueVisitor());
				}
				if (qualifiers.containsKey(predicate))
					triple(id, subject, predicate, object);
				
				for (SnakGroup qualifier : statement.getClaim().getQualifiers()) {
					String qualifier_predicate = qualifier.getProperty().getIri();
					for (Snak snak : qualifier) {
						Value qualifier_value = snak.getValue();
						String qualifier_object = "";
						if (qualifier_value != null) {
							qualifier_object = qualifier_value.accept(new OutputValueVisitor());
						}
						boolean writeQualifier = false;
						if (qualifiers.containsKey(predicate)) {
							if (qualifiers.get(predicate).contains(qualifier_predicate))
								writeQualifier = true;
						} else if (qualifiers.containsKey(qualifier_predicate))
							writeQualifier = true;
						if (writeQualifier)
							qualifier(id, qualifier_predicate, qualifier_object);						
					}
				}
				for (Reference reference : statement.getReferences()) {
					for (SnakGroup rGroup : reference.getSnakGroups()) {
						String reference_predicate = rGroup.getProperty().getIri();
						for (Snak snak : rGroup) {
							Value reference_value = snak.getValue();
							String reference_object = "";
							if (reference_value != null) {
								reference_object = reference_value.accept(new OutputValueVisitor());
							}
							if (qualifiers.containsKey(reference_predicate))
								reference(id, reference_predicate, reference_object);
						}
					}
				}
			}
		}
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		super.triple(id, subject, predicate, object);
		statementIDs.add(id);
	}
	
	@Override
	protected void qualifier(String id, String predicate, String object) {
		super.qualifier(id, predicate, object);
		
		qualifierValues.add(object);
		
		if (lastID == null)
			firstQualifier.write(id, predicate, object);
		else if (!lastID.equals(id)) {
			lastQualifier.write(lastID, lastPredicate, lastValue);
			firstQualifier.write(id, predicate, object);
		} else {
			nextQualifier.write(lastID, lastPredicate, lastValue, id, predicate, object);
		}
			
		lastID = id;
		lastPredicate = predicate;
		lastValue = object;
	}
	
	@Override
	protected void reference(String id, String predicate, String object) {
		super.reference(id, predicate, object);
		referenceValues.add(object);
	}
	
	@Override
	public void delete() throws IOException {
		super.delete();
		firstQualifier.deleteRawFile();
		nextQualifier.deleteRawFile();
		lastQualifier.deleteRawFile();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		firstQualifier.close();
		nextQualifier.close();
		if (!lastQualifier.isClosed() && lastID != null) {
			lastQualifier.write(lastID, lastPredicate, lastValue);
		}
		lastQualifier.close();
	}
	
	public Set<String> getStatementIDs() {
		return statementIDs;
	}
	
	public Set<String> getQualifierValues() {
		return qualifierValues;
	}

	@Override
	protected String getTripleSetType() {
		return "SingleValue";
	}

}
