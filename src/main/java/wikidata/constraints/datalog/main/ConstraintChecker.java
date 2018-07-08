/**
 * 
 */
package wikidata.constraints.datalog.main;

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

import wikidata.constraints.datalog.rdf.TripleSet;

/**
 * @author adrian
 *
 */
public abstract class ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ConstraintChecker.class);
	
	String constraint;
	
	List<PropertyConstraintChecker> propertyCheckers;
	
	Map<String, String> additionalQualifiers;
	
	public ConstraintChecker(String constraint_) throws IOException {
		constraint = constraint_;
		propertyCheckers = new ArrayList<PropertyConstraintChecker>();
		additionalQualifiers = additionalQualifiers();
		init();
	}
	
	void init() throws IOException {
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
		
		String propertiesQuery =
		"PREFIX wd: <http://www.wikidata.org/entity/>\n"+
		"PREFIX p: <http://www.wikidata.org/prop/>\n"+
		"PREFIX ps: <http://www.wikidata.org/prop/statement/>\n"+
		"PREFIX pq: <http://www.wikidata.org/prop/qualifier/>\n"+
		"SELECT ?item";
		for (String key : additionalQualifiers.keySet()) {
			propertiesQuery += " (GROUP_CONCAT(DISTINCT (?var" + key + "); separator=',') as ?" + key + ")";
		}
		propertiesQuery += "\n"+
		"WHERE\n"+
		"{\n"+
		"  ?item p:P2302 ?s .\n"+
		"  ?s ps:P2302 wd:" + constraint + ".\n";
		for (Map.Entry<String, String> entry : additionalQualifiers.entrySet()) {
			propertiesQuery += "  ?s pq:" + entry.getValue() + " ?var" + entry.getKey() + ".\n";
		}
		propertiesQuery +=
		"}"+
		"GROUP BY ?item";

		Query query = QueryFactory.create(propertiesQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);

		ResultSet results = qexec.execSelect();
		
		while (results.hasNext()) {
			QuerySolution solution = results.next();
			String property = solution.get("item").asResource().getLocalName();
			
			// To limit testing
			if (!property.equals("P31"))
				continue;
			Map<String, String> qualifiers = new HashMap<String, String>();
			for (String variableName : additionalQualifiers.keySet()) {
				RDFNode node = solution.get(variableName);
				if (node.isLiteral()) {
					Literal literal = node.asLiteral();
					qualifiers.put(variableName, literal.getString());
				} else {
					logger.error("Node " + node + " is no a literal.");
				}
				
			}
			propertyCheckers.add(getPropertyChecker(property, qualifiers));
		}       

		qexec.close() ;
	}
	
	public abstract String violations() throws ReasonerStateException, IOException;
	
	abstract Map<String, String> additionalQualifiers();
	
	abstract PropertyConstraintChecker getPropertyChecker(String property, Map<String, String> qualifiers) throws IOException;
	
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
	
}
