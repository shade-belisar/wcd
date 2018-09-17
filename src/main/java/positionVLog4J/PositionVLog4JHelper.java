package positionVLog4J;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Conjunction;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Term;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import main.Main;

public class PositionVLog4JHelper {
	
	public static <T> List<T> sort(Predicate predicate, List<T> oldList) {
		if (!Main.getPredicateSorting())
			return oldList;
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
		if (Main.getConjuntionSorting()) {
			sortedAtoms.sort(new Comparator<Atom>() {

				@Override
				public int compare(Atom o1, Atom o2) {
					List<Term> terms1 = o1.getTerms();
					List<Term> terms2 = o2.getTerms();
					
					Iterator<Term> iterator1 = terms1.iterator();
					Iterator<Term> iterator2 = terms2.iterator();
					
					while(iterator1.hasNext() && iterator2.hasNext()) {
						Term t1 = iterator1.next();
						Term t2 = iterator2.next();
						
						boolean t1Constant = t1 instanceof Constant;
						boolean t2Constant = t2 instanceof Constant;
						
						if (t1Constant && !t2Constant)
							return -1;
						if (t2Constant && !t1Constant)
							return 1;
					}
					
					Integer size1 = terms1.size();
					Integer size2 = terms2.size();
					
					return size1.compareTo(size2);
				}
			});
		}
		return Expressions.makeConjunction(sortedAtoms);
	}
	
	public static Rule sort(Rule rule) {
		return Expressions.makeRule(sort(rule.getHead()), sort(rule.getBody()));
	}
}
