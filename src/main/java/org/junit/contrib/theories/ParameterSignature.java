package org.junit.contrib.theories;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javaruntype.type.Types;

public class ParameterSignature {
    private static final Map<Type, Type> CONVERTIBLE_TYPES_MAP = buildConvertibleTypesMap();

    private static Map<Type, Type> buildConvertibleTypesMap() {
        Map<Class<?>, Class<?>> map = new HashMap<>();

        putSymmetrically(map, boolean.class, Boolean.class);
        putSymmetrically(map, byte.class, Byte.class);
        putSymmetrically(map, short.class, Short.class);
        putSymmetrically(map, char.class, Character.class);
        putSymmetrically(map, int.class, Integer.class);
        putSymmetrically(map, long.class, Long.class);
        putSymmetrically(map, float.class, Float.class);
        putSymmetrically(map, double.class, Double.class);

        return Collections.unmodifiableMap(map);
    }

    private static <T> void putSymmetrically(Map<T, T> map, T a, T b) {
        map.put(a, b);
        map.put(b, a);
    }

    public static List<ParameterSignature> signatures(Method method) {
        return signatures(method.getGenericParameterTypes(), method.getParameters());
    }

    public static List<ParameterSignature> signatures(Constructor<?> constructor) {
        return signatures(constructor.getGenericParameterTypes(), constructor.getParameters());
    }

    private static List<ParameterSignature> signatures(Type[] parameterTypes, Parameter[] parameters) {
        List<ParameterSignature> sigs = new ArrayList<>();
        for (int i = 0; i < parameterTypes.length; i++) {
            sigs.add(new ParameterSignature(
                    parameterTypes[i],
                    parameters[i].getAnnotations(),
                    parameters[i].getName()));
        }
        return sigs;
    }

    private final Type type;
    private final Annotation[] annotations;
    private final String name;

    private ParameterSignature(Type type, Annotation[] annotations, String name) {
        this.type = type;
        this.annotations = annotations;
        this.name = name;
    }

    public boolean canAcceptValue(Object candidate) {
        return candidate == null
                ? !Types.forJavaLangReflectType(type).getRawClass().isPrimitive()
                : canAcceptType(candidate.getClass());
    }

    public boolean canAcceptType(Type candidate) {
        return assignable(type, candidate) || isAssignableViaTypeConversion(type, candidate);
    }

    public boolean canPotentiallyAcceptType(Class<?> candidate) {
        return assignable(candidate, type)
                || isAssignableViaTypeConversion(candidate, type)
                || canAcceptType(candidate);
    }

    private static boolean isAssignableViaTypeConversion(Type targetType, Type candidate) {
        if (CONVERTIBLE_TYPES_MAP.containsKey(candidate)) {
            Type wrapper = CONVERTIBLE_TYPES_MAP.get(candidate);
            return assignable(targetType, wrapper);
        }
        return false;
    }

    private static boolean assignable(Type first, Type second) {
        return Types.forJavaLangReflectType(first).isAssignableFrom(Types.forJavaLangReflectType(second));
    }

    public Type getType() {
        return type;
    }

    public List<Annotation> getAnnotations() {
        return Arrays.asList(annotations);
    }

    public String getName() {
        return name;
    }

    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return getAnnotation(type) != null;
    }

    public <T extends Annotation> T findDeepAnnotation(Class<T> annotationType) {
        return findDeepAnnotation(annotations, annotationType, 3);
    }

    private <T extends Annotation> T findDeepAnnotation(Annotation[] annotations,
            Class<T> annotationType, int depth) {

        if (depth == 0) {
            return null;
        }

        for (Annotation each : annotations) {
            if (annotationType.isInstance(each)) {
                return annotationType.cast(each);
            }

            Annotation candidate =
                    findDeepAnnotation(each.annotationType().getAnnotations(), annotationType, depth - 1);
            if (candidate != null) {
                return annotationType.cast(candidate);
            }
        }

        return null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation each : getAnnotations()) {
            if (annotationType.isInstance(each)) {
                return annotationType.cast(each);
            }
        }

        return null;
    }
}
