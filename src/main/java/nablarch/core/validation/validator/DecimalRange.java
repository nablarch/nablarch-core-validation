package nablarch.core.validation.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.Validation;

/**
 * 数値型のプロパティが指定した数値の範囲内であるかをチェックするアノテーション。
 * <p>
 * ※整数のみの値のバリデーションには、{@link NumberRange}を使用すること。
 *
 * @author siosio
 * @see DecimalRangeValidator
 */
@Validation
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Published
public @interface DecimalRange {

    /** 許容する最小値。 */
    String min() default "";

    /** 許容する最大値。 */
    String max() default "";

    /**
     * バリデーションエラー時に使用する
     */
    String messageId() default "";
}
