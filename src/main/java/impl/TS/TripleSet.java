package impl.TS;

import java.io.File;
import java.io.IOException;

import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.StatementRank;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import main.Main;
import utility.OutputUnitVisitor;
import utility.OutputValueVisitor;
import utility.SC;

public class TripleSet implements EntityDocumentProcessor {
	
	static final Logger logger = LoggerFactory.getLogger(TripleSet.class);
	
	TripleSetFile tripleFile;
	TripleSetFile qualifierFile;
	TripleSetFile referenceFile;
	
	TripleSetFile itemsFile;
	TripleSetFile propertiesFile;
	
	TripleSetFile unitsFile;
	TripleSetFile ranksFile;
	
	TripleSetFile firstFile;
	TripleSetFile nextFile;
	TripleSetFile lastFile;
	
	TripleSetFile firstQualifier;
	TripleSetFile nextQualifier;
	TripleSetFile lastQualifier;

	String lastID;
	String lastSubject;

	String lastQualifierID;
	String lastQualifierPredicate;
	String lastQualifierValue;

	public TripleSet() throws IOException {
		
		tripleFile = new TripleSetFile("triple", SC.tripleEDB);
		qualifierFile = new TripleSetFile("qualifier", SC.qualifierEDB);
		referenceFile = new TripleSetFile("reference", SC.referenceEDB);
		
		itemsFile = new TripleSetFile("items", SC.item);
		propertiesFile = new TripleSetFile("properties", SC.property);
		
		unitsFile = new TripleSetFile("units", SC.unit);
		ranksFile = new TripleSetFile("ranks", SC.rank);
		
		firstFile = new TripleSetFile("first", SC.first);
		nextFile = new TripleSetFile("next", SC.next);
		lastFile = new TripleSetFile("last", SC.last);
		
		firstQualifier = new TripleSetFile("firstQualifier", SC.first_qualifier);
		nextQualifier = new TripleSetFile("nextQualifier", SC.next_qualifier);
		lastQualifier = new TripleSetFile("lastQualifier", SC.last_qualifier);
		
		Main.registerProcessor(this);
	}
	
	public File getTripleFile() throws IOException {
		return tripleFile.getFile();
	}
	
	public File getQualifierFile() throws IOException {
		return qualifierFile.getFile();
	}
	
	public File getReferenceFile() throws IOException {
		return referenceFile.getFile();
	}
	
	public File getItemsFile() throws IOException {
		return itemsFile.getFile();
	}
	
	public File getPropertiesFile() throws IOException {
		return propertiesFile.getFile();
	}
	
	public File getUnitsFile() throws IOException {
		return unitsFile.getFile();
	}
	
	public File getRanksFile() throws IOException {
		return ranksFile.getFile();
	}
	
	public void loadTripleFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		tripleFile.loadFile(reasoner);
	}
	
	public void loadQualifierFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		qualifierFile.loadFile(reasoner);
	}
	
	public void loadReferenceFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		referenceFile.loadFile(reasoner);
	}

	public void loadItemsFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		itemsFile.loadFile(reasoner);
	}
	
	public void loadPropertiesFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		propertiesFile.loadFile(reasoner);
	}
	
	public void loadUnitsFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		unitsFile.loadFile(reasoner);
	}
	
	public void loadRanksFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		ranksFile.loadFile(reasoner);
	}
	
	public void loadFirstFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		firstFile.loadFile(reasoner);
	}
	
	public void loadNextFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		nextFile.loadFile(reasoner);
	}
	
	public void loadLastFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		if (!lastFile.isClosed() && lastID != null) {
			lastFile.write(lastID, lastSubject);
		}
		lastFile.loadFile(reasoner);
	}
	
	public void loadFirstQualifierFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		firstQualifier.loadFile(reasoner);
	}
	
	public void loadNextQualifierFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		nextQualifier.loadFile(reasoner);
	}
	
	public void loadLastQualifierFile(Reasoner reasoner) throws IOException, ReasonerStateException {
		if (!lastQualifier.isClosed() && lastQualifierID != null)
			lastQualifier.write(lastQualifierID, lastQualifierPredicate, lastQualifierValue);
		lastQualifier.loadFile(reasoner);
	}
	
	void processTriple(String id, String subject, String predicate, String object) {
		tripleFile.write(id, subject, predicate, object);
		
		if (lastID == null)
			firstFile.write(id, subject);
		else if (!lastSubject.equals(subject)) {
			lastFile.write(lastID, lastSubject);
			firstFile.write(id, subject);
		} else {
			nextFile.write(lastID, id);
		}
			
		lastID = id;
		lastSubject = subject;
	}
	
	void processQualifier(String id, String predicate, String object) {
		qualifierFile.write(id, predicate, object);
		
		if (lastQualifierID == null)
			firstQualifier.write(id, predicate, object);
		else if (!lastQualifierID.equals(id)) {
			lastQualifier.write(lastQualifierID, lastQualifierPredicate, lastQualifierValue);
			firstQualifier.write(id, predicate, object);
		} else {
			nextQualifier.write(lastQualifierID, lastQualifierPredicate, lastQualifierValue, id, predicate, object);
		}
			
		lastQualifierID = id;
		lastQualifierPredicate = predicate;
		lastQualifierValue = object;
	}
	
	void processUnit(String object, Value value) {
		String unit = "";
		if (value != null) {
			unit = value.accept(new OutputUnitVisitor());
		}
		if (unit != null) {
			unitsFile.write(object, unit);
		}
	}
	
	void processRank(String id, StatementRank rank) {
		ranksFile.write(id, rank.toString());
	}
	
	public void processStatementDocument(StatementDocument statementDocument) {
		String subject = statementDocument.getEntityId().getIri();
		for (StatementGroup	sg : statementDocument.getStatementGroups()) {
			String predicate = sg.getProperty().getIri();
			for (Statement statement : sg) {
				String id = statement.getStatementId();
				Value value = statement.getValue();
				StatementRank rank = statement.getRank();
				String object = "";
				if (value != null) {
					object = value.accept(new OutputValueVisitor());					
				}
				processTriple(id, subject, predicate, object);
				processUnit(object, value);
				processRank(id, rank);
				
				for (SnakGroup qualifier : statement.getClaim().getQualifiers()) {
					String qualifier_predicate = qualifier.getProperty().getIri();
					for (Snak snak : qualifier) {
						Value qualifier_value = snak.getValue();
						String qualifier_object = "";
						if (qualifier_value != null) {
							qualifier_object = qualifier_value.accept(new OutputValueVisitor());
						}
						processQualifier(id, qualifier_predicate, qualifier_object);
						processUnit(qualifier_object, qualifier_value);
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
							referenceFile.write(id, reference_predicate, reference_object);
							processUnit(reference_object, reference_value);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		String subject = itemDocument.getEntityId().getIri();
		itemsFile.write(subject);

		processStatementDocument(itemDocument);		
	}
	
	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		String subject = propertyDocument.getEntityId().getIri();
		
		propertiesFile.write(subject);
		
		processStatementDocument(propertyDocument);
	}
	
	public void close() throws IOException {
		tripleFile.close();
		qualifierFile.close();
		referenceFile.close();
		
		itemsFile.close();
		propertiesFile.close();
		
		unitsFile.close();
		ranksFile.close();
		
		firstFile.close();
		nextFile.close();
		if (!lastFile.isClosed() && lastID != null) {
			lastFile.write(lastID, lastSubject);
		}
		lastFile.close();
		
		firstQualifier.close();
		nextQualifier.close();
		if (!lastQualifier.isClosed() && lastQualifierID != null) {
			lastQualifier.write(lastQualifierID, lastQualifierPredicate, lastQualifierValue);
		}
		lastQualifier.close();
	}
}
