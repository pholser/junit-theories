package org.junit.contrib.tests.theories;

import org.junit.Test;
import org.junit.contrib.theories.DataPoint;
import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.contrib.theories.suppliers.TestedOn;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.junit.contrib.theories.ParameterSignature.*;

@RunWith(Theories.class)
public class ParameterSignatureTest {
    @DataPoint public static Method getType() throws Exception {
        return ParameterSignatureTest.class.getMethod("getType", Method.class, int.class);
    }

    @DataPoint public static int ZERO = 0;
    @DataPoint public static int ONE = 1;

    @Theory public void getType(Method method, int index) {
        assumeTrue(index < method.getParameterTypes().length);

        assertEquals(method.getParameterTypes()[index], signatures(method).get(index).getType());
    }

    public void foo(@TestedOn(ints = {1, 2, 3}) int x) {
    }

    @Test public void getAnnotations() throws Exception {
        Method method = getClass().getMethod("foo", int.class);

        List<Annotation> annotations = Arrays.asList(signatures(method).get(0).getAnnotations());

        assertThat(annotations, hasItem(isA(TestedOn.class)));
    }

    @Test public void getParameterNames() throws Exception {
        Method method = getClass().getMethod("foo", int.class);

        assertThat(signatures(method).get(0).getName(), anyOf(is("x"), is("arg0")));
    }

    @Test public void getAnnotatedType() throws Exception {
        Method method = getClass().getMethod("annotatedTypeMethod", List.class);

        ParameterSignature sig = signatures(method).get(0);

        AnnotatedType annotatedType = sig.getAnnotatedType();
        assertThat(annotatedType, instanceOf(AnnotatedParameterizedType.class));
        AnnotatedParameterizedType parameterized = (AnnotatedParameterizedType) annotatedType;
        assertThat(asList(parameterized.getAnnotatedActualTypeArguments()[0].getAnnotations()),
                hasItem(isA(Positive.class)));
    }

    @Target(TYPE_USE)
    @Retention(RUNTIME)
    public @interface Positive {
    }

    public void annotatedTypeMethod(List<@Positive Integer> param) {
    }

    public void intMethod(int param) {
    }

    public void integerMethod(Integer param) {
    }

    public void numberMethod(Number param) {
    }

    @Test public void primitiveTypesShouldBeAcceptedAsWrapperTypes() throws Exception {
        List<ParameterSignature> signatures =
                signatures(getClass().getMethod("integerMethod", Integer.class));
        ParameterSignature integerSignature = signatures.get(0);

        assertTrue(integerSignature.canAcceptType(int.class));
    }

    @Test public void primitiveTypesShouldBeAcceptedAsWrapperTypeAssignables() throws Exception {
        List<ParameterSignature> signatures =
                signatures(getClass().getMethod("numberMethod", Number.class));
        ParameterSignature numberSignature = signatures.get(0);

        assertTrue(numberSignature.canAcceptType(int.class));
    }

    @Test public void wrapperTypesShouldBeAcceptedAsPrimitiveTypes() throws Exception {
        List<ParameterSignature> signatures =
                signatures(getClass().getMethod("intMethod", int.class));
        ParameterSignature intSignature = signatures.get(0);

        assertTrue(intSignature.canAcceptType(Integer.class));
    }
}
