package br.com.myprojects.trials.jms.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AwsTextractUtils {

    public static AmazonTextract createClient(String accessKeyId, String secretAccessKey, Regions region) {
        var builder = AmazonTextractClientBuilder.standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        accessKeyId,
                                        secretAccessKey)))
                .withRegion(region);
        return builder.build();
    }
}
