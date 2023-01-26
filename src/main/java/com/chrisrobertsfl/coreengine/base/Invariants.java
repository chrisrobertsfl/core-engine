package com.chrisrobertsfl.coreengine.base;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class Invariants {
    public static <T> T isTrueOrElseThrow(T target, Predicate<T> predicate, Supplier<? extends RuntimeException> supplier) {
        if (predicate.test(target)) {
            return target;
        }
        throw supplier.get();
    }

    public static void isTrueOrElseThrow(boolean b, Supplier<? extends RuntimeException> supplier) {
        if (!b) {
            throw supplier.get();
        }
    }

}
