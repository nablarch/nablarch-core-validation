package nablarch.core.validation.validator;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.hamcrest.FeatureMatcher;

import nablarch.core.message.Message;
import nablarch.core.message.MockStringResourceHolder;
import nablarch.core.util.StringUtil;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.creator.ReflectionFormCreator;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * {@link DecimalRangeValidator}のテスト。
 */
@RunWith(Enclosed.class)
public class DecimalRangeValidatorTest {

    private static class DecimalRangeImpl implements DecimalRange {

        private final String min;

        private final String max;

        private final String messageId;

        private DecimalRangeImpl(final String min, final String max, final String messageId) {
            this.min = min;
            this.max = max;
            this.messageId = messageId;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return DecimalRange.class;
        }

        @Override
        public String min() {
            return min;
        }

        @Override
        public String max() {
            return max;
        }

        @Override
        public String messageId() {
            return messageId;
        }
    }

    private static class Param {

        private final String min;

        private final String max;

        private final String messageId;

        private final Object value;

        private Param(final String min, final String max, final String messageId, final Object value) {
            this.min = min;
            this.max = max;
            this.messageId = messageId;
            this.value = value;
        }
    }

    /**
     * バリデーションOKとなるパターンを実施する。
     */
    @RunWith(Parameterized.class)
    public static final class バリデーションOK {

        @Parameters
        public static Collection<Param> data() {
            return Arrays.asList(
                    new Param("0", "1", "", null),
                    new Param("1", "", "", Long.MAX_VALUE),
                    new Param("", "1", "", Long.MIN_VALUE),
                    new Param("0.00001", "0.99999", "", new BigDecimal("0.00001")),
                    new Param("0.00001", "0.99999", "", new BigDecimal("0.99999")),
                    new Param("0.00001", "0.99999", "", 0.99D),
                    new Param("1", "1.99999", "custom", 1L)
            );
        }

        private final Param param;

        public バリデーションOK(final Param param) {
            this.param = param;
        }

        @Test
        public void test() {
            final ValidationContext<Object> context = new ValidationContext<Object>("", Object.class,
                    new ReflectionFormCreator(), new HashMap<String, Object>(), "");


            final DecimalRangeValidator sut = new DecimalRangeValidator();

            // annotation 
            final DecimalRangeImpl range = new DecimalRangeImpl(param.min, param.max, param.messageId);
            assertThat(sut.validate(context, "property", "表示名", range, param.value), is(true));

            // map
            final Map<String, Object> def = new HashMap<String, Object>() {{
                put("min", param.min);
                put("max", param.max);
                put("messageId", param.messageId);
            }};
            assertThat(sut.validate(context, "property", "表示名", def, param.value), is(true));
        }
    }

    /**
     * バリデーションNGとなるパターンを実施する。
     */
    @RunWith(Parameterized.class)
    public static class バリデーションNG {

        private static final String[][] MESSAGES = {
                {"max", "ja", "{0}は{2}以下で入力してください。", "EN", "{0} cannot be greater than {2}."},
                {"min-max", "ja", "{0}は{1}以上{2}以下で入力してください。", "EN", "{0} is not in the range {1} through {2}."},
                {"min", "ja", "{0}は{1}以上で入力してください。", "EN", "{0} cannot be lesser than {2}."},
                {"custom", "ja", "{0}は{1}と{2}の間で入力してください。", "EN", "{0} cannot be lesser than {2}."}
        };

        @Parameters
        public static Collection<Param> data() {
            return Arrays.asList(
                    new Param("", "1", "", 2),
                    new Param("100", "", "", 99L),
                    new Param("0.00001", "0.99999", "", new BigDecimal("0.000001")),
                    new Param("0.00001", "0.99999", "", new BigDecimal("0.999991")),
                    new Param("0.00001", "0.99999", "custom", 0.999991D)
            );
        }

        private final Param param;

        public バリデーションNG(final Param param) {
            this.param = param;
        }

        @Rule
        public SystemRepositoryResource repositoryResource = new SystemRepositoryResource(
                "nablarch/core/validation/convertor-test-base.xml");

        @Before
        public void setUp() {
            final MockStringResourceHolder stringResource = repositoryResource.getComponentByType(
                    MockStringResourceHolder.class);
            stringResource.setMessages(MESSAGES);
        }

        @Test
        public void test() {
            final DecimalRangeValidator sut = new DecimalRangeValidator();
            sut.setMinMessageId("min");
            sut.setMaxMessageId("max");
            sut.setMaxAndMinMessageId("min-max");

            // annotation
            final DecimalRangeImpl range = new DecimalRangeImpl(param.min, param.max, param.messageId);
            ValidationContext<Object> context = new ValidationContext<Object>("", Object.class,
                    new ReflectionFormCreator(), new HashMap<String, Object>(), "");
            assertThat(sut.validate(context, "property", "表示名", range, param.value), is(false));
            assertThat(context.getMessages(), hasMessage(param));

            // map
            final Map<String, Object> def = new HashMap<String, Object>() {{
                put("min", param.min);
                put("max", param.max);
                put("messageId", param.messageId);
            }};
            context = new ValidationContext<Object>("", Object.class,
                    new ReflectionFormCreator(), new HashMap<String, Object>(), "");
            assertThat(sut.validate(context, "property", "表示名", def, param.value), is(false));
            assertThat(context.getMessages(), hasMessage(param));
        }

        private FeatureMatcher<List<Message>, List<String>> hasMessage(final Param param) {
            final String expectedMessage;
            if (StringUtil.hasValue(param.messageId)) {
                expectedMessage = "custom:表示名は" + param.min + "と" + param.max + "の間で入力してください。";
            } else if (StringUtil.isNullOrEmpty(param.max)) {
                expectedMessage = "min:表示名は" + param.min + "以上で入力してください。";
            } else if (StringUtil.isNullOrEmpty(param.min)) {
                expectedMessage = "max:表示名は" + param.max + "以下で入力してください。";
            } else {
                expectedMessage = "min-max:表示名は" + param.min + "以上" + param.max + "以下で入力してください。";
            }
            return new FeatureMatcher<List<Message>, List<String>>(hasItem(expectedMessage), null, null) {
                @Override
                protected List<String> featureValueOf(final List<Message> actual) {
                    final ArrayList<String> result = new ArrayList<String>();
                    for (final Message message : actual) {
                        result.add(message.getMessageId() + ':' + message.formatMessage());
                    }
                    return result;
                }
            };
        }
    }

    public static class 異常なケース {

        private final DecimalRangeValidator sut = new DecimalRangeValidator();

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Test
        public void Rangeの最小値が非数値の場合は例外が送出されること() {
            expectedException.expect(IllegalArgumentException.class);
            expectedException.expectMessage("invalid decimal range.");
            expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(NumberFormatException.class));

            final ValidationContext<Object> context = new ValidationContext<Object>("", Object.class,
                    new ReflectionFormCreator(), new HashMap<String, Object>(), "");

            final DecimalRangeImpl range = new DecimalRangeImpl("a", "10", "");
            sut.validate(context, "prop", "prop", range, "1");
        }

        @Test
        public void Rangeの最大値が非数値の場合は例外が送出されること() {
            expectedException.expect(IllegalArgumentException.class);
            expectedException.expectMessage("invalid decimal range.");
            expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(NumberFormatException.class));

            final ValidationContext<Object> context = new ValidationContext<Object>("", Object.class,
                    new ReflectionFormCreator(), new HashMap<String, Object>(), "");

            final DecimalRangeImpl range = new DecimalRangeImpl("1", " ", "");
            sut.validate(context, "prop", "prop", range, "1");
        }

        @Test
        public void validation対象の値がNumberのサブタイプでない場合例外が送出sれること() {
            expectedException.expect(IllegalArgumentException.class);
            expectedException.expectMessage(
                    "unsupported data type. supported type:[Number], actual type:java.lang.String");

            final ValidationContext<Object> context = new ValidationContext<Object>("", Object.class,
                    new ReflectionFormCreator(), new HashMap<String, Object>(), "");

            final DecimalRangeImpl range = new DecimalRangeImpl("1", "2", "");
            sut.validate(context, "prop", "prop", range, "a");
        }
    }
}
