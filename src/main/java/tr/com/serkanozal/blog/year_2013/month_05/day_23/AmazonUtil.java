package tr.com.serkanozal.blog.year_2013.month_05.day_23;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import tr.com.serkanozal.blog.common.util.ConfigUtil;
import tr.com.serkanozal.blog.common.util.IOUtil;
import tr.com.serkanozal.blog.common.util.LogUtil;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

/**
 * @author Serkan OZAL
 */
public class AmazonUtil {

	private static final Logger logger = LogUtil.getLogger();
	
	private static final String AWS_CREDENTIALS_CONFIG_FILE_PATH = 
			ConfigUtil.CONFIG_DIRECTORY_PATH + File.separator + "aws" + File.separator + "credentials.properties";
	
	private static AWSCredentials awsCredentials;
	
	static {
		init();
	}
	
	private AmazonUtil() {
		
	}
	
	private static void init() {
		try {
			awsCredentials = new PropertiesCredentials(IOUtil.getResourceAsStream(AWS_CREDENTIALS_CONFIG_FILE_PATH));
		} 
		catch (IOException e) {
			logger.error("Unable to initialize AWS Credentials from " + AWS_CREDENTIALS_CONFIG_FILE_PATH);
		}  
	}
	
	public static AWSCredentials getAwsCredentials() {
		return awsCredentials;
	}
	
}
