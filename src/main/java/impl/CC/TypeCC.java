package impl.CC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.tuple.Tuple2;
import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;

import impl.PCC.PropertyConstraintChecker;

public class TypeCC extends ConstraintChecker {
	
	final static Logger logger = Logger.getLogger(TypeCC.class);
	
	public static final String RELATION = "P2309";
	
	public static final String CLASS = "P2308";
	
	Map<String, HashSet<String>> relationAndClasses = new HashMap<String, HashSet<String>>();

	public TypeCC() {
		super("Q21503250");
	}

	@Override
	protected Set<String> qualifiers() {
		return new HashSet<String>(Arrays.asList(RELATION));
	}

	@Override
	protected Set<String> concatQualifiers() {
		return new HashSet<String>(Arrays.asList(CLASS));
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = solution.get("item").asResource().getLocalName();
		
		if (!relationAndClasses.containsKey(property))
			relationAndClasses.put(property, new HashSet<String>());

		RDFNode node = solution.get(CLASS);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				relationAndClasses.get(property).add(value);				
			}
		} else {
			logger.error("Node " + node + " is no a literal.");
		}

	}
	
	List<Tuple2<String>> subclassRelations() {
		String subclassRelationsQuery =
		"PREFIX wd: <http://www.wikidata.org/entity/>\n"+
		"SELECT ?subclass ?class"+
		"WHERE {"+
		"  ?subclass wd:P279 ?class"+
		"}";
		
		Query query = QueryFactory.create(subclassRelationsQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", query);

		ResultSet results = qexec.execSelect();
		
		List<Tuple2<String>> result = new ArrayList<Tuple2<String>>();
		
		while (results.hasNext()) {
			QuerySolution solution = results.next();
			
			String subclass = solution.get("subclass").asResource().getLocalName();
			String superclass = solution.get("superclass").asResource().getLocalName();
			
			result.add(TupleFactory.create2(subclass, superclass));
		}
		
		return result;
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
