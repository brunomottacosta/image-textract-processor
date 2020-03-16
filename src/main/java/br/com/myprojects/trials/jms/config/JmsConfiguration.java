package br.com.myprojects.trials.jms.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.annotation.PostConstruct;
import javax.jms.Session;

@Configuration
public class JmsConfiguration {

    private final ApplicationProperties applicationProperties;
    private SQSConnectionFactory connectionFactory;

    public JmsConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @PostConstruct
    public void init() {
        connectionFactory = createSQSConnectionFactory();
    }

    private SQSConnectionFactory createSQSConnectionFactory() {
        final AmazonSQS sqs = AmazonSQSClient.builder()
                .withRegion(applicationProperties.getAws().getRegion())
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        applicationProperties.getAws().getAccessKeyId(),
                                        applicationProperties.getAws().getSecretAccessKey())))
                .build();
        return new SQSConnectionFactory(new ProviderConfiguration(), sqs);
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency("2-6");
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return factory;
    }

    @Bean
    public JmsTemplate defaultJmsTemplate() {
        return new JmsTemplate(connectionFactory);
    }
}
