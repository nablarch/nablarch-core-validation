package nablarch.core.validation.validator;

import java.math.BigDecimal;

import nablarch.core.util.StringUtil;

/**
 * 範囲を表すクラス。
 *
 * @author siosio
 */
class Range {

    /** 範囲の最小値 */
    private final BigDecimal min;

    /** 範囲の最大値 */
    private final BigDecimal max;

    /**
     * 最小値と最大値からRangeを生成する。
     *
     * @param min 最小値(未指定の場合は空文字列またはnull)
     * @param max 最大値(未指定の場合は空文字列またはnull)
     */
    Range(final String min, final String max) {
        this.min = toBigDecimal(min);
        this.max = toBigDecimal(max);
    }

    /**
     * 最小値と最大値からRangeを生成する。
     *
     * @param min 最小値(未指定の場合はnull)
     * @param max 最大値(未指定の場合はnull)
     */
    Range(final Long min, final Long max) {
        this.min = min != null ? BigDecimal.valueOf(min) : null;
        this.max = max != null ? BigDecimal.valueOf(max) : null; 
    }

    /**
     * 値をBigDecimalに変換する。
     * <p>
     * 変換対象の値が、nullか空文字列の場合はnullを返す。
     * これ以外の値で、BigDecimalに変換できない場合は、{@link NumberFormatException}を送出する。
     *
     * @param value 変換対象の値
     * @return 変換後の値
     */
    private static BigDecimal toBigDecimal(final String value) {
        if (StringUtil.hasValue(value)) {
            return new BigDecimal(value);
        } else {
            return null;
        }
    }

    /**
     * 指定の値がRangeの範囲内にあるかどうか。
     *
     * @param value 値
     * @return Rangeの範囲内の場合は {@code true}
     */
    boolean in(final BigDecimal value) {
        return isMoreThenOrEqualToMin(value) && isLessThenOrEqualToMax(value) ;
    }

    /**
     * 指定の値が最大値以下かどうか。
     * @param value 値
     * @return 最大値以下の場合true
     */
    private boolean isMoreThenOrEqualToMin(final BigDecimal value) {
        return min == null || min.compareTo(value) <= 0;
    }

    /**
     * 指定の値が最大値以下かどうか。
     * @param value 値
     * @return 最大値以下の場合true
     */
    private boolean isLessThenOrEqualToMax(final BigDecimal value) {
        return max == null || max.compareTo(value) >= 0;
    }

    /**
     * Rangeの最小値の文字列表現を返す。
     *
     * @return 最小値(未指定の場合はnull)
     */
    String getMinString() {
        return min != null ? min.toPlainString() : null;
    }

    /**
     * Rangeの最大値の文字列表現を返す。
     *
     * @return 最大値(未指定の場合はnull)
     */
    String getMaxString() {
        return max != null ? max.toPlainString() : null;
    }
}
