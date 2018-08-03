package utility;

import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

public class Utility {
	
	public final static String BASE_URI = "http://www.wikidata.org/entity/";
	
	public static Constant makeConstant(String string) {
		if (string.startsWith(BASE_URI))
			return Expressions.makeConstant(string);
		else
			return Expressions.makeConstant(BASE_URI + string);
	}
}
