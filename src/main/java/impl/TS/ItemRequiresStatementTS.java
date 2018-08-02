package impl.TS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;

import utility.OutputValueVisitor;

public class ItemRequiresStatementTS extends TripleSet {
	
	TripleSetFile first;
	
	TripleSetFile next;
	
	TripleSetFile last;
	
	String lastId;
	
	String lastSubject;
	
	Map<String, HashSet<String>> propertiesPerItem;
	
	Map<String, HashSet<String>> valuesPerProperty;

	public ItemRequiresStatementTS(String property_) throws IOException {
		super(property_);
		
		first = new TripleSetFile(getTripleSetType(), property + "first");
		next = new TripleSetFile(getTripleSetType(), property + "next");
		last = new TripleSetFile(getTripleSetType(), property + "last");
		
		propertiesPerItem = new HashMap<String, HashSet<String>>();
		valuesPerProperty = new HashMap<String, HashSet<String>>();
	}
	
	protected final void writeFirst(String statement, String item) {
		first.write(statement, item);
	}
	
	protected final void writeNext(String statement, String otherStatement) {
		next.write(statement, otherStatement);
	}
	
	protected final void writeLast(String statement, String item) {
		last.write(statement, item);
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
			writeLast(lastId, lastSubject);
		return last.getFile();
	}
	
	@Override
	protected void triple(String id, String subject, String predicate, String object) {
		super.triple(id, subject, predicate, object);
		
		if (!propertiesPerItem.containsKey(subject))
			propertiesPerItem.put(subject, new HashSet<String>());
		
		propertiesPerItem.get(subject).add(predicate);
		
		if (!valuesPerProperty.containsKey(predicate))
			valuesPerProperty.put(predicate, new HashSet<String>());
		
		valuesPerProperty.get(predicate).add(object);
		
		if (lastSubject == null) {
			first(id, subject);
		} else if (!lastSubject.equals(subject)) {
			last(lastId, lastSubject);
			first(id, subject);
		} else {
			next(lastId, id);
		}
		lastId = id;
		lastSubject = subject;
	}
	
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		boolean foundProperty = false;
		
		String subject = itemDocument.getEntityId().getIri();
		for (StatementGroup sg : itemDocument.getStatementGroups()) {
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
		for (StatementGroup	sg : itemDocument.getStatementGroups()) {
			String predicate = sg.getProperty().getIri();
			for (Statement statement : sg) {
				String id = statement.getStatementId();
				String object = statement.getValue().accept(new OutputValueVisitor());
				triple(id, subject, predicate, object);
			}
		}
	}
	
	protected void first(String statement, String item) {
		writeFirst(statement, item);
	}
	
	protected void next(String previousStatement, String nextStatement) {
		writeNext(previousStatement, nextStatement);
	}
	
	protected void last(String statement, String item) {
		writeLast(statement, item);
	}
	
	public List<Set<String>> getProperties() {
		List<Set<String>> result = new ArrayList<Set<String>>();
		for (Set<String> set : propertiesPerItem.values()) {
			result.add(set);
		}
		return result;
	}
	
	public Map<String, HashSet<String>> getValues() {
		return valuesPerProperty;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		first.close();
		next.close();
		if (!last.isClosed()) {
			writeLast(lastId, lastSubject);
			last.close();
		}
			
	}

	@Override
	protected String getTripleSetType() {
		return "ItemRequiresStatement";
	}
}
