package wikidata.constraints.datalog.impl.PCC;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.lib.tuple.Tuple2;
import org.apache.log4j.Logger;

import wikidata.constraints.datalog.impl.TS.ItemInstanceTS;
import wikidata.constraints.datalog.impl.TS.TripleSet;

public class TypePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(TypePCC.class);
	
	final String relation;
	
	final Set<String> classes;
	
	final List<Tuple2<String>> subclassRelations;
	
	final TripleSet tripleSet;

	public TypePCC(String property_, String relation_, Set<String> classes_, List<Tuple2<String>> subclassRelations_) throws IOException {
		super(property_);
		relation = relation_;
		classes = classes_;
		subclassRelations = subclassRelations_;
		tripleSet = new ItemInstanceTS(property);
		
	}

	@Override
	public String violations() throws IOException {
		// TODO Auto-generated method stub
		return "not yet implemented";
	}

	@Override
	protected Set<TripleSet> getRequiredTripleSets() throws IOException {
		return new HashSet<TripleSet>(Arrays.asList(tripleSet));
	}

}