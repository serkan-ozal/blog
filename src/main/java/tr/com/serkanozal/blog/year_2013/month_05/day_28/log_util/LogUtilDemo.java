package tr.com.serkanozal.blog.year_2013.month_05.day_28.log_util;

import org.apache.log4j.Logger;

/**
 * @author Serkan OZAL
 */
public class LogUtilDemo {

	public static void main(String[] args) {
		Logger logger = LogUtil.getLogger();
		logger.info("Hello LogUtil");
	}
	
}
