package impl.DS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
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

public class DataSetFile {
	
	static final Logger logger = LoggerFactory.getLogger(DataSetFile.class);
	
	static String BASE_LOCATION = "./resources/dataSets/";
	
	String fileName;
	
	Predicate predicate;
	
	File dataSetFileGz;
	
	CSVPrinter writer;
	
	boolean closed = true;
	
	public DataSetFile(String name, Predicate predicate_) throws IOException {
		fileName = BASE_LOCATION + "/" + name + ".csv.gz";
		predicate = predicate_;
		
		dataSetFileGz = new File(fileName);
		dataSetFileGz.getParentFile().mkdirs();
		dataSetFileGz.createNewFile();
		
		closed = false;
		
		if (Main.getExtract())
			writer = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(dataSetFileGz, false)))), CSVFormat.DEFAULT);
	}
	
	public void forceWrite() throws IOException {
		writer = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(dataSetFileGz, false)))), CSVFormat.DEFAULT);
	}
	
	public void write(String...strings) {
		write(Arrays.asList(strings));
	}
	
	public void write(List<String> strings) {
		if (writer == null) {
			logger.warn("Trying to write to file " + fileName + " but extraction has not been activated.");
			return;
		}
			
		try {
			writer.printRecord(strings);
			writer.flush();
		} catch (IOException e) {
			logger.error("Could not write line to file " + dataSetFileGz.getAbsolutePath(), e);
		}
	}
	
	public void loadFile(Reasoner reasoner) throws ReasonerStateException, IOException {
		close();
		CsvGzFileDataSource dataSource = new CsvGzFileDataSource(dataSetFileGz);
		reasoner.addFactsFromDataSource(predicate, dataSource);
	}
	
	public File getFile() throws IOException {
		close();
		return dataSetFileGz;
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
