package utility;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class CombinedIterator<E> implements Iterator<E> {
	
	final List<Iterator<E>> iterators;
	
	int currentIterator = 0;
	
	public CombinedIterator(Iterator<E>...iterators_) {
			iterators = Arrays.asList(iterators_);
	}
	
	public CombinedIterator(List<Iterator<E>> iterators_) {
		iterators = iterators_;
	}

	@Override
	public boolean hasNext() {
		while (currentIterator < iterators.size()) {
			if (iterators.get(currentIterator).hasNext())
				return true;
			else
				currentIterator++;
		}
		return false;
	}

	@Override
	public E next() {
		if (hasNext())
			return iterators.get(currentIterator).next();
		else
			throw new NoSuchElementException();
	}

}
