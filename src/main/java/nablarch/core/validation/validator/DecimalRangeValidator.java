package nablarch.core.validation.validator;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Map;

import nablarch.core.util.StringUtil;
import nablarch.core.validation.DirectCallableValidator;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;

/**
 * 小数部を含む数値の範囲バリデーションを行う。
 * <p>
 * 最小値が{@code 0.00001}で最大値が{@code 0.99999}の範囲バリデーションを行う場合には、
 * 以下の用にバリデーション用のアノテーションを定義する。
 * 本バリデーションは、数値型({@link Number}のサブタイプ)以外は許容しないため、
 * {@link nablarch.core.validation.convertor.Digits}セットで設定すること。
 * <pre>
 * {@code @Digits(integer = 1, fraction = 5)}
 * {@code @DecimalRange(min = "0.00001", max = "0.99999")}
 * {@code
 * public void setRate(final BigDecimal rate) {
 *     this.rate = rate;
 * }
 * }
 * </pre>
 * 
 * @author siosio
 * @see DecimalRange
 */
public class DecimalRangeValidator implements DirectCallableValidator {

    /**
     * バリデーションの条件に最小値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID。
     */
    private String minMessageId;

    /**
     * バリデーションの条件に最大値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID
     */
    private String maxMessageId;

    /**
     * バリデーションの条件に最大値と最小値が指定されていた場合のデフォルトのエラーメッセージのメッセージID。
     */
    private String maxAndMinMessageId;

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return DecimalRange.class;
    }

    @Override
    public <T> boolean validate(final ValidationContext<T> context, final String propertyName,
            final Object propertyDisplayName,
            final Map<String, Object> params, final Object value) {
        final DecimalRange range = new DecimalRange() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return DecimalRange.class;
            }

            @Override
            public String min() {
                return (String) params.get("min");
            }

            @Override
            public String max() {
                return (String) params.get("max");
            }

            @Override
            public String messageId() {
                return (String) params.get("messageId");
            }
        };
        return validate(context, propertyName, propertyDisplayName, range, value);
    }

    @Override
    public <T> boolean validate(final ValidationContext<T> context, final String propertyName,
            final Object propertyDisplayName,
            final Annotation annotation, final Object value) {

        // nullはバリデーションOK
        if (value == null) {
            return true;
        }

        final DecimalRange decimalRange = (DecimalRange) annotation;
        final Range range;
        try {
            range = new Range(decimalRange.min(), decimalRange.max());
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("invalid decimal range.", e);
        }

        if (!range.includes(toBigDecimal(value))) {
            ValidationResultMessageUtil.addResultMessage(
                    context, propertyName, getMessageId(decimalRange), propertyDisplayName, range.getMinString(),
                    range.getMaxString());
            return false;
        }
        return true;
    }

    /**
     * 使用するメッセージのIDを返す。
     *
     * @param decimalRange {@link DecimalRange}
     * @return メッセージのID
     */
    private String getMessageId(final DecimalRange decimalRange) {
        if (StringUtil.hasValue(decimalRange.messageId())) {
            return decimalRange.messageId();
        } else {
            if (StringUtil.hasValue(decimalRange.min()) && StringUtil.hasValue(decimalRange.max())) {
                return maxAndMinMessageId;
            } else if (StringUtil.hasValue(decimalRange.min())) {
                return minMessageId;
            } else {
                return maxMessageId;
            }
        }
    }

    /**
     * バリデーション対象の値を{@link BigDecimal}に変換する。
     * <p>
     * 変換出来ない場合は、{@link IllegalArgumentException}を送出する。
     *
     * @param value 変換対象の値
     * @return 変換後の値
     */
    private BigDecimal toBigDecimal(final Object value) {
        if (value instanceof BigDecimal) {
            return BigDecimal.class.cast(value);
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        throw new IllegalArgumentException("unsupported data type."
                + " supported type:[Number],"
                + " actual type:" + value.getClass()
                                         .getName());
    }

    /**
     * 最小値のみ指定した際のメッセージのIDを設定する。
     *
     * @param minMessageId メッセージID
     */
    public void setMinMessageId(final String minMessageId) {
        this.minMessageId = minMessageId;
    }

    /**
     * 最大値のみ指定した際のメッセージのIDを設定する。
     *
     * @param maxMessageId メッセージID
     */
    public void setMaxMessageId(final String maxMessageId) {
        this.maxMessageId = maxMessageId;
    }

    /**
     * 最小値と最大値を指定した際のメッセージのIDを設定する。
     *
     * @param maxAndMinMessageId メッセージID
     */
    public void setMaxAndMinMessageId(final String maxAndMinMessageId) {
        this.maxAndMinMessageId = maxAndMinMessageId;
    }
}
