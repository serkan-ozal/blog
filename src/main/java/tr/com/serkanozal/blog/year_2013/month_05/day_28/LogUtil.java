package tr.com.serkanozal.blog.year_2013.month_05.day_28;

import org.apache.log4j.Logger;

/**
 * @author Serkan OZAL
 */
public class LogUtil {

	private LogUtil() {
		
	}
	
	public static Logger getLogger() {
		return Logger.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
	}
	
}
