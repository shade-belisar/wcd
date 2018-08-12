package impl.TS;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import main.Main;
import utility.OutputUnitVisitor;
import utility.OutputValueVisitor;

public class AllowedUnitsTS extends TripleSet {
	
	final TripleSetFile unitsFile;
	
	Set<String> units = new HashSet<String>();
	
	Set<String> properties;

	public AllowedUnitsTS(Set<String> properties_) throws IOException {
		unitsFile = new TripleSetFile(getTripleSetType(), "units");
		properties = properties_;		
	}
	
	public Set<String> getUnits() throws IOException {
		if (Main.getExtract())
			return units;
		else
			return unitsFile.getEntrySet(1);
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
				String unit = "";
				if (value != null) {
					object = value.accept(new OutputValueVisitor());
					unit = value.accept(new OutputUnitVisitor());
				}
				if (properties.contains(predicate)) {
					triple(id, subject, predicate, object);
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
						if (properties.contains(qualifier_predicate)) {
							qualifier(id, qualifier_predicate, qualifier_object);
							String qualifier_unit = null;
							if (qualifier_value != null) {
								qualifier_unit = qualifier_value.accept(new OutputUnitVisitor());
							}
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
							if (reference_value != null)
								reference_object = reference_value.accept(new OutputValueVisitor());
							if (properties.contains(reference_predicate)) {
								reference(id, reference_predicate, reference_object);
								String reference_unit = "";
								if (reference_value != null)
									reference_unit = reference_value.accept(new OutputUnitVisitor());
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
		unitsFile.deleteRawFile();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		unitsFile.close();			
	}

	@Override
	protected String getTripleSetType() {
		return "AllowedUnits";
	}

}
