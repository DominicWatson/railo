/**
 * Implements the Cold Fusion Function week
 */
package railo.runtime.functions.dateTime;

import java.util.Calendar;
import java.util.TimeZone;

import railo.commons.date.TimeZoneUtil;
import railo.runtime.PageContext;
import railo.runtime.exp.ExpressionException;
import railo.runtime.ext.function.Function;
import railo.runtime.type.dt.DateTime;

public final class Week implements Function {
	private static Calendar calendar;

	public static double call(PageContext pc , DateTime date) {
		return _call(pc, date, pc.getTimeZone());
	}
	
	public static double call(PageContext pc , DateTime date, String strTimezone) throws ExpressionException {
		return _call(pc, date, TimeZoneUtil.toTimeZone(strTimezone));
	}
	
	private static double _call(PageContext pc , DateTime date,TimeZone tz) {
		if (calendar == null)
        	calendar=Calendar.getInstance();
        synchronized (calendar) {
        	calendar.clear();
        	DateUtil.setTimeZone(calendar,date,tz);      
    		calendar.setTime(date);         
    		return calendar.get(Calendar.WEEK_OF_YEAR);
        }
	}
}