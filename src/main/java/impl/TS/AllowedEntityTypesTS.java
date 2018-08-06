package impl.TS;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

public class AllowedEntityTypesTS extends TripleSet {
	
	Set<String> properties;
	
	final TripleSetFile itemsFile;
	final TripleSetFile propertiesFile;

	public AllowedEntityTypesTS(Set<String> properties_) throws IOException {
		itemsFile = new TripleSetFile(getTripleSetType(), "items");
		propertiesFile = new TripleSetFile(getTripleSetType(), "properties");
		properties = properties_;
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		String subject = itemDocument.getEntityId().getIri();
		
		itemsFile.write(subject);
		
		super.processItemDocument(itemDocument);
	}
	
	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		String subject = propertyDocument.getEntityId().getIri();
		
		propertiesFile.write(subject);
		
		super.processPropertyDocument(propertyDocument);
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		if (properties.contains(predicate))
			super.triple(id, subject, predicate, object);
	}
	
	@Override
	public boolean notEmpty() {
		return super.notEmpty() || itemsNotEmpty() || propertiesNotEmpty();
	}
	
	public boolean itemsNotEmpty() {
		return itemsFile.notEmpty();
	}
	
	public boolean propertiesNotEmpty() {
		return propertiesFile.notEmpty();
	}
	
	public File getItemsFile() throws IOException {
		return itemsFile.getFile();
	}
	
	public File getPropertiesFile() throws IOException {
		return propertiesFile.getFile();
	}
	
	@Override
	public void delete() throws IOException {
		super.delete();
		itemsFile.deleteRawFile();
		propertiesFile.deleteRawFile();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		itemsFile.close();
		propertiesFile.close();			
	}

	@Override
	protected String getTripleSetType() {
		return "AllowedEntityTypes";
	}

}
