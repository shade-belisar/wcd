package utility;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class InequalityFileIndex {
	
	final File file;
	
	final int index;
	
	public InequalityFileIndex(File file_, int index_) {
		file = file_;
		index = index_;
	}
	
	public Iterator<String> getIterator() throws IOException {
		return new IndexedCSVIterator(file, index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InequalityFileIndex other = (InequalityFileIndex) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (index != other.index)
			return false;
		return true;
	}
}
