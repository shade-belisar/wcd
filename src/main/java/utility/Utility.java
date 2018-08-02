package utility;

public class Utility {
	
	public final static String BASE_URI = "http://www.wikidata.org/entity/";
	
	public static String removeBaseURI(String string) {
		if (string.startsWith(BASE_URI))
			return string.substring(BASE_URI.length());
		return string;
	}

}
