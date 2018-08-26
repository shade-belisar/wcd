package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class IndexedCSVIterator implements Iterator<String> {
	
	final File csvFile;
	
	final int csvIndex;
	
	final Iterator<CSVRecord> csvIterator;
	
	public IndexedCSVIterator(File csvFile_, int csvIndex_) throws IOException {
		csvFile = csvFile_;
		csvIndex = csvIndex_;
		csvIterator = CSVFormat.DEFAULT.parse(new InputStreamReader(new GZIPInputStream(new FileInputStream(csvFile)))).iterator();
	}

	@Override
	public boolean hasNext() {
		return csvIterator.hasNext();
	}

	@Override
	public String next() {
		return csvIterator.next().get(csvIndex);
	}
}
