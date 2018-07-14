package wikidata.constraints.datalog.impl.PCC;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

import wikidata.constraints.datalog.main.PropertyConstraintChecker;
import wikidata.constraints.datalog.rdf.ConflictsWithTripleSet;
import wikidata.constraints.datalog.rdf.TripleSet;

public class ConflictsWithPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ConflictsWithPCC.class);
	
	final String TRIPLE_SET = "triple_set";
	
	final Map<String, HashSet<String>> conflicts;

	public ConflictsWithPCC(String property_, Map<String, HashSet<String>> qualifiers_) throws IOException {
		super(property_);
		conflicts = qualifiers_;
	}

	@Override
	public String violations() throws ReasonerStateException, IOException {
		TripleSet tripleSet = tripleSets.get(TRIPLE_SET);
		
		if (!tripleSet.notEmpty())
			return "";
		
		try {
			loadTripleSets(tripleSet);
		} catch (ReasonerStateException e) {
			logger.error("Trying to load facts to the reasoner in the wrong state for property " + property + ".", e);
			return "INTERNAL ERROR for property " + property + ".";
		}
		reasoner.close();
		return "not yet implemented";
	}

	@Override
	protected Map<String, TripleSet> getRequiredTripleSets(String property) throws IOException {
		Map<String, TripleSet> result = new HashMap<String, TripleSet>();
		result.put(TRIPLE_SET, new ConflictsWithTripleSet(property));
		return result;
	}

}
