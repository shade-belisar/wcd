/**
 * 
 */
package main;

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

import impl.CC.ConflictsWithCC;
import impl.CC.ConstraintChecker;
import impl.CC.DistinctValuesCC;
import impl.CC.ItemRequiresStatementCC;
import impl.CC.NoneOfCC;
import impl.CC.ScopeCC;
import impl.CC.SingleValueCC;

/**
 * @author adrian
 *
 */
public class Main {
	
	final static Logger logger = Logger.getLogger(Main.class);
	
	final static String DUMP_FILE = "./resources/sample-dump-20150815.json.gz";
	
	static DumpProcessingController dumpProcessingController;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		configureLogging();
		
		dumpProcessingController = new DumpProcessingController("wikidatawiki");

		List<ConstraintChecker> checkers = new ArrayList<ConstraintChecker>();
		checkers.add(new DistinctValuesCC());
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
			for(ConstraintChecker checker : checkers) {
				String violations = checker.violations();
				if (!violations.equals("")) {
					System.out.println(violations);
				}
			}
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