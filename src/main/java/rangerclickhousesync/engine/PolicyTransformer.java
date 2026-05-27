package rangerclickhousesync.engine;

import org.springframework.stereotype.Component;
import rangerclickhousesync.dto.PolicyKey;
import rangerclickhousesync.dto.RangerPolicyDto;
import rangerclickhousesync.dto.ResourceContext;
import rangerclickhousesync.engine.builder.policy.PolicySqlBuilder;
import rangerclickhousesync.enums.PolicyType;
import rangerclickhousesync.enums.ResourceType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Преобразует политики Ranger в набор SQL-инструкций ClickHouse.
 * <p>
 * Выбирает нужный {@link PolicySqlBuilder} по комбинации типа политики и типа ресурса.
 * Строится на основе всех доступных билдеров, собранных Spring.
 */
@Component
public class PolicyTransformer {
    private final Map<PolicyKey, PolicySqlBuilder> builderMap;

    public PolicyTransformer(final List<PolicySqlBuilder> builders) {
        this.builderMap = builders.stream()
                                  .collect(Collectors.toMap(b -> PolicyKey.from(b.getClass()), Function.identity()));
    }

    public List<String> transform(final RangerPolicyDto policy) {
        final ResourceType resourceType = ResourceContext.from(policy.resources()).resourceType();
        final PolicyType policyType = PolicyType.getPolicyType(policy.policyType());
        final PolicyKey key = new PolicyKey(policyType, resourceType);
        final PolicySqlBuilder builder = builderMap.getOrDefault(key, builderMap.get(new PolicyKey(policyType, null)));
        return builder.build(policy);
    }
}
