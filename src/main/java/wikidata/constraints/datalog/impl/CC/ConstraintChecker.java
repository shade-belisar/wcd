/**
 * 
 */
package wikidata.constraints.datalog.impl.CC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import wikidata.constraints.datalog.impl.PCC.PropertyConstraintChecker;

/**
 * @author adrian
 *
 */
public abstract class ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ConstraintChecker.class);
	
	String constraint;
	
	protected List<PropertyConstraintChecker> propertyCheckers;
	
	public ConstraintChecker(String constraint_) {
		constraint = constraint_;
	}

	protected abstract List<PropertyConstraintChecker> propertyCheckers() throws IOException;

	public void init() throws IOException {
		// Fetching the properties with this constraint
		/*
		 * 	PREFIX wd: <http://www.wikidata.org/entity/>
			PREFIX p: <http://www.wikidata.org/prop/>
			PREFIX ps: <http://www.wikidata.org/prop/statement/>
			PREFIX pq: <http://www.wikidata.org/prop/qualifier/>
			SELECT ?item (GROUP_CONCAT(DISTINCT (?scope); separator=",") as ?scope)
			WHERE
			{
			  ?item p:P2302 ?s .
			  ?s ps:P2302 wd:Q53869507.
			  ?s pq:P5314 ?scope.
			}
			GROUP BY ?item
		 */
		
		Set<String> qualifiers = qualifiers();
		Set<String> concatQualifiers = concatQualifiers();
		
		String propertiesQuery =
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

		Query query = QueryFactory.create(propertiesQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);

		ResultSet results = qexec.execSelect();
		
		while (results.hasNext()) {
			QuerySolution solution = results.next();
			String property = solution.get("item").asResource().getLocalName();
			if (!property.equals("P31"))
				continue;

			process(solution);
		}       

		qexec.close();
		propertyCheckers = propertyCheckers();
	}
	
	public String violations() throws ReasonerStateException, IOException {
		String result = "";
		for (PropertyConstraintChecker propertyConstraintChecker : propertyCheckers) {
			result += propertyConstraintChecker.violations() + "\n";
		}
		return result;
	}
	
	public void close() throws IOException {
		for (PropertyConstraintChecker propertyConstraintChecker : propertyCheckers) {
			propertyConstraintChecker.close();
		}
	}
	
	@Override
	public String toString() {
		String result = "Constraint id: " + constraint + "\n";
		for (PropertyConstraintChecker property : propertyCheckers) {
			if (!property.equals(""))
				result += "  " + property + "\n";
		}
			
		return result;
	}
	
	protected abstract Set<String> qualifiers();
	
	protected abstract Set<String> concatQualifiers();

	protected abstract void process(QuerySolution solution);
	
}
