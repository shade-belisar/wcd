package positionVLog4J;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Conjunction;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

public class PositionVLog4JHelper {
	
	public static <T> List<T> sort(Predicate predicate, List<T> oldList) {
		if (predicate.getArity() != oldList.size())
			throw new ArityPositionException("List with size " + oldList.size() + " cannot be sorted based on predicate " + predicate.getName() + " with arity " + predicate.getArity() + ".");
		if (!(predicate instanceof PositionPredicate))
			return oldList;
		PositionPredicate positionPredicate = (PositionPredicate) predicate;
		
		List<T> newList = new ArrayList<T>();
		for(int i = 0; i < oldList.size(); i++) {
			newList.add(null);
		}
		
		int i = 0;
		for (Integer position : positionPredicate.getWeights()) {
			newList.set(position, oldList.get(i));
			
			i++;
		}
		
		return newList;		
	}

	public static Atom sort(Atom atom) {
		Predicate predicate = atom.getPredicate();
		return Expressions.makeAtom(predicate, sort(predicate, atom.getTerms()));
	}
	
	public static Conjunction sort(Conjunction conjunction) {
		List<Atom> sortedAtoms = new ArrayList<>();
		for (Atom atom : conjunction.getAtoms()) {
			sortedAtoms.add(sort(atom));
		}
		return Expressions.makeConjunction(sortedAtoms);
	}
	
	public static Rule sort(Rule rule) {
		return Expressions.makeRule(sort(rule.getHead()), sort(rule.getBody()));
	}
}
