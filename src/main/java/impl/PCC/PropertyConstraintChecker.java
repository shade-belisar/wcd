/**
 * 
 */
package impl.PCC;

import static utility.SC.i;
import static utility.SC.qualifierEDB;
import static utility.SC.referenceEDB;
import static utility.SC.s;
import static utility.SC.tripleEDB;
import static utility.SC.v;
import static utility.SC.violation_qualifier;
import static utility.SC.violation_reference;
import static utility.SC.violation_triple;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import utility.Utility;

/**
 * @author adrian
 *
 */
public abstract class PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(PropertyConstraintChecker.class);

	// violation_triple(S, I, propertyConstant, V)
	protected final Atom violation_triple_SIpV;
	// violation_qualifier(S, propertyConstant, V)
	protected final Atom violation_qualifier_SpV;
	// violation_reference(S, propertyConstant, V)
	protected final Atom violation_reference_SpV;
	
	// tripleEDB(S, I, propertyConstant, V)
	protected final Atom tripleEDB_SIpV;
	// qualifierEDB(S, propertyConstant, V)
	protected final Atom qualifierEDB_SpV;
	// referenceEDB(S, propertyConstant, V)
	protected final Atom referenceEDB_SpV;
	
	protected final Constant propertyConstant;
	
	protected final String property;
	
	
	
	public PropertyConstraintChecker(String property_) throws IOException {
		property = property_;
		propertyConstant = Utility.makeConstant(property);
		
		violation_triple_SIpV = Expressions.makeAtom(violation_triple, s, i, propertyConstant, v);
		violation_qualifier_SpV = Expressions.makeAtom(violation_qualifier, s, propertyConstant, v);
		violation_reference_SpV = Expressions.makeAtom(violation_reference, s, propertyConstant, v);
		
		tripleEDB_SIpV = Expressions.makeAtom(tripleEDB, s, i, propertyConstant, v);
		qualifierEDB_SpV = Expressions.makeAtom(qualifierEDB, s, propertyConstant, v);
		referenceEDB_SpV = Expressions.makeAtom(referenceEDB, s, propertyConstant, v);
	}
	
	public abstract List<Rule> rules();
}
