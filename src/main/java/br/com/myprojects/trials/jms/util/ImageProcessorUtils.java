package br.com.myprojects.trials.jms.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImagemProcessorUtils {

    public static String extractChassiFromKey(String key) {
        return key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'));
    }

    public static String extractCnpjFromKey(String key) {
        return key.substring(key.lastIndexOf("temp/") + 5, key.lastIndexOf('/'));
    }

    public static String extractFileExtensionFromKey(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    public static String createProcessedKey(String key, String processedPrefix) {
        return processedPrefix.concat(key.substring(key.lastIndexOf("temp/") + 5));
    }
}
