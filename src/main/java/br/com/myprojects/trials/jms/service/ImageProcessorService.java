package br.com.myprojects.trials.jms.service;

import br.com.myprojects.trials.jms.config.ApplicationProperties;
import br.com.myprojects.trials.jms.service.exceptions.ImageProcessorException;
import br.com.myprojects.trials.jms.util.AwsS3Utils;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ImageProcessorService {

    private final ApplicationProperties applicationProperties;

    private AmazonS3 s3;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public ImageProcessorService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void process(String sourceKey) {
        String sourceBucket = applicationProperties.getAws().getS3SourceBucket();

        // checks if object key exists in origin bucket (images to proccess)
        try (S3Object s3Object = AwsS3Utils.getObject(getAwsS3Client(), sourceBucket, sourceKey)) {
            // s3 object (image) should exists
            if (s3Object != null) {

                // TODO: do the thing

                // only reads object metadata then cancels data stream
                s3Object.getObjectContent().abort();
            }
        } catch (AmazonServiceException | IOException var0) {
            log.error(var0.getMessage(), var0);
            throw new AmazonS3Exception("Error while connecting to Amazon S3:", var0);
        } catch (Exception var1) {
            log.error(var1.getMessage(), var1);
            throw new ImageProcessorException(String.format("Error processing key %s", sourceKey));
        }
    }

    // stores s3 client in request scope of this service
    private AmazonS3 getAwsS3Client() {
        if (s3 == null) {
            s3 = AwsS3Utils.createClient(
                    applicationProperties.getAws().getAccessKeyId(),
                    applicationProperties.getAws().getSecretAccessKey(),
                    Regions.fromName(applicationProperties.getAws().getRegion()));
        }
        return s3;
    }
}
