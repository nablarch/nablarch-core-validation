package nablarch.core.validation.convertor;

import java.lang.annotation.Annotation;

import nablarch.core.validation.Convertor;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;

/**
 * 値をBooleanに変換するクラス。
 * 
 * @author TIS
 */
public class BooleanConvertor implements Convertor {

    /**
     * 変換失敗時のデフォルトのエラーメッセージのメッセージID。
     */
    private String conversionFailedMessageId;

    /**
     * 変換対象の値にnullを許可するか否か。
     */
    private boolean allowNullValue = true;

    /**
     * 変換失敗時のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * デフォルトメッセージの例 : "{0}が正しくありません"
     *
     * @param conversionFailedMessageId 変換失敗時のデフォルトのエラーメッセージのメッセージID
     */
    public void setConversionFailedMessageId(String conversionFailedMessageId) {

        this.conversionFailedMessageId = conversionFailedMessageId;
    }

    /**
     * 変換対象の値にnullを許可するか否かを設定する。
     * <p/>
     * 設定を省略した場合、nullが許可される。
     *
     * @param allowNullValue nullを許可するか否か。許可する場合は、true
     */
    public void setAllowNullValue(boolean allowNullValue) {

        this.allowNullValue = allowNullValue;
    }

    /**
     * {@inheritDoc}
     */
    public <T> Object convert(ValidationContext<T> context, String propertyName, Object value, Annotation format) {

        if (value == null) {
            return Boolean.FALSE;
        } else if (value instanceof Boolean) {
            return value;
        } else if (value instanceof String[]) {
            final String[] values = (String[]) value;
            return values[0] == null ? false : Boolean.valueOf(values[0]);
        } else {
            return Boolean.valueOf(value.toString());
        }
    }

    @Override
    public Class<?> getTargetClass() {
        return Boolean.class;
    }

    @Override
    public <T> boolean isConvertible(ValidationContext<T> context,
            String propertyName, Object propertyDisplayName, Object value,
            Annotation format) {

        final boolean convertible = isConvertible(value);
        if (!convertible) {
            ValidationResultMessageUtil.addResultMessage(context, propertyName,
                                                        conversionFailedMessageId, propertyDisplayName);
        }
        return convertible;
    }

    /**
     * 値がbooleanに変換可能かを返す。
     *
     * @param value 値
     * @return 変換可能な場合は{@code true}
     */
    private boolean isConvertible(final Object value) {
        if (value == null && allowNullValue) {
            return true;
        } else if (value instanceof Boolean) {
            return true;
        } else if (value instanceof String) {
            return isBooleanString((String) value);
        } else if (value instanceof String[]) {
            final String[] values = (String[]) value;
            if (values.length != 1) {
                return false;
            } else {
                final String str = values[0];
                if (str == null) {
                    return allowNullValue;
                } else {
                    return isBooleanString(str);
                }
            }
        }
        return false;
    }

    /**
     * 真偽値の文字列表記(大文字小文字は区別しない)にマッチするか否かを返す。
     * @param value 値
     * @return 値が真偽値の文字列表記の場合は{@code true}
     */
    private boolean isBooleanString(final String value) {
        return value.matches("(?i)true|false");
    }
}
