/**
 * 
 */
package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor;
import org.wikidata.wdtk.dumpfiles.MwDumpFile;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;

import impl.CC.AllowedEntityTypesCC;
import impl.CC.ConflictsWithCC;
import impl.CC.ConstraintChecker;
import impl.CC.DistinctValuesCC;
import impl.CC.ItemRequiresStatementCC;
import impl.CC.NoneOfCC;
import impl.CC.ScopeCC;
import impl.CC.SingleValueCC;
import impl.CC.TypeCC;

/**
 * @author adrian
 *
 */
public class Main {
	
	final static Logger logger = Logger.getLogger(Main.class);
	
	final static String DUMP_FILE = "./resources/sample-dump-20150815.json.gz";
	
	static DumpProcessingController dumpProcessingController;
	
	static boolean onlyCurrentRevisions = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("d", "download", false, "Download the current JSON dump.");
		options.addOption("l", "local", false, "Process local example dump.");
		options.addOption("h", "help", false, "Displays this help.");
		
		CommandLineParser parser = new DefaultParser();
	    CommandLine cmd;
	    
	    try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("There was an error while parsing the command line input.");
		    HelpFormatter formatter = new HelpFormatter();
		    formatter.printHelp("help", options);
		    return;
		}
	    
	    if (cmd.hasOption("help")) {
	    	HelpFormatter formatter = new HelpFormatter();
	        formatter.printHelp("help", options);
	        return;
	    }
		
		configureLogging();
		
		dumpProcessingController = new DumpProcessingController("wikidatawiki");
		if (cmd.hasOption("download")) {
			dumpProcessingController.setOfflineMode(false);
		} else {
			dumpProcessingController.setOfflineMode(true);
		}
		

		List<ConstraintChecker> checkers = new ArrayList<ConstraintChecker>();
		checkers.add(new ScopeCC());
		//checkers.add(new ItemRequiresStatementCC());
		//checkers.add(new AllowedEntityTypesCC());
		//checkers.add(new ConflictsWithCC());
		//checkers.add(new DistinctValuesCC());
		//checkers.add(new NoneOfCC());
		//checkers.add(new SingleValueCC());
		try {
			for (ConstraintChecker constraintChecker : checkers) {
				constraintChecker.init();
			}
		} catch (IOException e) {
			logger.error("Could not open a file, see the error message for details.", e);
			return;
		}
		
		// Add timer for progress
		EntityTimerProcessor time = new EntityTimerProcessor(0);
		dumpProcessingController.registerEntityDocumentProcessor(time, null, onlyCurrentRevisions);
		
		MwDumpFile mwDumpFile;
		if (cmd.hasOption("local")){
			mwDumpFile = new MwLocalDumpFile(DUMP_FILE);
		} else {
			mwDumpFile = dumpProcessingController.getMostRecentDump(DumpContentType.JSON);
		}

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
		dumpProcessingController.registerEntityDocumentProcessor(processor, null, onlyCurrentRevisions);
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
