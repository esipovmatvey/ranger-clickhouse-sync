package rangerclickhousesync.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Утилиты для работы со строками.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    /**
     * Снимает обрамляющие кавычки (двойные или одинарные) с переданной строки.
     *
     * @param value исходная строка (может быть {@code null})
     * @return строка без внешних кавычек, либо исходное значение, если кавычек нет
     */
    public static String unwrapQuotes(final String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
