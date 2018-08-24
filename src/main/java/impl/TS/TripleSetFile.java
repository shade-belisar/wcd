package impl.TS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.Main;

public class TripleSetFile {
	
	static final Logger logger = LoggerFactory.getLogger(TripleSetFile.class);
	
	static String BASE_LOCATION = "./resources/tripleSets/";
	
	String fileName;
	
	File tripleSetFileGz;
	
	CSVPrinter writer;
	
	boolean closed = true;
	
	public TripleSetFile(String name) throws IOException {
		fileName = BASE_LOCATION + "/" + name + ".csv.gz";
		
		tripleSetFileGz = new File(fileName);
		tripleSetFileGz.getParentFile().mkdirs();
		tripleSetFileGz.createNewFile();
		
		closed = false;
		
		if (Main.getExtract())
			writer = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(tripleSetFileGz, false)))), CSVFormat.DEFAULT);
	}
	
	public void write(String...strings) {
		if (writer == null) {
			logger.warn("Trying to write to file " + fileName + " but extraction has not been activated.");
			return;
		}
			
		try {
			writer.printRecord(Arrays.asList(strings));
			writer.flush();
		} catch (IOException e) {
			logger.error("Could not write line to file " + tripleSetFileGz.getAbsolutePath(), e);
		}
	}
	
	public File getFile() throws IOException {
		close();
		
		return tripleSetFileGz;
	}

	public void close() throws IOException {
		if (writer != null) {
			writer.close();
			closed = true;
		}
	}
	
	public boolean isClosed() {
		return closed;
	}

}
