package com.dvide.bosschesttips.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@SuppressWarnings("unused")
public class Reflection {
    // Returns any fully-qualified method name ending in "_Impl" for the given class
    public static <T> String implMethod(final Class<T> clz) throws NoSuchMethodException {
        return getMethodByTrailingName(clz, "Impl");
    }

    // Returns any fully-qualified method name ending in "_Impl1" for the given class
    public static <T> String implMethod1(Class<T> clz) throws NoSuchMethodException {
        return getMethodByTrailingName(clz, "Impl1");
    }

    // Returns any fully-qualified method name ending in "_Impl2" for the given class
    public static <T> String implMethod2(Class<T> clz) throws NoSuchMethodException {
        return getMethodByTrailingName(clz, "Impl2");
    }

    // Returns any fully-qualified method name ending in "_Impl3" for the given class
    public static <T> String implMethod3(Class<T> clz) throws NoSuchMethodException {
        return getMethodByTrailingName(clz, "Impl3");
    }

    // Returns any fully-qualified method name ending in "_Impl4" for the given class
    public static <T> String implMethod4(Class<T> clz) throws NoSuchMethodException {
        return getMethodByTrailingName(clz, "Impl4");
    }

    // Returns any fully-qualified method name ending in "_Impl5" for the given class
    public static <T> String implMethod5(Class<T> clz) throws NoSuchMethodException {
        return getMethodByTrailingName(clz, "Impl5");
    }

    // Returns any fully-qualified method name ending in '_' + String trailingName for the given class
    public static <T> String getMethodByTrailingName(final Class<T> clz, final String trailingName) throws NoSuchMethodException {
        final String className = clz.getName();
        final Optional<Method> implMethod = Arrays.stream(clz.getDeclaredMethods()).filter(m -> m.getName().endsWith(trailingName)).findAny();
        if (!implMethod.isPresent()) {
            throw new NoSuchMethodException("No '*_" + trailingName + "' method present");
        }

        return className + "." + implMethod.get().getName();
    }
}
