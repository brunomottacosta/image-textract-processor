package br.com.myprojects.trials.jms.util;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AwsS3Utils {

    public static AmazonS3 createClient(String accessKeyId, String secretAccessKey, Regions region) {
        var builder = AmazonS3ClientBuilder.standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        accessKeyId,
                                        secretAccessKey)));
        builder.withPathStyleAccessEnabled(true).withRegion(region);
        return builder.build();
    }

    public static S3Object getObject(AmazonS3 s3, String bucket, String key) {
        try {
            var request = new GetObjectRequest(bucket, key);
            return s3.getObject(request);
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean objectExists(AmazonS3 s3, String bucket, String key) {
        S3Object s3Object = getObject(s3, bucket, key);
        boolean exists = s3Object != null;
        if (exists) s3Object.getObjectContent().abort();
        return exists;
    }

    public static boolean copyObjectToSameBucket(
            AmazonS3 s3,
            String bucket,
            String sourceKey,
            String destinationKey) {
        return copyObject(s3, bucket, bucket, sourceKey, destinationKey);
    }

    public static boolean copyObject(
            AmazonS3 s3,
            String sourceBucket, String destinationBucket,
            String sourceKey, String destinationKey) {
        try {
            var request = new CopyObjectRequest(sourceBucket, sourceKey, destinationBucket, destinationKey);
            CopyObjectResult result = s3.copyObject(request);
            return result != null;
        } catch (SdkClientException var8) {
            return false;
        }
    }

    public static void removeObject(AmazonS3 s3, String bucket, String key) {
        DeleteObjectRequest request = new DeleteObjectRequest(bucket, key);
        try {
            s3.deleteObject(request);
        } catch (Exception var5) {
            log.error(var5.getMessage(), var5);
        }
    }
}
