package br.com.myprojects.trials.jms.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@SuppressWarnings("unused")
public class ResourceBundleUtils {

    private final MessageSource messageSource;

    @Autowired
    public ResourceBundleUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code) {
        return getMessageWithLocale(code, new Locale("pt","BR"));
    }

    public String getMessageWithLocale(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }
}
