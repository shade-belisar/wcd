package impl.DS;

import static utility.SC.statementEDB;
import static utility.SC.qualifierEDB;
import static utility.SC.referenceEDB;

import static utility.SC.item;
import static utility.SC.property;

import static utility.SC.unit;
import static utility.SC.rank;

import static utility.SC.first;
import static utility.SC.next;
import static utility.SC.last;

import static utility.SC.first_qualifier;
import static utility.SC.next_qualifier;
import static utility.SC.last_qualifier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.vlog4j.core.model.api.Predicate;
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

public class DataSet implements EntityDocumentProcessor {
	
	public enum DataSetPredicate {
		STATEMENT(statementEDB),
		QUALIFIER(qualifierEDB),
		REFERENCE(referenceEDB),

		ITEM(item),
		PROPERTY(property),

		UNIT(unit),
		RANK(rank),

		FIRST(first),
		NEXT(next),
		LAST(last),

		FIRST_QUALIFIER(first_qualifier),
		NEXT_QUALIFIER(next_qualifier),
		LAST_QUALIFIER(last_qualifier);
		
	    private final Predicate predicate;
	    
	    DataSetPredicate(Predicate predicate) {
	    	this.predicate = predicate;
	    }
	    
	    public Predicate getPredicate() {
	    	return predicate;
	    }
	}
	
	static final Logger logger = LoggerFactory.getLogger(DataSet.class);
	
	Map<DataSetPredicate, DataSetFile> files = new HashMap<>();

	String lastID;
	String lastSubject;

	String lastQualifierID;
	String lastQualifierPredicate;
	String lastQualifierValue;

	public DataSet() throws IOException {
		
		for(DataSetPredicate predicate : DataSetPredicate.values()) {
			files.put(predicate, new DataSetFile(predicate.toString(), predicate.getPredicate()));
		}
		
		Main.registerProcessor(this);
	}
	
	public DataSetFile getFile(DataSetPredicate predicate) throws IOException {
		return files.get(predicate);
	}
	
	public void loadFile(DataSetPredicate predicate, Reasoner reasoner) throws IOException, ReasonerStateException {
		DataSetFile file = files.get(predicate);
		
		if (predicate.equals(DataSetPredicate.LAST)) {
			if (!file.isClosed() && lastID != null) {
				file.write(lastID, lastSubject);
			}
		}
		
		if (predicate.equals(DataSetPredicate.LAST_QUALIFIER)) {
			if (!file.isClosed() && lastQualifierID != null)
				file.write(lastQualifierID, lastQualifierPredicate, lastQualifierValue);
		}
		
		file.loadFile(reasoner);
	}
	
	void processStatement(String id, String subject, String predicate, String object) {
		DataSetFile statementFile = files.get(DataSetPredicate.STATEMENT);
		
		DataSetFile firstFile = files.get(DataSetPredicate.FIRST);
		DataSetFile nextFile = files.get(DataSetPredicate.NEXT);
		DataSetFile lastFile = files.get(DataSetPredicate.LAST);
		
		statementFile.write(id, subject, predicate, object);
		
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
		DataSetFile qualifierFile = files.get(DataSetPredicate.QUALIFIER);

		DataSetFile firstQualifier = files.get(DataSetPredicate.FIRST_QUALIFIER);
		DataSetFile nextQualifier = files.get(DataSetPredicate.NEXT_QUALIFIER);
		DataSetFile lastQualifier = files.get(DataSetPredicate.LAST_QUALIFIER);

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
	
	void processReference(String id, String hash, String predicate, String object) {
		DataSetFile referenceFile = files.get(DataSetPredicate.REFERENCE);
		referenceFile.write(id, hash, predicate, object);
	}
	
	void processUnit(String object, Value value) {
		DataSetFile unitsFile = files.get(DataSetPredicate.UNIT);
		
		String unit = "";
		if (value != null) {
			unit = value.accept(new OutputUnitVisitor());
		}
		if (unit != null) {
			unitsFile.write(object, unit);
		}
	}
	
	void processRank(String id, StatementRank rank) {
		DataSetFile ranksFile = files.get(DataSetPredicate.RANK);
		
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
				processStatement(id, subject, predicate, object);
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
				// TODO this is not extracting the actual hash, but it does not seem to be available
				int semiHash = 0;
				for (Reference reference : statement.getReferences()) {
					for (SnakGroup rGroup : reference.getSnakGroups()) {
						String reference_predicate = rGroup.getProperty().getIri();
						for (Snak snak : rGroup) {
							Value reference_value = snak.getValue();
							String reference_object = "";
							if (reference_value != null) {
								reference_object = reference_value.accept(new OutputValueVisitor());
							}
							processReference(id, String.valueOf(semiHash), reference_predicate, reference_object);
							processUnit(reference_object, reference_value);
						}
					}
					semiHash++;
				}
			}
		}
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		String subject = itemDocument.getEntityId().getIri();
		DataSetFile itemsFile = files.get(DataSetPredicate.ITEM);
		
		itemsFile.write(subject);

		processStatementDocument(itemDocument);		
	}
	
	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		String subject = propertyDocument.getEntityId().getIri();
		DataSetFile propertiesFile = files.get(DataSetPredicate.PROPERTY);
		
		propertiesFile.write(subject);
		
		processStatementDocument(propertyDocument);
	}
	
	public void close() throws IOException {
		for (Map.Entry<DataSetPredicate, DataSetFile> entry : files.entrySet()) {
			DataSetPredicate predicate = entry.getKey();
			DataSetFile file = entry.getValue();
			
			if (predicate.equals(DataSetPredicate.LAST)) {
				if (!file.isClosed() && lastID != null) {
					file.write(lastID, lastSubject);
				}
			}
			
			if (predicate.equals(DataSetPredicate.LAST_QUALIFIER)) {
				if (!file.isClosed() && lastQualifierID != null) {
					file.write(lastQualifierID, lastQualifierPredicate, lastQualifierValue);
				}
			}
			
			file.close();
		}
	}
}
