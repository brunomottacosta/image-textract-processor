package br.com.myprojects.trials.jms;

import br.com.myprojects.trials.jms.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.EnableJms;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
@EnableJms
public class SpringAwsJmsApp {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringAwsJmsApp.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running!" +
                        "\n----------------------------------------------------------",
                env.getProperty("spring.application.name"));
    }
}

