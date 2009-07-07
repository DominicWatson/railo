/**
 * Implements the Cold Fusion Function queryaddcolumn
 */
package railo.runtime.functions.query;

import railo.runtime.PageContext;
import railo.runtime.db.SQLTypeCaster;
import railo.runtime.exp.PageException;
import railo.runtime.ext.function.Function;
import railo.runtime.op.Caster;
import railo.runtime.type.Query;

public final class QueryAddColumn implements Function {
	public static double call(PageContext pc , Query query, String string, Object array) throws PageException {
		query.addColumn(string,Caster.toArray(array));
		return query.size();
	}
	public static double call(PageContext pc , Query query, String string, Object datatype, Object array) throws PageException {
		query.addColumn(string,Caster.toArray(array),SQLTypeCaster.toSQLType(Caster.toString(datatype)));
		return query.size();
	}
}