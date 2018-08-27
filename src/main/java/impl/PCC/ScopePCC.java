/**
 * 
 */
package impl.PCC;

import static utility.SC.violation_qualifier_query;
import static utility.SC.violation_reference_query;
import static utility.SC.violation_statement_query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;

import impl.CC.ScopeCC;

/**
 * @author adrian
 *
 */
public class ScopePCC extends PropertyConstraintChecker {
	
	final static Logger logger = Logger.getLogger(ScopePCC.class);
	
	final Set<String> allowedScopes;
	
	public ScopePCC(String property_, Set<String> qualifiers_) throws IOException {
		super(property_);
		allowedScopes = qualifiers_;
	}
	
	public List<Rule> rules() {		
		List<Rule> rules = new ArrayList<Rule>();

		// violation_statement(S, I, propertyConstant, V) :- statementEDB(S, I, propertyConstant, V)
		Rule notStatement = Expressions.makeRule(violation_statement_SIpV, statementEDB_SIpV);
		// violation_qualfier(S, propertyConstant, V) :- qualifierEDB(S, propertyConstant, V)
		Rule notQualifier = Expressions.makeRule(violation_qualifier_SpV, qualifierEDB_SpV);
		// violation_reference(S, H, propertyConstant, V) :- referenceEDB(S, H, propertyConstant, V)
		Rule notReference = Expressions.makeRule(violation_reference_SHpV, referenceEDB_SHpV); 
		
		rules.add(notStatement);
		rules.add(notQualifier);
		rules.add(notReference);
		
		List<Atom> queries = new ArrayList<Atom>();
		queries.add(violation_statement_query);
		queries.add(violation_qualifier_query);
		queries.add(violation_reference_query);
		
		if (allowedAs(ScopeCC.AS_MAIN_VALUE)) {
			rules.remove(notStatement);
			queries.remove(violation_statement_query);
		}
		
		if (allowedAs(ScopeCC.AS_QUALIFIER)) {
			rules.remove(notQualifier);
			queries.remove(violation_qualifier_query);
		}
		
		if (allowedAs(ScopeCC.AS_REFERENCE)) {
			rules.remove(notReference);
			queries.remove(violation_reference_query);
		}
		
		return rules;
	}
	
	protected boolean allowedAs(String qualifier) {
		boolean result = false;
		for (String allowed : allowedScopes) {
			if (allowed.equals(qualifier))
				result = true;
		}
		return result;
	}
}
