package impl.TS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.Main;
import utility.CsvGzFileDataSource;

public class TripleSetFile {
	
	static final Logger logger = LoggerFactory.getLogger(TripleSetFile.class);
	
	static String BASE_LOCATION = "./resources/tripleSets/";
	
	String fileName;
	
	Predicate predicate;
	
	File tripleSetFileGz;
	
	CSVPrinter writer;
	
	boolean closed = true;
	
	public TripleSetFile(String name, Predicate predicate_) throws IOException {
		fileName = BASE_LOCATION + "/" + name + ".csv.gz";
		predicate = predicate_;
		
		tripleSetFileGz = new File(fileName);
		tripleSetFileGz.getParentFile().mkdirs();
		tripleSetFileGz.createNewFile();
		
		closed = false;
		
		if (Main.getExtract())
			writer = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(tripleSetFileGz, false)))), CSVFormat.DEFAULT);
	}
	
	public void forceWrite() throws IOException {
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
	
	public void loadFile(Reasoner reasoner) throws ReasonerStateException, IOException {
		close();
		CsvGzFileDataSource dataSource = new CsvGzFileDataSource(tripleSetFileGz);
		reasoner.addFactsFromDataSource(predicate, dataSource);
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
