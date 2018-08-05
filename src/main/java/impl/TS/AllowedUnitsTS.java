package impl.TS;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import utility.OutputUnitVisitor;
import utility.OutputValueVisitor;

public class AllowedUnitsTS extends TripleSet {
	
	final TripleSetFile unitsFile;
	
	final Set<String> units = new HashSet<String>();

	public AllowedUnitsTS(String property_) throws IOException {
		super(property_);
		unitsFile = new TripleSetFile(getTripleSetType(), property + "units");
	}
	
	public Set<String> getUnits() {
		return units;
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
				if (predicate.endsWith(property)) {
					triple(id, subject, predicate, object);
					String unit = value.accept(new OutputUnitVisitor());
					if (unit != null) {
						unitsFile.write(object, unit);
						units.add(unit);
					}
				}
					
				
				for (SnakGroup qualifier : statement.getClaim().getQualifiers()) {
					String qualifier_predicate = qualifier.getProperty().getIri();
					for (Snak snak : qualifier) {
						Value qualifier_value = snak.getValue();
						String qualifier_object = "";
						if (qualifier_value != null) {
							qualifier_object = qualifier_value.accept(new OutputValueVisitor());
						}
						if (qualifier_predicate.endsWith(property)) {
							qualifier(id, qualifier_predicate, qualifier_object);
							String qualifier_unit = qualifier_value.accept(new OutputUnitVisitor());
							if (qualifier_unit  != null) {
								unitsFile.write(qualifier_object, qualifier_unit);
								units.add(qualifier_unit);
							}
						}
						
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
							if (reference_predicate.endsWith(property)) {
								reference(id, reference_predicate, reference_object);
								String reference_unit = reference_value.accept(new OutputUnitVisitor());
								if (reference_unit != null) {
									unitsFile.write(reference_object, reference_unit);
									units.add(reference_unit);
								}
							}
							
						}
					}
				}
			}
			
			
		}
	}
	
	@Override
	public boolean notEmpty() {
		return super.notEmpty() || unitsNotEmpty();
	}
	
	public boolean unitsNotEmpty() {
		return unitsFile.notEmpty();
	}
	
	public File getUnitsFile() throws IOException {
		return unitsFile.getFile();
	}
	
	@Override
	public void delete() throws IOException {
		super.delete();
		unitsFile.delete();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		unitsFile.close();			
	}

	@Override
	protected String getTripleSetType() {
		return "AllowedUnitsTs";
	}

}
