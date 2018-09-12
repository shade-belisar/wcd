/**
 * 
 */
package impl.PCC;

import static utility.SC.h;
import static utility.SC.i;
import static utility.SC.qualifierEDB;
import static utility.SC.referenceEDB;
import static utility.SC.s;
import static utility.SC.statementEDB;
import static utility.SC.v;
import static utility.SC.violation_qualifier;
import static utility.SC.violation_reference;
import static utility.SC.violation_statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

	// violation_statement(S, I, propertyConstant, V)
	protected final Atom violation_statement_SIpV;
	// violation_qualifier(S, propertyConstant, V)
	protected final Atom violation_qualifier_SpV;
	// violation_reference(S, H, propertyConstant, V)
	protected final Atom violation_reference_SHpV;
	
	// statementEDB(S, I, propertyConstant, V)
	protected final Atom statementEDB_SIpV;
	// qualifierEDB(S, propertyConstant, V)
	protected final Atom qualifierEDB_SpV;
	// referenceEDB(S, H, propertyConstant, V)
	protected final Atom referenceEDB_SHpV;
	
	protected final Constant propertyConstant;
	
	protected final String property;
	
	
	
	public PropertyConstraintChecker(String property_) throws IOException {
		property = property_;
		propertyConstant = Utility.makeConstant(property);
		
		violation_statement_SIpV = Expressions.makeAtom(violation_statement, s, i, propertyConstant, v);
		violation_qualifier_SpV = Expressions.makeAtom(violation_qualifier, s, propertyConstant, v);
		violation_reference_SHpV = Expressions.makeAtom(violation_reference, s, h, propertyConstant, v);
		
		statementEDB_SIpV = Expressions.makeAtom(statementEDB, s, i, propertyConstant, v);
		qualifierEDB_SpV = Expressions.makeAtom(qualifierEDB, s, propertyConstant, v);
		referenceEDB_SHpV = Expressions.makeAtom(referenceEDB, s, h, propertyConstant, v);
	}
	
	public abstract List<Rule> rules();
}
