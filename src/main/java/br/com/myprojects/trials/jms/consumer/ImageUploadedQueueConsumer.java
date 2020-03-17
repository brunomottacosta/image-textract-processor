package br.com.myprojects.trials.jms.consumer;

import br.com.myprojects.trials.jms.service.ImageTextExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listens to SQS queue that receives uploaded images key
 */
@Slf4j
@Component
public class ImageUploadedQueueConsumer {

    private final ImageTextExtractionService imageTextractService;

    @Autowired
    public ImageUploadedQueueConsumer(ImageTextExtractionService imageTextractService) {
        this.imageTextractService = imageTextractService;
    }

    @JmsListener(destination = "${application.aws.sqs-queue}")
    public void consume(@Payload final Message<String> message) {
        String key = message.getPayload();

        log.info("Receiving new object key: \n     {}", key);
        imageTextractService.processExtractionFromKey(key);
    }
}
