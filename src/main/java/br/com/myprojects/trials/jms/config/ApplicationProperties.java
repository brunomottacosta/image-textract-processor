package br.com.myprojects.trials.jms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Aws aws = new Aws();
    private final Files files = new Files();

    @Getter
    @Setter
    public static class Aws {
        private String region;
        private String accessKeyId;
        private String secretAccessKey;
        private String sqsImagesUploadedQueue;
        private String s3SourceBucket;
        private String s3DestinationBucket;
    }

    @Getter
    @Setter
    public static class Files {
        private String sourcePrefix;
        private String destinationPrefix;
        private String processedPrefix;
    }
}
