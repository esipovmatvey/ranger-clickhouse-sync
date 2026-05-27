package rangerclickhousesync.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Тип политики Apache Ranger.
 * <p>
 * Соответствует значениям поля {@code policyType} в ответе REST API Ranger.
 * Используется для выбора стратегии обработки политики в {@code PolicyTransformer}.
 */
@Getter
@RequiredArgsConstructor
public enum PolicyType {
    ACCESS(0),
    MASKING(1),
    ROW_LEVEL(2);
    final int policyType;

    /**
     * Возвращает тип политики по целочисленному коду.
     *
     * @param type код типа политики из Ranger API
     * @return соответствующий элемент {@code PolicyType}
     * @throws IllegalArgumentException если код не распознан
     */
    public static PolicyType getPolicyType(final int type) {
        return Arrays.stream(values())
                     .filter(policyType -> policyType.policyType == type)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown policy type: " + type));
    }
}
