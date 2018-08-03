package utility;

import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor;

public class OutputValueVisitor implements ValueVisitor<String> {

	@Override
	public String visit(DatatypeIdValue value) {
		String result = value.getIri();
		return result;
	}

	@Override
	public String visit(EntityIdValue value) {
		String result = value.getIri();
		return result;
	}

	@Override
	public String visit(GlobeCoordinatesValue value) {
		String result = value.getLatitude() + ";" + value.getLongitude();
		return result;
	}

	@Override
	public String visit(MonolingualTextValue value) {
		String result = value.getText() + "@" + value.getLanguageCode();
		return result;
	}

	@Override
	public String visit(QuantityValue value) {
		String numericValue = "NaN";
		if (value.getNumericValue() != null)
			numericValue = value.getNumericValue().toString();
		String lowerBound = "NaN";
		if (value.getLowerBound() != null)
			lowerBound = value.getLowerBound().toString();
		String upperBound = "NaN";
		if (value.getUpperBound() != null)
			upperBound = value.getUpperBound().toString();
		String unit = "None";
		if (value.getUnit() != null)
			unit = value.getUnit().toString();
		String result = numericValue + " Lower: " + lowerBound + " Upper: " + upperBound + " Unit: " + unit;
		return result;
	}

	@Override
	public String visit(StringValue value) {
		String result = value.getString();
		return result;
	}

	@Override
	public String visit(TimeValue value) {
		// TODO
		String result = value.toString();
		return result;
	}

}
