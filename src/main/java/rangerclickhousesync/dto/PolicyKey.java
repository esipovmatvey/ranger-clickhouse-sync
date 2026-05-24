package rangerclickhousesync.dto;

import rangerclickhousesync.engine.builder.policy.*;
import rangerclickhousesync.enums.PolicyType;
import rangerclickhousesync.enums.ResourceType;

/**
 * Ключ для выбора стратегии построения SQL по типу политики и ресурса.
 * <p>
 * Используется в {@code PolicyTransformer} для сопоставления билдера и комбинации
 * {@link PolicyType} + {@link ResourceType}. Фабричный метод {@code from} создаёт
 * ключ по классу билдера.
 */
public record PolicyKey(PolicyType policyType, ResourceType resourceType) {
    public static PolicyKey from(final Class<? extends PolicySqlBuilder> builderClass) {
        return switch (builderClass) {
            case Class<?> c when c == AccessPolicySqlBuilder.class -> new PolicyKey(PolicyType.ACCESS, null);
            case Class<?> c when c == MaskingPolicySqlBuilder.class -> new PolicyKey(PolicyType.MASKING, null);
            case Class<?> c when c == RowPolicySqlBuilder.class -> new PolicyKey(PolicyType.ROW_LEVEL, null);
            case Class<?> c when c == QuotaPolicySqlBuilder.class -> new PolicyKey(PolicyType.ACCESS, ResourceType.QUOTA);
            default -> throw new IllegalArgumentException("Unknown builder: " + builderClass.getSimpleName());
        };
    }
}
