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
	
	String name;
	
	final String fileName;
	
	File tripleSetFileGz;
	
	File tripleSetFile;
	
	CSVPrinter writer;
	
	boolean closed = true;
	
	boolean unzipped = false;
	
	public TripleSetFile(String name_) throws IOException {
		name = name_;
		fileName = BASE_LOCATION + "/" + name + ".csv";
		
		tripleSetFileGz = new File(fileName + ".gz");
		tripleSetFileGz.getParentFile().mkdirs();
		tripleSetFileGz.createNewFile();
		
		closed = false;
		
		if (Main.getExtract())
			writer = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(tripleSetFileGz, false)))), CSVFormat.DEFAULT);
	}
	
	String getFileName() {
		return fileName;
	}
	
	String getFileNameGz() {
		return fileName + ".gz";
	}
	
	public void write(String...strings) {
		if (writer == null) {
			logger.warn("Trying to write to file " + getFileNameGz() + " but extraction has not been activated.");
			return;
		}
			
		try {
			writer.printRecord(Arrays.asList(strings));
			writer.flush();
		} catch (IOException e) {
			logger.error("Could not write line to file " + tripleSetFileGz.getAbsolutePath(), e);
		}
	}
	
	public void openUnzipped() throws IOException {
		close();

		tripleSetFile = new File(fileName); 

		GZIPInputStream gzippedInput = new GZIPInputStream(new FileInputStream(tripleSetFileGz));
		FileOutputStream unzippedOutput = new FileOutputStream(tripleSetFile);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = gzippedInput.read(buffer)) != -1) {
			unzippedOutput.write(buffer, 0, len);
		}
		gzippedInput.close();

		unzippedOutput.close();
	}
	
	public File getFile() throws IOException {
		close();

		if (tripleSetFile == null)
			openUnzipped();

		return tripleSetFile;
	}

	public void deleteRawFile() {
		tripleSetFile = null;
		new File(getFileName()).delete();
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
