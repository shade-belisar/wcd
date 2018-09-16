package impl.DS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
import positionVLog4J.PositionVLog4JHelper;
import utility.CsvGzFileDataSource;

public class DataSetFile {
	
	static final Logger logger = LoggerFactory.getLogger(DataSetFile.class);
	
	public final static String BASE_LOCATION = "./resources/dataSets/";
	
	String fileName;
	
	Predicate predicate;
	
	File dataSetFileGz;
	
	CSVPrinter writer;
	
	boolean closed = true;
	
	boolean didExist = false;
	
	public DataSetFile(String name, Predicate predicate) throws IOException {
		init("", name, predicate, Main.getExtract());
	}
	
	public DataSetFile(String name, Predicate predicate, boolean extract) throws IOException {
		init("", name, predicate, extract);
	}
	
	public DataSetFile(String folder, String name, Predicate predicate, boolean extract) throws IOException {
		init(folder, name, predicate, extract);
	}
	
	public void init(String folder, String name, Predicate predicate_, boolean extract) throws IOException {
		if (!folder.equals("") && !folder.endsWith("/"))
			folder += folder + "/";			
			
		fileName = BASE_LOCATION + folder + name + ".csv.gz";
		predicate = predicate_;
		
		dataSetFileGz = new File(fileName);
		dataSetFileGz.getParentFile().mkdirs();
		didExist = !dataSetFileGz.createNewFile();
		
		closed = false;
		
		if (extract)
			writer = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(dataSetFileGz, false)), StandardCharsets.UTF_8)), CSVFormat.DEFAULT);
	}
	
	public boolean didExist() {
		return didExist;
	}
	
	public void write(String...strings) {
		write(Arrays.asList(strings));
	}
	
	public void write(List<String> strings) {
		strings = PositionVLog4JHelper.sort(predicate, strings);
		if (writer == null) {
			logger.warn("Trying to write to file " + fileName + " but extraction has not been activated.");
			return;
		}
			
		try {
			writer.printRecord(strings);
		} catch (IOException e) {
			logger.error("Could not write line to file " + dataSetFileGz.getAbsolutePath(), e);
		}
	}
	
	public void loadFile(Reasoner reasoner) throws ReasonerStateException, IOException {
		close();
		CsvGzFileDataSource dataSource = new CsvGzFileDataSource(dataSetFileGz);
		reasoner.addFactsFromDataSource(predicate, dataSource);
	}
	
	public Predicate getPredicate() {
		return predicate;
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
