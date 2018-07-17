package wikidata.constraints.datalog.impl.TS;

import java.io.File;
import java.io.IOException;

public class StatementItemOrderTS extends StatementsItemTS {
	
	TripleSetFile first;
	
	TripleSetFile next;
	
	TripleSetFile last;
	
	String lastId;
	
	String lastSubject;

	public StatementItemOrderTS(String property_) throws IOException {
		super(property_);
		
		first = new TripleSetFile(getTripleSetType(), property + "first");
		next = new TripleSetFile(getTripleSetType(), property + "next");
		last = new TripleSetFile(getTripleSetType(), property + "last");
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
	
	protected void first(String statement, String item) {
		writeFirst(statement, item);
	}
	
	protected void next(String statement, String otherStatement) {
		writeNext(statement, otherStatement);
	}
	
	protected void last(String statement, String item) {
		writeLast(statement, item);
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
}
