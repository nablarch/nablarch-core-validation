package nablarch.core.validation.validator;

import java.lang.annotation.Annotation;
import java.util.Map;

import nablarch.core.util.StringUtil;
import nablarch.core.validation.DirectCallableValidator;
import nablarch.core.validation.ValidationContext;
import nablarch.core.validation.ValidationResultMessageUtil;


/**
 *   数値の範囲をチェックするクラス。
 *   <p>
 *   入力された値が、{@link NumberRange}アノテーションのプロパティに設定された値の範囲内であるかチェックする。
 *   </p>
 *   <p>
 *     <b>使用するための設定</b>
 *   </p>
 *     本バリデータを使用するためにはデフォルトのエラーメッセージIDを指定する必要がある。
 *   <pre>
 *      {@code <component name="numberRangeValidator" class="nablarch.core.validation.validator.NumberRangeValidator">
 *          <property name="maxMessageId" value="MSGXXXXXX"/>
 *          <property name="maxAndMinMessageId" value="MSGXXXXXX"/>
 *          <property name="minMessageId" value="MSGXXXXXX"/>
 *      </component>
 *      }
 *   </pre>
 *
 *   <p>
 *     <b>プロパティの設定</b>
 *   </p>
 *   範囲チェックをしたいプロパティのセッタに{@link NumberRange}アノテーションを次のように設定する。
 *   <pre>
 *   入力値が1以上10以下の範囲内であるかチェックする
 *   {@code @PropertyName("売上高")}
 *   {@code @NumberRange(min = 1, max = 10)}
 *   {@code public void setSales(Integer sales){
 *      this.sales = sales;
 *   }}
 *
 *   入力値が-1.5以上1.5以下の範囲内であるかチェックする
 *   {@code @PropertyName("売上高")}
 *   {@code @NumberRange(min = -1.5, max = 1.5)}
 *   {@code public void setSales(Integer sales){
 *      this.sales = sales;
 *   }}
 *
 *   入力値が0以上であるかチェックする
 *   {@code @PropertyName("売上高")}
 *   {@code @NumberRange(min = 0)}
 *   {@code public void setSales(Integer sales){
 *      this.sales = sales;
 *   }}
 *   </pre>
 *
 * @author Koichi Asano
 *
 */
public class NumberRangeValidator implements DirectCallableValidator {
    /**
     * バリデーションの条件に最大値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID
     */
    private String maxMessageId;

    /**
     * バリデーションの条件に最大値と最小値が指定されていた場合のデフォルトのエラーメッセージのメッセージID。
     */
    private String maxAndMinMessageId;

    /**
     * バリデーションの条件に最小値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID。
     */
    private String minMessageId;

    /**
     * バリデーションの条件に最大値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID。<br/>
     * 例 : "{0}は{2}以下で入力してください。"
     * 
     * @param maxMessageId バリデーションの条件に最大値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID。
     */
    public void setMaxMessageId(final String maxMessageId) {
        this.maxMessageId = maxMessageId;
    }
    
    /**
     * バリデーションの条件に最大値と最小値が指定されていた場合のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * 例 : "{0}は{1}以上{2}以下で入力してください。"
     * @param maxAndMinMessageId バリデーションの条件に最大値と最小値が指定されていた場合のデフォルトのエラーメッセージのメッセージID
     */
    public void setMaxAndMinMessageId(final String maxAndMinMessageId) {
        this.maxAndMinMessageId = maxAndMinMessageId;
    }
    /**
     * バリデーションの条件に最小値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージIDを設定する。<br/>
     * 例 : "{0}は{1}以上で入力してください。"
     * @param minMessageId バリデーションの条件に最小値のみが指定されていた場合のデフォルトのエラーメッセージのメッセージID
     */
    public void setMinMessageId(final String minMessageId) {
        this.minMessageId = minMessageId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return NumberRange.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean validate(final ValidationContext<T> context, final String propertyName,
            final Object propertyDisplayName, final Annotation annotation, final Object value) {
        
        if (value == null) {
            return true;
        }

        final Number num = (Number) value;
        
        final NumberRange numberRange = (NumberRange) annotation;
        final Range range = new Range(
                numberRange.min() != Long.MIN_VALUE ? numberRange.min() : null,
                numberRange.max() != Long.MAX_VALUE ? numberRange.max() : null);

        if (!range.includes(num)) {
            addMessage(context, propertyName, propertyDisplayName, numberRange);
            return false;
        }
        return true;
    }

    /**
     * エラーメッセージを設定する。
     * 
     * @param <T> バリデーション結果で取得できる型
     * @param context ValidationContext
     * @param propertyName プロパティ名
     * @param propertyDisplayName プロパティの表示名オブジェクト
     * @param range NumberRangeアノテーション
     */
    private <T> void addMessage(final ValidationContext<T> context,
            final String propertyName, final Object propertyDisplayName, final NumberRange range) {
        final String messageId;
        if (StringUtil.hasValue(range.messageId())) {
            messageId = range.messageId();
        } else {
            if (range.min() > Long.MIN_VALUE && range.max() < Long.MAX_VALUE) {
                messageId = maxAndMinMessageId;
            } else if (range.min() > Long.MIN_VALUE) {
                messageId = minMessageId;
            } else {
                messageId = maxMessageId;
            }
        }
        ValidationResultMessageUtil.addResultMessage(context, propertyName, messageId, propertyDisplayName, range.min(), range.max());
    }

    /**{@inheritDoc}*/
    @Override
    public <T> boolean validate(final ValidationContext<T>       context,
                                final String                     propertyName,
                                final Object                     propertyDisplayName,
                                final Map<String, Object>  params,
                                final Object                     value) {

        final NumberRange annotation = new NumberRange() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return NumberRange.class;
            }

            @Override
            public long min() {
                final Long min = (Long) params.get("min");
                return (min == null) ? Long.MIN_VALUE
                                     : min;
            }

            @Override
            public long max() {
                final Long max = (Long) params.get("max");
                return (max == null) ? Long.MAX_VALUE
                                     : max;
            }

            @Override
            public String messageId() {
                final String messageId = (String) params.get("messageId");
                return StringUtil.isNullOrEmpty(messageId) ? ""
                                                           : messageId;
            }
        };

        return validate(context, propertyName, propertyDisplayName, annotation, value);
    }
}
