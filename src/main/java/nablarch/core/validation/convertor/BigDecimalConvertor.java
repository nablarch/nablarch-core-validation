package nablarch.core.validation.convertor;


import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.regex.Pattern;

import nablarch.core.util.annotation.Published;

/**
 * 値をBigDecimalに変換するクラス。<br/>
 * 
 * 本クラスで変換するプロパティには、必ずDigitsアノテーションを付与しておく必要がある。
 * 本クラスでは、Digitsアノテーションの属性を下記の通り使用する。</br>
 * <table border=1>
 *     <tr bgcolor="#cccccc">
 *         <td>Digitsアノテーションの属性名</td><td>説明</td>
 *     </tr>
 *     <tr>
 *         <td>integer</td><td>整数部桁数上限値。指定値に上限はない。</td>
 *     </tr>
 *     <tr>
 *         <td>fraction</td><td>小数部桁数上限値。指定値に上限はない。</td>
 *     </tr>
 *     <tr>
 *         <td>comma</td><td>trueの場合、入力値が3桁区切り文字で編集されていてもよい。（区切り文字は省略可。）
 *         　　　　　　　　　　　　　</br>falseの場合、入力値が3桁区切り文字で編集されていてはいけない。</td>
 *     </tr>
 *     <tr>
 *         <td>messageId</td><td>変換失敗時のメッセージID。</td>
 *     </tr>
 * </table>
 *
 * <p>
 * <b>バリデーション仕様</b>
 * <p>
 * Digitsアノテーションの設定によって行われるバリデーション仕様の例を以下に示す。</br>
 * 桁数指定の設定とバリデーション内容：</br>
 * integer=4, fraction=4の場合、「1234.1234」、「-1234.1234」、「+1234.1234」、「.1234」等は有効である。<br/>
 * しかし、「12345.」、「.12345」、「12345.123」、「123.12345」、「1234.」等は無効であり、バリデーションエラーとなる。</br>
 * カンマ編集可否の設定とバリデーション内容：</br>
 * commaSeparatedがtrueの場合、「1,234.1234」、「1234.1234」は有効である。<br/>
 * 「12,34.1234」のようにカンマを3桁の区切り文字として使用していない場合は無効でありバリデーションエラーとなる。</br>
 * commaSeparatedがfalseの場合、「1,234.1234」等は無効であり、バリデーションエラーとなる。
 * </br>
 * <p>
 * 上記例のように、本クラスのバリデーションでは {@link NumberConvertorSupport} が行う共通バリデーションに加えて、下記仕様のバリデーションを行う。
 * <ul>
 *     <li>先頭が数字、プラス記号( + )、マイナス記号( - )、ピリオド( . )のいずれかで始まっていること。</li>
 *     <li>整数部桁数が、Digitsアノテーションにて指定されたinteger値以下であること。</li>
 *     <li>Digitsアノテーションでfractionが指定されている場合、小数部の桁数がfraction値以下であること。</li>
 *     <li>DigitsアノテーションのcommaSeparatedがfalseの場合、カンマが使用されていないこと。</li>
 *     <li>DigitsアノテーションのcommaSeparatedがtrueの場合、カンマが3桁区切りに付与されている、またはカンマを設定していないこと。</li>
 *     <li>末尾がピリオドで終わらないこと。</li>
 * </ul>
 * 
 * <p>
 * <b>国際化</b>
 * <p>
 * 数値の記述は、フランス語圏等、言語によっては数値を記載する際にカンマとピリオドの解釈を置き換えて使用する言語がある。</br>
 * Nablarchのカスタムタグで国際化機能を使用した場合、本クラスもカンマとピリオドの解釈を入れ替えて入力値を取り扱う。
 * 以下に @Digits(integer=6, fraction=2, commaSeparated=true) を設定していた場合の例を示す。</br>
 * 1) 言語に "ja" (日本語) が設定された場合。</br>
 * 　123,456.11 ⇒ 123456.11 として解釈する</br>
 * 　123.456,11 ⇒ バリデーションエラーとなる</br>
 * 2) 言語に "fr" (フランス語) が設定された場合。</br>
 * 　123,456.11 ⇒ バリデーションエラーとなる</br>
 * 　123.456,11 ⇒ 123456.11 として解釈する</br>
 * </p>
 * 
 * @author Koichi Asano
 * @see Digits
 * @see NumberConvertorSupport
 */
@Published(tag = "architect")
public class BigDecimalConvertor extends NumberConvertorSupport {

    /**
     * {@inheritDoc}
     */
    public Class<?> getTargetClass() {
        return BigDecimal.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Number convertToPropertyType(String numberString) {
        return new BigDecimal(numberString);
    }
    
    @Override
    protected Pattern createPattern(Digits digits, DecimalFormatSymbols symbols) {
        
        // @Digits(integer=5, fraction=3)の場合、
        // ^[\+-]?[0-9]{0,2},?[0-9]{0,3}(\.[0-9]{3})?$ のような正規表現文字列を作成する

        int integer = digits.integer();
        int fraction = digits.fraction();
        boolean commaAllowed = digits.commaSeparated();
        boolean isFirstNum = true;

        StringBuilder sb = new StringBuilder();
        
        sb.append("^");
        sb.append("[\\+-]?");
        if (integer % 3 != 0) {
            sb.append("[0-9]{0," + (integer % 3) + "}");
            isFirstNum = false;
        }

        for (int i = 0; i < integer / 3; i++) {

            if (commaAllowed && !isFirstNum) {
                sb.append(ConversionUtil.escapeForRegex(symbols.getGroupingSeparator()) + "?");
            }
            sb.append("[0-9]{0,3}");
            isFirstNum = false;
        }

        if (fraction != 0) {
            sb.append("(" + ConversionUtil.escapeForRegex(symbols.getDecimalSeparator())
                            + "[0-9]{1," + fraction + "})?");
        }
        sb.append("$");
        return Pattern.compile(sb.toString());
    }
}
