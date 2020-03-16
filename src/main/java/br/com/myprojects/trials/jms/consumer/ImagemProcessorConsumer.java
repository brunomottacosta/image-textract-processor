package br.com.myprojects.trials.jms.consumer;

import br.com.myprojects.trials.jms.config.ApplicationProperties;
import br.com.myprojects.trials.jms.service.ImageProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImagemProcessorConsumer {

    private final ImageProcessorService imageProcessorService;

    @Autowired
    public ImagemProcessorConsumer(ImageProcessorService imageProcessorService) {
        this.imageProcessorService = imageProcessorService;
    }

    @JmsListener(destination = "${application.aws.sqs-images-uploaded-queue}")
    public void consume(@Payload final Message<String> message) {
        String key = message.getPayload();

        log.info("Receiving message: \n     {}", key);
        imageProcessorService.process(key);
    }
}
