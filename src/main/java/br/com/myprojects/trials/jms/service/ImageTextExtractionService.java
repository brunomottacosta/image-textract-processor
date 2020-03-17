package br.com.myprojects.trials.jms.service;

import br.com.myprojects.trials.jms.config.ApplicationProperties;
import br.com.myprojects.trials.jms.data.entity.ImageText;
import br.com.myprojects.trials.jms.data.repository.ImageTextRepository;
import br.com.myprojects.trials.jms.service.exceptions.ImageTextExtractionException;
import br.com.myprojects.trials.jms.util.AwsS3Utils;
import br.com.myprojects.trials.jms.util.AwsTextractUtils;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ImageTextExtractionService {

    private final ApplicationProperties applicationProperties;
    private final ImageTextRepository imageTextRepository;

    private AmazonS3 s3;
    private AmazonTextract textract;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public ImageTextExtractionService(
            ApplicationProperties applicationProperties,
            ImageTextRepository imageTextRepository) {
        this.applicationProperties = applicationProperties;
        this.imageTextRepository = imageTextRepository;
    }

    /**
     * Receives an S3 object key and send it to textract
     *
     * @param key the S3 object key
     */
    public void processExtractionFromKey(String key) {
        String sourceBucket = applicationProperties.getAws().getS3SourceBucket();

        // checks if object key exists in origin bucket (images to proccess)
        try (S3Object s3Object = AwsS3Utils.getObject(getAwsS3Client(), sourceBucket, key)) {
            assert s3Object != null;
            var request = new DetectDocumentTextRequest()
                    .withDocument(new Document()
                            // s3 object from textract
                            .withS3Object(new com.amazonaws.services.textract.model.S3Object()
                                    .withName(s3Object.getKey())
                                    .withBucket(s3Object.getBucketName())));

            DetectDocumentTextResult result = getAwsTextractClient().detectDocumentText(request);
            imageTextRepository.save(ImageText.builder()
                    .extractedTextBlocks(result.getBlocks())
                    .s3ObjectKey(s3Object.getKey())
                    .s3Bucket(s3Object.getBucketName())
                    .imageByteLength(s3Object.getObjectMetadata().getContentLength())
                    .build());

            // cancels object data stream
            s3Object.getObjectContent().abort();
        } catch (AmazonServiceException | IOException var0) {
            log.error(var0.getMessage(), var0);
            throw new AmazonS3Exception("Error while connecting to Amazon S3:", var0);
        } catch (Exception var1) {
            log.error(var1.getMessage(), var1);
            throw new ImageTextExtractionException(String.format("Error processing key %s", key));
        }
    }

    private AmazonS3 getAwsS3Client() {
        if (s3 == null) {
            s3 = AwsS3Utils.createClient(
                    applicationProperties.getAws().getAccessKeyId(),
                    applicationProperties.getAws().getSecretAccessKey(),
                    Regions.fromName(applicationProperties.getAws().getRegion()));
        }
        return s3;
    }

    private AmazonTextract getAwsTextractClient() {
        if (textract == null) {
            textract = AwsTextractUtils.createClient(
                    applicationProperties.getAws().getAccessKeyId(),
                    applicationProperties.getAws().getSecretAccessKey(),
                    Regions.fromName(applicationProperties.getAws().getRegion()));
        }
        return textract;
    }
}
