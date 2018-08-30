package utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class IndexedCSVFile {
	
	final File file;
	
	final Set<Integer> indexes = new HashSet<>();
	
	public IndexedCSVFile(File file_, int...indexes_) {
		file = file_;
		for (int i : indexes_) {
			indexes.add(i);
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public void addIndex(int index) {
		indexes.add(index);
	}
	
	public Set<Integer> getIndexes() {
		return indexes;
	}
	
	public Iterator<String> getIterator() throws IOException {
		return new IndexedCSVIterator(file, indexes);
	}
}
