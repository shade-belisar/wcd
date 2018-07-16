package wikidata.constraints.datalog.impl.TS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripleSetFile {
	
	static final Logger logger = LoggerFactory.getLogger(TripleSetFile.class);
	
	static String BASE_LOCATION = "./resources/tripleSets/";
	
	String folder;
	
	String name;
	
	File tripleSetFile;
	
	CSVPrinter writer;
	
	boolean tripleNotEmpty = false;
	
	public TripleSetFile(String folder_, String name_) throws IOException {
		folder = folder_;
		name = name_;
		
		tripleSetFile = new File(BASE_LOCATION + folder + "/" + name + ".csv");
		tripleSetFile.getParentFile().mkdirs();
		tripleSetFile.createNewFile();
		
		writer = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tripleSetFile, false))), CSVFormat.DEFAULT);
	}
	
	public void write(String...strings) {
		try {
			writer.printRecord(Arrays.asList(strings));
			writer.flush();
			tripleNotEmpty = true;
		} catch (IOException e) {
			logger.error("Could not write line to file " + tripleSetFile.getAbsolutePath(), e);
		}
	}
	
	public boolean notEmpty() {
		return tripleNotEmpty;
	}
	
	public File getFile() throws IOException {
		close();
		return tripleSetFile;
	}
	
	public void close() throws IOException {
		writer.close();
	}

}
