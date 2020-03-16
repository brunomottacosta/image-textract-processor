package br.com.myprojects.trials.jms.consumer;

import br.com.myprojects.trials.jms.config.ApplicationProperties;
import br.com.myprojects.trials.jms.service.ImagemProcessorService;
import br.com.myprojects.trials.jms.util.ImagemProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImagemProcessorConsumer {

    private final ApplicationProperties applicationProperties;
    private final ImagemProcessorService imagemProcessorService;

    @Autowired
    public ImagemProcessorConsumer(ApplicationProperties applicationProperties, ImagemProcessorService imagemProcessorService) {
        this.applicationProperties = applicationProperties;
        this.imagemProcessorService = imagemProcessorService;
    }

    @JmsListener(destination = "${application.aws.sqs-images-uploaded-queue}")
    public void consume(@Payload final Message<String> message) {
        String key = message.getPayload();

        log.info("Receiving message: \n     {}", key);
        if (isValidKey(key)) {
            log.info("Start processing...");
            imagemProcessorService.process(key);
        } else {
            log.info("Not a valid key");
        }
    }

    // the key to be processed must follow a default pattern
    // sample: contracts/temp/##############/AAAAAAAAAAAAAAAAA.xxx
    private boolean isValidKey(String key) {
        try {
            val chassi = ImagemProcessorUtils.extractChassiFromKey(key);
            return key.contains(applicationProperties.getFiles().getSourcePrefix())
                    && chassi.matches("^[0-9A-Z]{4,21}$");
        } catch (Exception ex) {
            return false;
        }
    }
}
