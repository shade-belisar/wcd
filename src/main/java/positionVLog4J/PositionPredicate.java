package positionVLog4J;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.semanticweb.vlog4j.core.model.implementation.PredicateImpl;

public class PositionPredicate extends PredicateImpl {
	
	final List<Integer> positions;
	
	public PositionPredicate(@NonNull String name, int arity) {
		this(name, arity, 0);
	}

	public PositionPredicate(@NonNull String name, int arity, int...positions_) {
		super(name, arity);
		int positionLength = positions_.length;
		if (positionLength != arity)
			throw new ArityPositionException("Arity " + arity + " does not match number of weights (" + positionLength + ") given for predicate " + name + ".");
		List<Integer> temp = new ArrayList<>(positions_.length);
		for (int integer : positions_) {
			temp.add(integer);
		}
		positions = Collections.unmodifiableList(temp);
		if (new HashSet<>(positions).size() != positions.size())
			throw new ArityPositionException("Positions overlap for predicate " + name + " with arity " + arity + ".");
		for (int i = 0; i < arity; i++) {
			if (!positions.contains(i))
				throw new ArityPositionException("Position " + i + " not in positions given but expected because of arity " + arity + " for predicate " + name + ".");
		}
			
	}
	
	public List<Integer> getWeights() {
		return positions;
	}
	
	public int transformPosition(int i) {
		return positions.get(i);
	}
}
