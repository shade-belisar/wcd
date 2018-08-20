/**
 * 
 */
package impl.CC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.semanticweb.vlog4j.core.model.api.Conjunction;
import org.semanticweb.vlog4j.core.model.api.QueryResult;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.api.Variable;
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
import main.Main;
import utility.InequalityHelper;
import utility.PrepareQueriesException;
import utility.SC;
import utility.Utility;

import static utility.SC.require_inequality;

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
	
	int resultSize = 0;
	
	String resultString = "";
	
	public ConstraintChecker(String constraint_) throws IOException {
		constraint = constraint_;
		internalError = "INTERNAL_ERROR for constraint " + constraint + ".";
		reasoner.setAlgorithm(Algorithm.RESTRICTED_CHASE);
		initDataField();
		init();
	}
	
	abstract void initDataField();

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
			//if (!(property.equals("P209"))) 
			//	continue;

			process(solution);
		}       

		qexec.close();
		propertyCheckers = propertyCheckers();
	}
	
	public void violations() throws ReasonerStateException, IOException {
		loadTripleSet();
		prepareFacts();
		List<Rule> rulesToAdd = new ArrayList<Rule>();
		for (PropertyConstraintChecker propertyConstraintChecker : propertyCheckers) {
			rulesToAdd.addAll(propertyConstraintChecker.rules());

		}
		//System.out.println(rulesToAdd.size());
		//for (Rule rule : rulesToAdd) {
		//	System.out.println(rule);
		//}
		if (InequalityHelper.mode.equals(InequalityHelper.Mode.DEMANDED)) {
			rulesToAdd.addAll(addRequireInequality(rulesToAdd));
		}
		 
		try {
			prepareAndExecuteQueries(rulesToAdd, queries());
		} catch (PrepareQueriesException e) {
			resultString += e.getMessage();
		}
	}
	
	List<Rule> addRequireInequality(List<Rule> rules) {
		List<Rule> result = new ArrayList<Rule>();
		for (Rule rule : rules) {
			List<Atom> unequalAtoms = new ArrayList<Atom>();
			List<Atom> otherAtoms = new ArrayList<Atom>();
			Set<Variable> otherVariables = new HashSet<Variable>();
			Conjunction body = rule.getBody();
			for (Atom bodyAtom : body.getAtoms()) {
				if (bodyAtom.getPredicate().equals(InequalityHelper.unequal)) {
					unequalAtoms.add(bodyAtom);
				} else {
					otherAtoms.add(bodyAtom);
					otherVariables.addAll(bodyAtom.getVariables());
				}
			}
			for (Atom unequalAtom : unequalAtoms) {
				if (otherVariables.containsAll(unequalAtom.getVariables())) {
					if (unequalAtom.getTerms().size() != 2) {
						logger.error("Atom with unequal predicate had an arity different from 2: " + unequalAtom.toString());
						continue;
					}
					
					Term t1 = unequalAtom.getTerms().get(0);
					Term t2 = unequalAtom.getTerms().get(1);
						
					// require_inequality(t1, t2)
					Atom require_inequality_tt = Expressions.makeAtom(require_inequality, t1, t2);
					
					// require_inequality(t1, t2) :- otherAtoms
					Rule require = Expressions.makeRule(require_inequality_tt, Utility.toArray(otherAtoms));
					result.add(require);
				}
			}
		}
		return result;
	}
	
	void loadTripleSet() throws ReasonerStateException, IOException {		
		final DataSource tripleEDBPath = new CsvFileDataSource(Main.tripleSet.getTripleFile());
		reasoner.addFactsFromDataSource(SC.tripleEDB, tripleEDBPath);

		final DataSource qualifierEDBPath = new CsvFileDataSource(Main.tripleSet.getQualifierFile());
		reasoner.addFactsFromDataSource(SC.qualifierEDB, qualifierEDBPath);

		final DataSource referenceEDBPath = new CsvFileDataSource(Main.tripleSet.getReferenceFile());
		reasoner.addFactsFromDataSource(SC.referenceEDB, referenceEDBPath);					
	}
	
	protected void prepareAndExecuteQueries(List<Rule> rules, Set<Atom> queries) throws IOException, PrepareQueriesException {
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
		for (Atom query : queries) {
	    	try (QueryResultIterator iterator = reasoner.answerQuery(query, true)) {
	    		while (iterator.hasNext()) {
	    			QueryResult queryResult = iterator.next();
	    			resultSize++;
	    			if (Main.getStringResult()) {
	    				String triple = "";
		    			for (Term term : queryResult.getTerms()) {
		    				triple += term.getName() + "\t";
		    			}
		    			
		    			resultString += "\t" + triple.substring(0, triple.length() - 1) + "\n";
	    			}
	    		}
	    	} catch (ReasonerStateException e) {
				logger.error("Trying to answer query in the wrong state for constraint " + constraint + ".", e);
				throw new PrepareQueriesException(internalError);
			}
		}
	}
	
	public int getResultSize() {
		return resultSize;
	}
	
	public String getResultString() {
		return resultString;
	}
	
	public String identify() {
		return "Constraint: " + constraint;
	}
	
	protected abstract Set<Atom> queries();
	
	protected abstract Set<String> qualifiers();
	
	protected abstract Set<String> concatQualifiers();

	protected abstract void process(QuerySolution solution);
	
	abstract void prepareFacts() throws ReasonerStateException, IOException;
	
	protected abstract List<PropertyConstraintChecker> propertyCheckers() throws IOException;
	
	protected <T> Set<T> asSet(T...type) {
		return new HashSet<T>(Arrays.asList(type));
	}
	
}
