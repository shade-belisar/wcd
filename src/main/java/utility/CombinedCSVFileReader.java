package utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import utility.InequalityHelper.InequalityHandler;

public class CombinedCSVFileReader {
	
	final static Map<File, Map<InequalityHandler, Set<Integer>>> files = new HashMap<>();

	private CombinedCSVFileReader(){}
	
	public static void register (InequalityHandler helper) {
		for (IndexedCSVFile csvFile : helper.indexedFiles()) {
			File file = csvFile.getFile();
			if (!files.containsKey(file))
				files.put(file, new HashMap<>());
			
			Map<InequalityHandler, Set<Integer>> helperMap = files.get(file);
			if (!helperMap.containsKey(helper))
				helperMap.put(helper, new HashSet<>());
			helperMap.get(helper).addAll(csvFile.getIndexes());
		}
	}
	
	public static void run() throws IOException {
		for (Map.Entry<File, Map<InequalityHandler, Set<Integer>>> entry : files.entrySet()) {
			File file = entry.getKey();
			Map<InequalityHandler, Set<Integer>> helperMap = entry.getValue();
			
			Iterator<CSVRecord> csvIterator = CSVFormat.DEFAULT.parse(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))).iterator();
			
			while(csvIterator.hasNext()) {
				CSVRecord record = csvIterator.next();
				
				for (Map.Entry<InequalityHandler, Set<Integer>> helperMapEntry : helperMap.entrySet()) {
					InequalityHandler helper = helperMapEntry.getKey();
					Set<Integer> indexes = helperMapEntry.getValue();
					
					for (Integer integer : indexes) {
						helper.encoded(record.get(integer));
					}
				}
			}
		}
	}
}
