/**
 * 
 */
package impl.CC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.web.HttpOp;
import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.QueryResult;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Algorithm;
import org.semanticweb.vlog4j.core.reasoner.DataSource;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.CsvFileDataSource;
import org.semanticweb.vlog4j.core.reasoner.implementation.QueryResultIterator;

import impl.PCC.PropertyConstraintChecker;
import impl.TS.TripleSet;
import impl.TS.TripleSet;
import utility.PrepareQueriesException;
import utility.SC;

/**
 * @author adrian
 *
 */
public abstract class ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ConstraintChecker.class);
	
	String constraint;
	
	protected final Reasoner reasoner = Reasoner.getInstance();
	
	protected final String internalError;
	
	protected List<PropertyConstraintChecker> propertyCheckers;
	
	public ConstraintChecker(String constraint_) {
		constraint = constraint_;
		internalError = "INTERNAL_ERROR for constraint " + constraint + ".";
		reasoner.setAlgorithm(Algorithm.RESTRICTED_CHASE);
	}

	public void init() throws IOException {
		Set<String> qualifiers = qualifiers();
		Set<String> concatQualifiers = concatQualifiers();
		
		String propertiesQuery =
		"#Tool: Wikidata Constraints Datalog https://github.com/Adrian-Bielefeldt/wcd\n"+
		"PREFIX wd: <http://www.wikidata.org/entity/>\n"+
		"PREFIX p: <http://www.wikidata.org/prop/>\n"+
		"PREFIX ps: <http://www.wikidata.org/prop/statement/>\n"+
		"PREFIX pq: <http://www.wikidata.org/prop/qualifier/>\n"+
		"SELECT ?item";
		for (String key : qualifiers) {
			propertiesQuery += " ?" + key + " ";
		}
		for (String key : concatQualifiers) {
			propertiesQuery += " (GROUP_CONCAT(DISTINCT (?var" + key + "); separator=',') as ?" + key + ")";
		}
		propertiesQuery += "\n"+
		"WHERE\n"+
		"{\n"+
		"  ?item p:P2302 ?s .\n"+
		"  ?s ps:P2302 wd:" + constraint + ".\n";
		for (String entry : qualifiers) {
			propertiesQuery += "  ?s pq:" + entry + " ?" + entry + ".\n";
		}
		for (String entry : concatQualifiers) {
			propertiesQuery += "  OPTIONAL {?s pq:" + entry + " ?var" + entry + "}.\n";
		}
		propertiesQuery +=
		"}\n"+
		"GROUP BY ?item";
		for (String key : qualifiers) {
			propertiesQuery += " ?" + key;
		}
		
		HttpOp.setUserAgent("Wikidata Constraints Datalog https://github.com/Adrian-Bielefeldt/wcd");

		Query query = QueryFactory.create(propertiesQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);

		ResultSet results = qexec.execSelect();
		
		while (results.hasNext()) {
			QuerySolution solution = results.next();
			//String property = solution.get("item").asResource().getLocalName();
			//if (!(property.equals("P6"))) 
			//	continue;

			process(solution);
		}       

		qexec.close();
		propertyCheckers = propertyCheckers();
	}
	
	public String violations() throws ReasonerStateException, IOException {
		close();
		prepareFacts();
		String result = "Constraint: " + constraint + "\n";
		List<Rule> rulesToAdd = new ArrayList<Rule>();
		for (PropertyConstraintChecker propertyConstraintChecker : propertyCheckers) {
			rulesToAdd.addAll(propertyConstraintChecker.rules());

		}
		try {
			result += prepareAndExecuteQueries(rulesToAdd, queries());
		} catch (PrepareQueriesException e) {
			return e.getMessage();
		}
		delete();
		return result;
	}
	
	protected void loadTripleSets(TripleSet... sets) throws ReasonerStateException, IOException {
		for (TripleSet tripleSet : sets) {
			if (tripleSet.tripleNotEmpty()) {
				final DataSource tripleEDBPath = new CsvFileDataSource(tripleSet.getTripleFile());
				reasoner.addFactsFromDataSource(SC.tripleEDB, tripleEDBPath);
			}
			if (tripleSet.qualifierNotEmpty()) {
				final DataSource qualifierEDBPath = new CsvFileDataSource(tripleSet.getQualifierFile());
				reasoner.addFactsFromDataSource(SC.qualifierEDB, qualifierEDBPath);
			}
			if (tripleSet.referenceNotEmpty()) {
				final DataSource referenceEDBPath = new CsvFileDataSource(tripleSet.getReferenceFile());
				reasoner.addFactsFromDataSource(SC.referenceEDB, referenceEDBPath);				
			}
		}	
	}
	
	protected String prepareAndExecuteQueries(List<Rule> rules, Set<Atom> queries) throws IOException, PrepareQueriesException {
		try {
			reasoner.addRules(rules);
		} catch (ReasonerStateException e) {
			logger.error("Trying to add rules in the wrong state for constraint " + constraint + ".", e);
			throw new PrepareQueriesException(internalError);
		}
		
		try {
			reasoner.load();
		} catch (EdbIdbSeparationException e) {
			logger.error("EDB rule occured in IDB for constraint " + constraint + ".", e);
			throw new PrepareQueriesException(internalError);
		} catch (IncompatiblePredicateArityException e) {
			logger.error("Predicate does not match the datasource for constraint " + constraint + ".", e);
			throw new PrepareQueriesException(internalError);
		}
		
		try {
			reasoner.reason();
		} catch (ReasonerStateException e) {
			logger.error("Trying to reason in the wrong state for constraint " + constraint + ".", e);
			throw new PrepareQueriesException(internalError);
		}
		String result = "";
		for (Atom query : queries) {
	    	try (QueryResultIterator iterator = reasoner.answerQuery(query, true)) {
	    		result += result(iterator);
	    	} catch (ReasonerStateException e) {
				logger.error("Trying to answer query in the wrong state for constraint " + constraint + ".", e);
				throw new PrepareQueriesException(internalError);
			}
		}

    	return result;
	}
	
	protected String result(QueryResultIterator queryResultIterator) {
		String result = ""; 
		while (queryResultIterator.hasNext()) {
			QueryResult queryResult = queryResultIterator.next();
			String triple = "";
			for (Term term : queryResult.getTerms()) {
				triple += term.getName() + "\t";
			}
			
			result += triple.substring(0, triple.length() - 1) + "\n";
		}
		return result;
	}
	
	protected abstract Set<Atom> queries();
	
	protected abstract Set<String> qualifiers();
	
	protected abstract Set<String> concatQualifiers();

	protected abstract void process(QuerySolution solution);
	
	protected abstract List<PropertyConstraintChecker> propertyCheckers() throws IOException;
	
	abstract void prepareFacts() throws ReasonerStateException, IOException;
	
	abstract void delete() throws IOException;
	
	abstract void close() throws IOException;
	
	protected <T> Set<T> asSet(T...type) {
		return new HashSet<T>(Arrays.asList(type));
	}
	
}
