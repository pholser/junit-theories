package org.junit.contrib.theories;

import org.javaruntype.type.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterSignature implements AnnotatedElement {
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

    public static List<ParameterSignature> signatures(Executable e) {
        List<ParameterSignature> sigs = new ArrayList<>();
        for (Parameter each : e.getParameters()) {
            sigs.add(new ParameterSignature(each));
        }
        return sigs;
    }

    private final Parameter parameter;

    private ParameterSignature(Parameter parameter) {
        this.parameter = parameter;
    }

    public boolean canAcceptValue(Object candidate) {
        return candidate == null
                ? !Types.forJavaLangReflectType(getType()).getRawClass().isPrimitive()
                : canAcceptType(candidate.getClass());
    }

    public boolean canAcceptType(Type candidate) {
        return assignable(getType(), candidate) || isAssignableViaTypeConversion(getType(), candidate);
    }

    public boolean canPotentiallyAcceptType(Class<?> candidate) {
        return assignable(candidate, getType())
                || isAssignableViaTypeConversion(candidate, getType())
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
        return getAnnotatedType().getType();
    }

    public AnnotatedType getAnnotatedType() {
        return parameter.getAnnotatedType();
    }

    @Override
    public Annotation[] getAnnotations() {
        return parameter.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return parameter.getDeclaredAnnotations();
    }

    public String getName() {
        return parameter.getName();
    }

    public String getDeclarerName() {
        Executable exec = parameter.getDeclaringExecutable();
        return exec.getDeclaringClass().getName() + '.' + exec.getName();
    }

    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return getAnnotation(type) != null;
    }

    public <T extends Annotation> T findDeepAnnotation(Class<T> annotationType) {
        return findDeepAnnotation(parameter.getAnnotations(), annotationType, 3);
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
