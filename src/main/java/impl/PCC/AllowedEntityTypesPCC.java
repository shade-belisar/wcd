package impl.PCC;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import impl.TS.AllowedEntityTypesTS;
import impl.TS.TripleSet;

public class AllowedEntityTypesPCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(AllowedEntityTypesPCC.class);
	
	final TripleSet tripleSet;
	
	final Set<String> allowedEntityTypes;

	public AllowedEntityTypesPCC(String property_, Set<String> allowedEntityTypes_) throws IOException {
		super(property_);
		allowedEntityTypes = allowedEntityTypes_;
		tripleSet = new AllowedEntityTypesTS(property);
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
