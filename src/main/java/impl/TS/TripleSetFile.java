package impl.TS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.Main;

public class TripleSetFile {
	
	static final Logger logger = LoggerFactory.getLogger(TripleSetFile.class);
	
	static String BASE_LOCATION = "./resources/tripleSets/";
	
	String folder;
	
	String name;
	
	final String fileName;
	
	File tripleSetFileGz;
	
	File tripleSetFile;
	
	CSVPrinter writer;
	
	boolean closed = true;
	
	boolean tripleNotEmpty = false;
	
	public TripleSetFile(String folder_, String name_) throws IOException {
		folder = folder_;
		name = name_;
		fileName = BASE_LOCATION + folder + "/" + name + ".csv";
		
		tripleSetFileGz = new File(fileName + ".gz");
		tripleSetFileGz.getParentFile().mkdirs();
		tripleNotEmpty = !tripleSetFileGz.createNewFile();
		
		closed = false;
		
		if (Main.extract())
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
			tripleNotEmpty = true;
		} catch (IOException e) {
			logger.error("Could not write line to file " + tripleSetFileGz.getAbsolutePath(), e);
		}
	}
	
	public boolean notEmpty() {
		return tripleNotEmpty;
	}
	
	public File getFile() throws IOException {
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

		return tripleSetFile;
	}

	public void deleteRawFile() {
		new File(getFileName()).delete();
	}

	public void close() throws IOException {
		if (writer != null) {
			writer.close();
			closed = true;
			if (!notEmpty()) {
				new File(getFileNameGz()).delete();
			}
		}
	}
	
	public boolean isClosed() {
		return closed;
	}

}
