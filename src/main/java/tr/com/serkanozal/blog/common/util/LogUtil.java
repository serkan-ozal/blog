package tr.com.serkanozal.blog.common.util;

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
