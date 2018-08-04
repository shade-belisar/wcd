package impl.TS;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import main.Main;
import utility.OutputValueVisitor;

public abstract class TripleSet implements EntityDocumentProcessor {
	
	static final Logger logger = LoggerFactory.getLogger(TripleSet.class);
	
	protected String property;
	
	TripleSetFile triple;
	
	TripleSetFile qualifier;
	
	TripleSetFile reference;
	
	public TripleSet(String property_) throws IOException {
		property = property_;
		
		triple = new TripleSetFile(getTripleSetType(), property);
		qualifier = new TripleSetFile(getTripleSetType(), property + "qualifier");
		reference = new TripleSetFile(getTripleSetType(), property + "reference");
		
		Main.registerProcessor(this);
	}
	
	protected final void write(String id, String subject, String predicate, String object) {
		triple.write(id, subject, predicate, object);
		
	}	
	
	protected final void writeQualifier(String id, String predicate, String object) {
		qualifier.write(id, predicate, object);
		
	}
	
	protected final void writeReference(String id, String predicate, String object) {
		reference.write(id, predicate, object);		
	}
	
	public boolean notEmpty() {
		return tripleNotEmpty() || qualifierNotEmpty() || referenceNotEmpty();
	}
	
	public boolean tripleNotEmpty() {
		return triple.notEmpty();
	}
	
	public boolean qualifierNotEmpty() {
		return qualifier.notEmpty();
	}
	
	public boolean referenceNotEmpty() {
		return reference.notEmpty();
	}
	
	public File getTripleFile() throws IOException {
		return triple.getFile();
	}
	
	public File getQualifierFile() throws IOException {
		return qualifier.getFile();
	}
	
	public File getReferenceFile() throws IOException {
		return reference.getFile();
	}
	
	protected void triple(String id, String subject, String predicate, String object) {
		write(id, subject, predicate, object);
	}
	
	protected void qualifier(String id, String predicate, String object) {
		writeQualifier(id, predicate, object);
	}
	
	protected void reference(String id, String predicate, String object) {
		writeReference(id, predicate, object);
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		String subject = itemDocument.getEntityId().getIri();
		for (StatementGroup	sg : itemDocument.getStatementGroups()) {
			String predicate = sg.getProperty().getIri();
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
					for (Snak snak : qualifier) {
						Value qualifier_value = snak.getValue();
						String qualifier_object = "";
						if (qualifier_value != null) {
							qualifier_object = qualifier_value.accept(new OutputValueVisitor());
						}
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
							reference(id, reference_predicate, reference_object);
						}
					}
				}
			}
		}		
	}
	
	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		
	}
	
	public void delete() throws IOException {
		triple.delete();
		qualifier.delete();
		reference.delete();
	}
	
	public void close() throws IOException {
		triple.close();
		qualifier.close();
		reference.close();
	}
	
	protected abstract String getTripleSetType();
	
	protected static String listToCSV(List<String> strings) {
		String result = "";
		for (String string : strings) {
			result += string + ",";
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}

}
