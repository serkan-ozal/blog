package tr.com.serkanozal.blog.common.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author Serkan OZAL
 */
public class IOUtil {

	private static final Logger logger = LogUtil.getLogger();
	
	private IOUtil() {
		
	}
	
	public static InputStream getResourceAsStream(String resourcePath) {
		try {
			if (resourcePath.startsWith("/") == false) {
				resourcePath = "/" + resourcePath;
			}
			InputStream is = IOUtil.class.getResourceAsStream(resourcePath);
			if  (is == null) {
				is = getCallerClass().getResourceAsStream(resourcePath);
			}
			return is;
		}
		catch (Throwable t) {
			logger.error("Unable to get resource " + "(" + resourcePath + ")" + " as stream", t);
			return null;
		}
	}
	
	public static File getResourceAsFile(String resourcePath) {
		try {
			if (resourcePath.startsWith("/") == false) {
				resourcePath = "/" + resourcePath;
			}
			URL url = IOUtil.class.getResource(resourcePath);
			if (url == null) {
				url = getCallerClass().getResource(resourcePath);
			}
			return new File(url.toURI());
		} 
		catch (Throwable t) {
			logger.error("Unable to get resource " + "(" + resourcePath + ")" + " as file", t);
			return null;
		}
	}
	
	private static Class<?> getCallerClass() {
		try {
			return Class.forName(Thread.currentThread().getStackTrace()[3].getClassName());
		} 
		catch (ClassNotFoundException e) {
			logger.error("Unable to get caller class", e);
			return null;
		}
	}
	
}
