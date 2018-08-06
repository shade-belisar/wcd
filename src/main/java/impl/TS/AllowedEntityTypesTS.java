package impl.TS;

import java.io.File;
import java.io.IOException;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

public class AllowedEntityTypesTS extends TripleSet {
	
	final TripleSetFile items;
	final TripleSetFile properties;

	public AllowedEntityTypesTS(String property_) throws IOException {
		super(property_);
		items = new TripleSetFile(getTripleSetType(), property + "items");
		properties = new TripleSetFile(getTripleSetType(), property + "properties");
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		String subject = itemDocument.getEntityId().getIri();
		
		items.write(subject);
		
		super.processItemDocument(itemDocument);
	}
	
	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		String subject = propertyDocument.getEntityId().getIri();
		
		properties.write(subject);
		
		super.processPropertyDocument(propertyDocument);
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		if (predicate.endsWith(property))
			write(id, subject, predicate, object);
	}
	
	@Override
	public boolean notEmpty() {
		return super.notEmpty() || itemsNotEmpty() || propertiesNotEmpty();
	}
	
	public boolean itemsNotEmpty() {
		return items.notEmpty();
	}
	
	public boolean propertiesNotEmpty() {
		return properties.notEmpty();
	}
	
	public File getItemsFile() throws IOException {
		return items.getFile();
	}
	
	public File getPropertiesFile() throws IOException {
		return properties.getFile();
	}
	
	@Override
	public void delete() throws IOException {
		super.delete();
		items.delete();
		properties.delete();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		items.close();
		properties.close();			
	}

	@Override
	protected String getTripleSetType() {
		return "AllowedEntityTypes";
	}

}
