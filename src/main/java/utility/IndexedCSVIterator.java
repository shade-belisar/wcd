package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class IndexedCSVIterator implements Iterator<String> {
	
	final File csvFile;
	
	final List<Integer> csvIndexes;
	
	final Iterator<CSVRecord> csvIterator;
	
	Iterator<String> lineIterator;
	
	public IndexedCSVIterator(File csvFile_, Set<Integer> csvIndexes_) throws IOException {
		csvFile = csvFile_;
		csvIndexes = new ArrayList<>(csvIndexes_);
		csvIterator = CSVFormat.DEFAULT.parse(new InputStreamReader(new GZIPInputStream(new FileInputStream(csvFile)))).iterator();
	}

	@Override
	public boolean hasNext() {
		return csvIterator.hasNext() || lineIterator.hasNext();
	}

	@Override
	public String next() {
		if (lineIterator == null)
			updateIterator();
		while (!lineIterator.hasNext()) {
			updateIterator();
		}
		return lineIterator.next();
	}
	
	void updateIterator() {
		if (!csvIterator.hasNext())
			throw new NoSuchElementException(); 
		List<String> currentLine = new ArrayList<>();
		CSVRecord record = csvIterator.next();
		for (Integer index : csvIndexes) {
			currentLine.add(record.get(index));
		}
		lineIterator = currentLine.iterator();
	}
}
