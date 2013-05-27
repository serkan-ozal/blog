package tr.com.serkanozal.blog.year_2013.month_05.day_23;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import tr.com.serkanozal.blog.common.util.LogUtil;

/**
 * @author Serkan OZAL
 */
public class AmazonS3MultipartFileUploadDemo {

	private static final Logger logger = LogUtil.getLogger();

	public static void main(String[] args) throws IOException {
		/**
		 * NOTE: 
		 * 		You must set your unique "Access Key" and "Secret Key" values to 
		 * 		"src/main/resources/config/aws/credentials.properties" file.
		 */
		
		File sampleInputFile = new File("SampleFileToUpload.txt");
		if (sampleInputFile.exists()) {
			logger.info("Existing sample input file detected: " + sampleInputFile.getAbsolutePath());
			sampleInputFile.delete();
			logger.info("Deleted existing input file");
		}
		
		logger.info("Creating new sample input file: " + sampleInputFile.getAbsolutePath() + " ...");
		sampleInputFile.createNewFile();
		
		//////////////////////////////////////////////////////////////////////////////////////////////////
		
		logger.info("Generating new sample input file: " + sampleInputFile.getAbsolutePath() + " ...");
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(sampleInputFile));
		for (int i = 0; i < 100000; i++) {
			bos.write(i);
		}
		bos.flush();
		bos.close();
		logger.info("Sample input file generated");
		
		//////////////////////////////////////////////////////////////////////////////////////////////////
		
		AmazonS3Util.FILE_PART_SIZE = 10 * 1024; // For testing purpose, out test data is about 100KB and our block size is 10KB.
		
		final String BUCKET_NAME = "AmazonS3MultipartFileUploadDemoBucket";
		
		if (AmazonS3Util.isBucketExist(BUCKET_NAME) == false) {
			logger.info("Creating Amazon S3 bucket: " + BUCKET_NAME);
			AmazonS3Util.createBucket(BUCKET_NAME);
		}
		logger.info("Using Amazon S3 bucket: " + BUCKET_NAME + " ...");
		
		
		logger.info("Uploading sample file to Amazon S3 ...");
		AmazonS3Util.putObjectAsMultiPart(BUCKET_NAME, sampleInputFile);
		logger.info("Uploading sample file finished");
	}
	
}
