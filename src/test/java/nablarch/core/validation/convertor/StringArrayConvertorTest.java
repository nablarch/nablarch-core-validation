package nablarch.core.validation.convertor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.creator.ReflectionFormCreator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Enclosed.class)
public class StringArrayConvertorTest {

    @RunWith(Parameterized.class)
    public static class 変換可能の場合のテスト {

        private final StringArrayConvertor sut = new StringArrayConvertor();

        private final String[] params;

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(
                    new Object[] {new String[] {"1", "2"}},
                    new Object[] {new String[] {"1", "2", "3"}},
                    new Object[] {new String[] {null}},
                    new Object[] {null});
        }

        public 変換可能の場合のテスト(final String[] params) {
            this.params = params;
        }

        @Test
        public void test() {
            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class, new ReflectionFormCreator(), Collections.<String, Object>emptyMap(), "");


            assertThat(sut.isConvertible(context, "param", "param", params, null), is(true));
            assertThat((String[]) sut.convert(context, "param", params, null), is(params));
        }
    }

    @RunWith(Parameterized.class)
    public static class 変換不可能な場合のテスト {

        private final StringArrayConvertor sut = new StringArrayConvertor();

        private final Object value;

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(
                    new Object[] {new Object[] {"1", "2"}},
                    new Object[] {new Integer[] {1, 2, 3}},
                    new Object[] {"string"},
                    new Object[] {BigDecimal.ONE});
        }

        public 変換不可能な場合のテスト(final Object value) {
            this.value = value;
        }

        @Test
        public void test() {
            ValidationContext<TestTarget> context = new ValidationContext<TestTarget>(
                    "", TestTarget.class, new ReflectionFormCreator(), Collections.<String, Object>emptyMap(), "");

            expectedException.expect(IllegalArgumentException.class);
            sut.isConvertible(context, "prop", "prop", value, null);
        }
    }
}
