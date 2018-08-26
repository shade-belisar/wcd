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
import org.apache.commons.lang3.time.StopWatch;
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
import impl.CC.AllowedQualifiersCC;
import impl.CC.AllowedUnitsCC;
import impl.CC.ConflictsWithCC;
import impl.CC.ConstraintChecker;
import impl.CC.DistinctValuesCC;
import impl.CC.InverseCC;
import impl.CC.ItemRequiresStatementCC;
import impl.CC.MandatoryQualifierCC;
import impl.CC.MultiValueCC;
import impl.CC.NoneOfCC;
import impl.CC.OneOfCC;
import impl.CC.OneOfQualifierValueCC;
import impl.CC.ScopeCC;
import impl.CC.SingleBestValueCC;
import impl.CC.SingleValueCC;
import impl.CC.SymmetricCC;
import impl.CC.ValueRequiresStatementCC;
import impl.TS.TripleSet;
import utility.InequalityHelper;

/**
 * @author adrian
 *
 */
public class Main {
	
	final static Logger logger = Logger.getLogger(Main.class);
	
	final static String DUMP_FILE = "./resources/sample-dump-20150815.json.gz";
	
	static DumpProcessingController dumpProcessingController;
	
	static boolean onlyCurrentRevisions = true;
	
	static boolean extract = false;
	
	static boolean stringResults = false;
	
	public static TripleSet tripleSet;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("d", "download", false, "Download the current JSON dump.");
		options.addOption("l", "local", false, "Process local example dump.");
		options.addOption("h", "help", false, "Displays this help.");
		options.addOption("e", "extract", false, "Extract the necessary data from the dump.");
		options.addOption("c", "constraints", true, "The constraint to check.");
		options.addOption("n", "noviolations", false, "Do not compute violations.");
		options.addOption("s", "stringResults", false, "Output violations as string.");
		options.addOption("i", "inequalityMode", true, "Choose the inequality mode. Default is encoded.");
		
		CommandLineParser parser = new DefaultParser();
	    CommandLine cmd;
	    HelpFormatter formatter = new HelpFormatter();
	    
	    try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("There was an error while parsing the command line input.");
		    formatter.printHelp("help", options);
		    return;
		}
	    
	    if (cmd.hasOption("help")) {
	        formatter.printHelp("help", options);
	        return;
	    }
	    
	    if (!cmd.hasOption("constraints")) {
	    	System.out.println("Please specify the constraints.");
		    formatter.printHelp("help", options);
		    return;
	    }
	    
	    if (!cmd.hasOption("inequalityMode"))
	    	InequalityHelper.mode = InequalityHelper.Mode.ENCODED;
	    else {
	    	String inequalityMode = cmd.getOptionValue("inequalityMode").toLowerCase();
	    	switch (inequalityMode) {
			case "naive":
				InequalityHelper.mode = InequalityHelper.Mode.NAIVE;
				break;
			case "encoded":
				InequalityHelper.mode = InequalityHelper.Mode.ENCODED;
				break;
			case "demanded":
				InequalityHelper.mode = InequalityHelper.Mode.DEMANDED;
				break;
			default:
				System.out.println("Inequality mode " + inequalityMode + " is unknown.");
				return;
			}
	    }
	    	
	    
	    extract = cmd.hasOption("extract");
	    stringResults = cmd.hasOption("stringResults");
		
		configureLogging();
		
		dumpProcessingController = new DumpProcessingController("wikidatawiki");
		if (cmd.hasOption("download")) {
			dumpProcessingController.setOfflineMode(false);
		} else {
			dumpProcessingController.setOfflineMode(true);
		}
		
		try {
			tripleSet = new TripleSet();
		} catch (IOException e) {
			logger.error("Could not open a file, see the error message for details.", e);
		}

		List<ConstraintChecker> checkers = new ArrayList<ConstraintChecker>();
		try {
			for (String constraintName : cmd.getOptionValue("constraints").trim().split(",")) {
				switch (constraintName.toLowerCase()) {
				case "scope":
					checkers.add(new ScopeCC());
					break;
				case "conflictswith":
					checkers.add(new ConflictsWithCC());
					break;
				case "allowedentitytypes":
					checkers.add(new AllowedEntityTypesCC());
					break;
				case "noneof":
					checkers.add(new NoneOfCC());
					break;
				case "distinctvalues":
					checkers.add(new DistinctValuesCC());
					break;
				case "allowedunits":
					checkers.add(new AllowedUnitsCC());
					break;
				case "allowedqualifiers":
					checkers.add(new AllowedQualifiersCC());
					break;
				case "oneof":
					checkers.add(new OneOfCC());
					break;
				case "oneofqualifiervalue":
					checkers.add(new OneOfQualifierValueCC());
					break;
				case "itemrequiresstatement":
					checkers.add(new ItemRequiresStatementCC());
					break;
				case "valuerequiresstatement":
					checkers.add(new ValueRequiresStatementCC());
					break;
				case "inverse":
					checkers.add(new InverseCC());
					break;
				case "symmetric":
					checkers.add(new SymmetricCC());
					break;
				case "multivalue":
					checkers.add(new MultiValueCC());
					break;
				case "singlevalue":
					checkers.add(new SingleValueCC());
					break;
				case "mandatoryqualifier":
					checkers.add(new MandatoryQualifierCC());
					break;
				case "singlebestvalue":
					checkers.add(new SingleBestValueCC());
					break;
				default:
					System.out.println("Constraint " + constraintName + " is unknown.");
					return;
				}
			}
		} catch (IOException e) {
			logger.error("Could not open a file, see the error message for details.", e);
			return;
		}
		
		if (extract) {			
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
				Main.tripleSet.close();
			} catch (IOException e) {
				logger.error("Error closing the triple set files.", e);
				return;
			}
		}
		
		if (!cmd.hasOption("noviolations")) {
			try {
				for(ConstraintChecker checker : checkers) {
					StopWatch watch = new StopWatch();
					watch.start();
					checker.violations();
					watch.stop();
					System.out.println("Total time elapsed: " + watch.getTime() + "ms");
					System.out.println(checker.identify() + ", violations: " + checker.getResultSize());
					if (getStringResult())
						System.out.println(checker.getResultString());
					
				}
			} catch (ReasonerStateException e) {
				logger.error("Reasoner was called in the wrong state.", e);
			} catch (IOException e) {
				logger.error("Could not open a file", e);
			}
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
	
	public static boolean getExtract() {
		return extract;
	}
	
	public static boolean getStringResult() {
		return stringResults;
	}

}
