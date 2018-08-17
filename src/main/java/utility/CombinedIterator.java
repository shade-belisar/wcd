package utility;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.csv.CSVRecord;

public class CombinedIterator implements Iterator<String> {

	Iterator<CSVRecord> csvIterator;
	
	Iterator<String> stringIterator;
	
	int csvIndex;
	
	public CombinedIterator(Iterator<CSVRecord> csvIterator_, int csvIndex_, Iterator<String> stringIterator_) {
		csvIterator = csvIterator_;
		stringIterator = stringIterator_;
		csvIndex = csvIndex_;
	}

	@Override
	public boolean hasNext() {
		return csvIterator.hasNext() || stringIterator.hasNext();
	}

	@Override
	public String next() {
		if (csvIterator.hasNext())
			return csvIterator.next().get(csvIndex);
		if (stringIterator.hasNext())
			return stringIterator.next();
		throw new NoSuchElementException();
	}

}
