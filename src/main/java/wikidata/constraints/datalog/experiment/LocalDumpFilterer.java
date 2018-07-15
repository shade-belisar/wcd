package wikidata.constraints.datalog.rdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.wikidata.wdtk.datamodel.interfaces.Claim;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.json.jackson.JacksonStatement;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;

public class LocalDumpFilterer implements EntityDocumentProcessor {
	
	private final static String DUMP_FILE = "./resources/sample-dump-20150815.json.gz";
	
	public static void main(String[] args) {
		ExampleHelpers.configureLogging();
		
		DumpProcessingController dumpProcessingController = new DumpProcessingController("wikidatawiki");
		
		LocalDumpFilterer filterer = new LocalDumpFilterer();
		dumpProcessingController.registerEntityDocumentProcessor(filterer, null, false);
		
		MwLocalDumpFile mwDumpFile = new MwLocalDumpFile(DUMP_FILE);

		dumpProcessingController.processDump(mwDumpFile);
	}

	@Override
	public void processItemDocument(ItemDocument itemDocument) {		
		Iterator<Statement> statements = itemDocument.getAllStatements();
		while (statements.hasNext()) {
			Statement statement = statements.next();
			Claim claim = statement.getClaim();
			//System.out.println(statement.getStatementId() + "\t" + claim.getSubject() + "\t" + claim.getMainSnak().getPropertyId() + "\t" + claim.getValue());
		}
		
		EntityIdValue subject = itemDocument.getEntityId();
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
			PropertyIdValue predicate = sg.getProperty();
			//if (!predicate.getId().equals("P31"))
			//	continue;
			for (Statement statement : sg) {
				Value object = statement.getValue();
				for (Reference reference : statement.getReferences()) {
					//System.out.println(reference);
				}
				statement.getClaim().getAllQualifiers();
				System.out.println(statement.getStatementId() + "\t" + subject + "\t" + predicate + "\t" + object.accept(new OutputValueVisitor()));
				
			}
		}
	}

	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {
		//System.out.println(propertyDocument);
		
	}

}
