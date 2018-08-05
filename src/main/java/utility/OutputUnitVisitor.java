package utility;

import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor;

public class OutputUnitVisitor implements ValueVisitor<String> {

	@Override
	public String visit(DatatypeIdValue value) {
		return null;
	}

	@Override
	public String visit(EntityIdValue value) {
		return null;
	}

	@Override
	public String visit(GlobeCoordinatesValue value) {
		return null;
	}

	@Override
	public String visit(MonolingualTextValue value) {
		return null;
	}

	@Override
	public String visit(QuantityValue value) {
		return value.getUnit();
	}

	@Override
	public String visit(StringValue value) {
		return null;
	}

	@Override
	public String visit(TimeValue value) {
		return null;
	}

}
