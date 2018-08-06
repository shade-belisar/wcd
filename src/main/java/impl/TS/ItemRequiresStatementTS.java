package impl.TS;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import utility.OutputValueVisitor;

public class ItemRequiresStatementTS extends TripleSet {
	
	Set<String> allProperties = new HashSet<String>();
	Set<String> allValues = new HashSet<String>();

	TripleSetFile first = new TripleSetFile(getTripleSetType(), property + "first");
	TripleSetFile next = new TripleSetFile(getTripleSetType(), property + "next");
	TripleSetFile last = new TripleSetFile(getTripleSetType(), property + "last");
	
	String lastID;
	String lastSubject;

	public ItemRequiresStatementTS(String property_) throws IOException {
		super(property_);
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
		return next.notEmpty();
	}
	
	public File getFirstFile() throws IOException {
		return first.getFile();
	}
	
	public File getNextFile() throws IOException {
		return next.getFile();
	}
	
	public File getLastFile() throws IOException {
		if (!last.isClosed())
			last.write(lastID, lastSubject);
		return last.getFile();
	}
	
	public Set<String> allProperties() {
		return allProperties;
	}
	
	public Set<String> allValues() {
		return allValues;
	}
	
	@Override
	public void processStatementDocument(StatementDocument statementDocument) {
		boolean foundProperty = false;
		
		String subject = statementDocument.getEntityId().getIri();
		for (StatementGroup sg : statementDocument.getStatementGroups()) {
			if (foundProperty)
				break;
			String predicate = sg.getProperty().getIri();
			if (predicate.endsWith(property)) {
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
		if (!last.isClosed()) {
			last.write(lastID, lastSubject);
			last.close();
		}
			
	}

	@Override
	protected String getTripleSetType() {
		return "ItemRequiresStatement";
	}

}
