package utility;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.csv.CSVRecord;

public class CombinedIterator implements Iterator<String> {

	Iterator<CSVRecord> csvIterator1;
	
	int csvIndex1;
	
	Iterator<CSVRecord> csvIterator2;
	
	int csvIndex2;
	
	Iterator<String> stringIterator;
	
	public CombinedIterator(Iterator<CSVRecord> csvIterator1_, int csvIndex1_, Iterator<CSVRecord> csvIterator2_, int csvIndex2_, Iterator<String> stringIterator_) {
		csvIterator1 = csvIterator1_;
		csvIndex1 = csvIndex1_;
		csvIterator2 = csvIterator2_;
		csvIndex2 = csvIndex2_;
		stringIterator = stringIterator_;
		
	}

	@Override
	public boolean hasNext() {
		if (csvIterator1 != null)
			if (csvIterator1.hasNext())
				return true;
		if (csvIterator2 != null)
			if (csvIterator2.hasNext())
				return true;
		if (stringIterator != null)
			if (stringIterator.hasNext())
				return true;
		return false;
	}

	@Override
	public String next() {
		if (csvIterator1 != null)
			if (csvIterator1.hasNext())
				return csvIterator1.next().get(csvIndex1);
		if (csvIterator2 != null)
			if (csvIterator2.hasNext())
				return csvIterator2.next().get(csvIndex2);
		if (stringIterator != null)
			if (stringIterator.hasNext())
				return stringIterator.next();
		throw new NoSuchElementException();
	}

}
