package experiment;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;

public class MinimalExample {
	
	final static Logger logger = Logger.getLogger(MinimalExample.class);
	
	public static void main(String[] args) throws Exception {
		
		configureLogging();
		
		Predicate predicate = Expressions.makePredicate("predicate", 1);
		
		Reasoner reasoner = Reasoner.getInstance();
		
		DataSource dataSource = new CsvFileDataSource(new File("test.csv"));
		reasoner.addFactsFromDataSource(predicate, dataSource);
		
		reasoner.load();
		
		reasoner.close();
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