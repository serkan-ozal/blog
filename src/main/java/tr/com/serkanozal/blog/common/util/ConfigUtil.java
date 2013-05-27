package tr.com.serkanozal.blog.common.util;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Serkan OZAL
 */
public class ConfigUtil {

	private static final Logger logger = LogUtil.getLogger();
	
	public static final String DEFAULT_CONFIG_DIRECTORY_PATH = "config";
	public static String CONFIG_DIRECTORY_PATH = DEFAULT_CONFIG_DIRECTORY_PATH;
	private static final String PROPERTIES_FILE_EXTENSION = ".properties";
	
	private static Map<String, Map<String, String>> cachedConfigs = new HashMap<String, Map<String, String>>();
	
	private ConfigUtil() {
		
	}
	
	private static String preProcessConfigFileName(String configFileName) {
		if (configFileName.endsWith(PROPERTIES_FILE_EXTENSION) == false) {
			configFileName += PROPERTIES_FILE_EXTENSION;
		}
		return configFileName;
	}

	@SuppressWarnings("rawtypes")
	private static Map<String, String> loadConfigs(String configFileName) {
		try {
			configFileName = preProcessConfigFileName(configFileName);
			Properties prop = new Properties();
			prop.load(IOUtil.getResourceAsStream(
							CONFIG_DIRECTORY_PATH +  File.separator + configFileName));
			Map<String, String> configs = new HashMap<String, String>();
			Enumeration keys = prop.keys();
			while (keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
				String value = prop.getProperty(key);
				configs.put(key, value);
			}
			return configs;
		} 
		catch (Throwable t) {
			logger.error("Unable to load configs from " + configFileName, t);
			return null;
		}
	}
	
	public static Map<String, String> getConfigurations(String configFileName) {
		configFileName = preProcessConfigFileName(configFileName);
		Map<String, String> configs = cachedConfigs.get(configFileName);
		if (configs == null) {
			configs = loadConfigs(configFileName);
			cachedConfigs.put(configFileName, configs);
		}
		return configs;
	}
	
	public static String getConfiguration(String configFileName, String key) {
		return getConfigurations(configFileName).get(key);
	}
	
}
