/**
 * 
 */
package wikidata.constraints.datalog.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;

import wikidata.constraints.datalog.impl.ConflictsWithCC;
import wikidata.constraints.datalog.impl.ScopeCC;

/**
 * @author adrian
 *
 */
public class Main {
	
	final static Logger logger = Logger.getLogger(Main.class);
	
	final static String DUMP_FILE = "./resources/sample-dump-20150815.json.gz";
	
	public final static String BASE_URI = "http://www.wikidata.org/entity/";
	
	static DumpProcessingController dumpProcessingController;
	
	// Just for testing
	public static Set<String> properties = new HashSet<String>();
	public static Set<String> qualifiers = new HashSet<String>();
	public static Set<String> references = new HashSet<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		configureLogging();
		
		dumpProcessingController = new DumpProcessingController("wikidatawiki");

		List<ConstraintChecker> checkers = new ArrayList<ConstraintChecker>();
		checkers.add(new ConflictsWithCC());
		try {
			for (ConstraintChecker constraintChecker : checkers) {
				constraintChecker.init();
			}
		} catch (IOException e) {
			logger.error("Could not open a file, see the error message for details.", e);
			return;
		}
		
		MwLocalDumpFile mwDumpFile = new MwLocalDumpFile(DUMP_FILE);

		dumpProcessingController.processDump(mwDumpFile);
		
		try {
			for(ConstraintChecker checker : checkers)
				checker.close();
		} catch (IOException e) {
			logger.error("Could not close a file, see the error message for details.", e);
			return;
		}
		
		try {
			for(ConstraintChecker checker : checkers)
				System.out.println(checker.violations());
		} catch (ReasonerStateException e) {
			logger.error("Reasoner was called in the wrong state.", e);
			return;
		} catch (IOException e) {
			logger.error("Could not open a file", e);
			return;
		}

	}
	
	public static void registerProcessor(EntityDocumentProcessor processor) {
		dumpProcessingController.registerEntityDocumentProcessor(processor, null, false);
	}
	
	public static void configureLogging() {
		// Create the appender that will write log messages to the console.
		ConsoleAppender consoleAppender = new ConsoleAppender();
		// Define the pattern of log messages.
		// Insert the string "%c{1}:%L" to also show class name and line.
		String pattern = "%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n";
		consoleAppender.setLayout(new PatternLayout(pattern));
		// Change to Level.ERROR for fewer messages:
		consoleAppender.setThreshold(Level.INFO);

		consoleAppender.activateOptions();
		Logger.getRootLogger().addAppender(consoleAppender);
	}

}
