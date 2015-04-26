package tr.com.serkanozal.blog.year_2013.month_05.day_23.aws_s3_multipart_file_upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tr.com.serkanozal.blog.common.util.LogUtil;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * @author Serkan OZAL
 */
public class AmazonS3Util {

	private static final Logger logger = LogUtil.getLogger();
	
	public static final long DEFAULT_FILE_PART_SIZE = 5 * 1024 * 1024; // 5MB
	public static long FILE_PART_SIZE = DEFAULT_FILE_PART_SIZE;
	
	private static AmazonS3 s3Client;
	private static TransferManager transferManager;
	
	static {
		init();
	}
	
	private AmazonS3Util() {
		
	}

	private static void init() {
		s3Client = new AmazonS3Client(AmazonUtil.getAwsCredentials());
		transferManager = new TransferManager(AmazonUtil.getAwsCredentials());        
	}
	
	public static AmazonS3 getS3Client() {
		return s3Client;
	}
	
	private static String preProcessBucketName(String bucketName) {
		return bucketName.toLowerCase().replace("_", ".").replaceAll("[\\s]+", "");
	}
	
	public static boolean isBucketExist(String bucketName) {
		bucketName = preProcessBucketName(bucketName);
		return s3Client.doesBucketExist(bucketName);
	}
	
	public static void createBucket(String bucketName) {
		bucketName = preProcessBucketName(bucketName);
		s3Client.createBucket(bucketName);
	}

	public static void deleteBucket(String bucketName) {
		bucketName = preProcessBucketName(bucketName);
		s3Client.deleteBucket(bucketName);
	}

	public static List<Bucket> listBuckets() {
		return s3Client.listBuckets();
	}
	
	public static void putObject(String bucketName, String filePath) {
		putObject(bucketName, new File(filePath));
	}

	public synchronized static void putObject(String bucketName, File file) {
		bucketName = preProcessBucketName(bucketName);
		
		PutObjectRequest request = new PutObjectRequest(bucketName, file.getName(), file);
		request.setProgressListener(new UploadProgressListener(file));
		
		Upload upload = transferManager.upload(request);
	        
		try {
			upload.waitForCompletion();
		} 
		catch (Throwable t) {
			logger.error("Unable to put object to Amazon S3 for file " + file.getName(), t);
		} 
	}
	
	public static void putObjectAsMultiPart(String bucketName, String filePath) {
		putObjectAsMultiPart(bucketName, new File(filePath));
	}
	
	public static void putObjectAsMultiPart(String bucketName, File file) {
		putObjectAsMultiPart(bucketName, file, FILE_PART_SIZE);
	}
	
	public static void putObjectAsMultiPart(String bucketName, String filePath, long partSize) {
		putObjectAsMultiPart(bucketName, new File(filePath), partSize);
	}
	
	public synchronized static void putObjectAsMultiPart(String bucketName, File file, long partSize) {
		bucketName = preProcessBucketName(bucketName);
		
		List<PartETag> partETags = new ArrayList<PartETag>();
		List<MultiPartFileUploader> uploaders = new ArrayList<MultiPartFileUploader>();
		
		// Step 1: Initialize.
		InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, file.getName());
		InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
		long contentLength = file.length();
		
		try {
		    // Step 2: Upload parts.
		    long filePosition = 0;
		    for (int i = 1; filePosition < contentLength; i++) {
		        // Last part can be less than 5 MB. Adjust part size.
		    	partSize = Math.min(partSize, (contentLength - filePosition));

		        // Create request to upload a part.
		        UploadPartRequest uploadRequest = 
		        	new UploadPartRequest().
			            withBucketName(bucketName).withKey(file.getName()).
			            withUploadId(initResponse.getUploadId()).withPartNumber(i).
			            withFileOffset(filePosition).
			            withFile(file).
			            withPartSize(partSize);

		        uploadRequest.setProgressListener(new UploadProgressListener(file, i, partSize));
		        
		        // Upload part and add response to our list.
		        MultiPartFileUploader uploader = new MultiPartFileUploader(uploadRequest);
		        uploaders.add(uploader);
		        uploader.upload();
		        //partETags.add(s3Client.uploadPart(uploadRequest).getPartETag());

		        filePosition += partSize;
		    }
		    
		    for (MultiPartFileUploader uploader : uploaders) {
		    	uploader.join();
		    	partETags.add(uploader.getPartETag());
		    }

		    // Step 3: complete.
		    CompleteMultipartUploadRequest compRequest = new 
		                CompleteMultipartUploadRequest(bucketName, 
		                                               file.getName(), 
		                                               initResponse.getUploadId(), 
		                                               partETags);

		    s3Client.completeMultipartUpload(compRequest);
		} 
		catch (Throwable t) {
			logger.error("Unable to put object as multipart to Amazon S3 for file " + file.getName(), t);
		    s3Client.abortMultipartUpload(
		    	new AbortMultipartUploadRequest(
		    			bucketName, file.getName(), initResponse.getUploadId()));
		}
	}

	public static void deleteObject(String bucketName, String fileName) {
		bucketName = preProcessBucketName(bucketName);
		s3Client.deleteObject(bucketName, fileName);
	}

	public static List<S3ObjectSummary> listObjects(String bucketName) {
		bucketName = preProcessBucketName(bucketName);
		
		List<S3ObjectSummary> allObjects = new ArrayList<S3ObjectSummary>();
		ObjectListing objectList = s3Client.listObjects(bucketName);
		while (objectList != null) {
			List<S3ObjectSummary> objects = objectList.getObjectSummaries();
			if ((objects != null) && (objects.isEmpty() == false)) {
				for (S3ObjectSummary object : objects) {
					allObjects.add(object);
				}
				if (objectList.isTruncated()) {
					objectList = s3Client.listObjects(bucketName);
				}
				else {
					break;
				}
			}
			else {
				break;
			}
		}
		return allObjects;
	}
	
	private static class MultiPartFileUploader extends Thread {
		
		private UploadPartRequest uploadRequest;
		private PartETag partETag;
		
		MultiPartFileUploader(UploadPartRequest uploadRequest) {
			this.uploadRequest = uploadRequest;
		}
		
		@Override
		public void run() {
			partETag = s3Client.uploadPart(uploadRequest).getPartETag();
		}
		
		private PartETag getPartETag() {
			return partETag;
		}
		
		private void upload() {
			start();
		}
		
	}
	
	private static class UploadProgressListener implements ProgressListener {
		
		File file;
		int partNo;
		long partLength;
		
		UploadProgressListener(File file) {
			this.file = file;
		}
		
		@SuppressWarnings("unused")
		UploadProgressListener(File file, int partNo) {
			this(file, partNo, 0);
		}
		
		UploadProgressListener(File file, int partNo, long partLength) {
			this.file = file;
			this.partNo = partNo;
			this.partLength = partLength;
		}

		@Override
		public void progressChanged(ProgressEvent progressEvent) {
			switch (progressEvent.getEventCode()) {
				case ProgressEvent.STARTED_EVENT_CODE:
					logger.info("Upload started for file " + "\"" + file.getName() + "\"");
					break;
				case ProgressEvent.COMPLETED_EVENT_CODE:
					logger.info("Upload completed for file " + "\"" + file.getName() + "\"" + 
							", " + file.length() + " bytes data has been transferred");
					break;
				case ProgressEvent.FAILED_EVENT_CODE:
					logger.info("Upload failed for file " + "\"" + file.getName() + "\"" + 
							", " + progressEvent.getBytesTransfered() + " bytes data has been transferred");
					break;
				case ProgressEvent.CANCELED_EVENT_CODE:
					logger.info("Upload cancelled for file " + "\"" + file.getName() + "\"" + 
							", " + progressEvent.getBytesTransfered() + " bytes data has been transferred");
					break;
				case ProgressEvent.PART_STARTED_EVENT_CODE:
					logger.info("Upload started at " + partNo + ". part for file " + "\"" + file.getName() + "\"");
					break;
				case ProgressEvent.PART_COMPLETED_EVENT_CODE:
					logger.info("Upload completed at " + partNo + ". part for file " + "\"" + file.getName() + "\"" + 
							", " + (partLength > 0 ? partLength : progressEvent.getBytesTransfered())  + 
							" bytes data has been transferred");
					break;
				case ProgressEvent.PART_FAILED_EVENT_CODE:
					logger.info("Upload failed at " + partNo + ". part for file " + "\"" + file.getName() + "\"" +
							", " + progressEvent.getBytesTransfered() + " bytes data has been transferred");
					break;
			}
		}

	}

}
