package impl.TS;

import java.io.IOException;

public class AllowedEntityTypesTS extends TripleSet {

	public AllowedEntityTypesTS(String property_) throws IOException {
		super(property_);
	}

	@Override
	protected String getTripleSetType() {
		return "AllowedEntityTypes";
	}

}
