package br.com.myprojects.trials.jms.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PredicateUtils {

    public static <T> Predicate<T> negate(Predicate<T> predicate) {
        return predicate.negate();
    }
}
