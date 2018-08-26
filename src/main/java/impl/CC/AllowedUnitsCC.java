package impl.CC;

import static utility.SC.violation_qualifier_query;
import static utility.SC.violation_reference_query;
import static utility.SC.violation_triple_query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import impl.PCC.AllowedUnitsPCC;
import impl.PCC.PropertyConstraintChecker;
import main.Main;
import utility.InequalityHelper;
import utility.Utility;

public class AllowedUnitsCC extends ConstraintChecker {
	
	public static final String ALLOWED_UNIT =  "P2305";
	
	Map<String, HashSet<String>> allowedUnits;

	public AllowedUnitsCC() throws IOException {
		super("Q21514353");
	}

	@Override
	void initDataField() {
		allowedUnits = new HashMap<String, HashSet<String>>();
	}
	
	@Override
	protected Set<String> qualifiers() {
		return asSet();
	}

	@Override
	protected Set<String> concatQualifiers() {
		return asSet(ALLOWED_UNIT);
	}

	@Override
	protected void process(QuerySolution solution) {
		String property = Utility.addBaseURI(solution.get("item").asResource().getLocalName());
		
		if (!allowedUnits.containsKey(property))
			allowedUnits.put(property, new HashSet<String>());

		RDFNode node = solution.get(ALLOWED_UNIT);
		if (node.isLiteral()) {
			Literal literal = node.asLiteral();
			String content = literal.getString();
			if (content.equals(""))
				return;
			for (String value : literal.getString().split(",")) {
				allowedUnits.get(property).add(value);				
			}
		} else {
			logger.error("Node " + node + " is no a literal.");
		}
	}
	
	@Override
	protected Set<Atom> queries() {
		return asSet(violation_triple_query, violation_qualifier_query, violation_reference_query);
	}

	@Override
	void prepareFacts() throws ReasonerStateException, IOException {
		Main.tripleSet.loadUnitsFile(reasoner);

		InequalityHelper.setOrReset(reasoner);
		Set<String> units = new HashSet<String>();
		for (Set<String> unitsSet : allowedUnits.values()) {
			units.addAll(unitsSet);
		}
		InequalityHelper.establishInequality(Main.tripleSet.getUnitsFile(), 1, units);
	}

	@Override
	protected List<PropertyConstraintChecker> propertyCheckers() throws IOException {
		List<PropertyConstraintChecker> result = new ArrayList<PropertyConstraintChecker>();
		for (Map.Entry<String, HashSet<String>> entry : allowedUnits.entrySet()) {
			result.add(new AllowedUnitsPCC(entry.getKey(), entry.getValue()));
		}
		return result;
	}
}