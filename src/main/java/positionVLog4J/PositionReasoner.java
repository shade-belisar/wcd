package positionVLog4J;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.VLogReasoner;

public class PositionReasoner extends VLogReasoner {

	@Override
	public void addRules(Rule... rules) throws ReasonerStateException {
		addRules(Arrays.asList(rules));
	}
	
	@Override
	public void addRules(List<Rule> rules) throws ReasonerStateException {
		List<Rule> toAdd = new ArrayList<>();
		for (Rule rule : rules) {
			toAdd.add(PositionVLog4JHelper.sort(rule));
		}		
		super.addRules(toAdd);
	}
}
