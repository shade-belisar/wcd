package impl.TS;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import main.Main;
import utility.OutputValueVisitor;

public class ItemRequiresStatementTS extends TripleSet {
	
	Set<String> allProperties = new HashSet<String>();
	Set<String> allValues = new HashSet<String>();

	TripleSetFile first = new TripleSetFile(getTripleSetType(), "first");
	TripleSetFile next = new TripleSetFile(getTripleSetType(), "next");
	TripleSetFile last = new TripleSetFile(getTripleSetType(), "last");
	
	String lastID;
	String lastSubject;
	
	Set<String> properties;

	public ItemRequiresStatementTS(Set<String> properties_) throws IOException {
		properties = properties_;
	}
	
	@Override
	public boolean notEmpty() {
		return super.notEmpty() || firstNotEmpty() || nextNotEmpty() || lastNotEmpty();
	}
	
	public boolean firstNotEmpty() {
		return first.notEmpty();
	}
	
	public boolean nextNotEmpty() {
		return next.notEmpty();
	}
	
	public boolean lastNotEmpty() {
		return last.notEmpty() || lastID != null;
	}
	
	public File getFirstFile() throws IOException {
		return first.getFile();
	}
	
	public File getNextFile() throws IOException {
		return next.getFile();
	}
	
	public File getLastFile() throws IOException {
		if (!last.isClosed() && lastID != null) {
			last.write(lastID, lastSubject);
		}
		return last.getFile();
	}
	
	public Set<String> allProperties() throws IOException {
		if (Main.getExtract())
			return allProperties;
		else
			return triple.getEntrySet(2);
	}
	
	public Set<String> allValues() throws IOException {
		if (Main.getExtract())
			return allValues;
		else
			return triple.getEntrySet(3);
	}
	
	@Override
	public void processStatementDocument(StatementDocument statementDocument) {
		boolean foundProperty = false;
		
		String subject = statementDocument.getEntityId().getIri();
		for (StatementGroup sg : statementDocument.getStatementGroups()) {
			if (foundProperty)
				break;
			String predicate = sg.getProperty().getIri();
			if (properties.contains(predicate)) {
				foundProperty = true;
				break;
			}
		}
		if (!foundProperty)
			return;
		for (StatementGroup	sg : statementDocument.getStatementGroups()) {
			String predicate = sg.getProperty().getIri();
			for (Statement statement : sg) {
				String id = statement.getStatementId();
				Value value = statement.getValue();
				String object = "";
				if (value != null) {
					object = value.accept(new OutputValueVisitor());
				}
				triple(id, subject, predicate, object);
			}
		}
	}

	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		super.triple(id, subject, predicate, object);
		
		allProperties.add(predicate);
		allValues.add(object);
		
		if (lastID == null)
			first.write(id, subject);
		else if (!lastSubject.equals(subject)) {
			last.write(lastID, lastSubject);
			first.write(id, subject);
		} else {
			next.write(lastID, id);
		}
			
		lastID = id;
		lastSubject = subject;
	}
	
	@Override
	public void delete() throws IOException {
		super.delete();
		first.deleteRawFile();
		next.deleteRawFile();
		last.deleteRawFile();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		first.close();
		next.close();
		if (!last.isClosed() && lastID != null) {
			last.write(lastID, lastSubject);
		}
		last.close();
	}

	@Override
	protected String getTripleSetType() {
		return "ItemRequiresStatement";
	}

}
