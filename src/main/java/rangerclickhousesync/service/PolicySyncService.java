package rangerclickhousesync.service;

import rangerclickhousesync.client.ClickHouseClient;
import rangerclickhousesync.client.RangerClient;
import rangerclickhousesync.config.properties.RangerProperties;
import rangerclickhousesync.dto.RangerPolicyDto;
import rangerclickhousesync.dto.SyncStateDto;
import rangerclickhousesync.engine.PolicyTransformer;
import rangerclickhousesync.repository.RangerSyncStateRepository;
import rangerclickhousesync.utils.HashUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис синхронизации политик безопасности Ranger → ClickHouse.
 * <p>
 * Выполняет полный цикл актуализации политик: получает текущие политики из Ranger Admin,
 * сравнивает их с сохранённым состоянием в PostgreSQL и применяет только изменившиеся.
 * Поддерживает все типы политик:
 * <ul>
 *   <li><b>Access</b> – управление доступом через роли и гранты;</li>
 *   <li><b>Row Level Filter</b> – строковая фильтрация через {@code CREATE ROW POLICY};</li>
 *   <li><b>Masking</b> – маскирование данных через представления.</li>
 * </ul>
 * <p>
 * Для каждой политики вычисляется SHA-256 хеш значимых полей, который сравнивается с
 * сохранённым в таблице {@code sync.ranger_sync_state}. Это позволяет обрабатывать только
 * изменившиеся, новые или удалённые политики, не пересоздавая все роли заново.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicySyncService {
    private final PolicyTransformer policyTransformer;
    private final RangerClient rangerClient;
    private final RangerProperties rangerProperties;
    private final ClickHouseClient clickHouseClient;
    private final RangerSyncStateRepository rangerSyncStateRepository;

    /**
     * Выполняет синхронизацию политик.
     * <p>
     * Алгоритм:
     * <ol>
     *   <li>Получить все политики из Ranger для заданного сервиса.</li>
     *   <li>Удалить из ClickHouse объекты политик, которые отсутствуют в Ranger.</li>
     *   <li>Для каждой политики вычислить хеш и сравнить с сохранённым состоянием.</li>
     *   <li>Применить изменившиеся политики (создать/обновить) или удалить отключённые.</li>
     *   <li>Сохранить новый хеш в БД.</li>
     * </ol>
     */
    @Transactional
    public void synchronize() {
        final List<RangerPolicyDto> policies = rangerClient.getPolicies(rangerProperties.serviceName());
        if (policies == null || policies.isEmpty()) {
            log.warn("No policies found for service: {}", rangerProperties.serviceName());
            return;
        }

        final Set<Long> rangerIds = policies.stream()
                                            .map(RangerPolicyDto::id)
                                            .collect(Collectors.toSet());
        final Set<Long> dbIds = rangerSyncStateRepository.findAllPolicyIds();
        dbIds.forEach(dbId -> {
                          if (!rangerIds.contains(dbId)) {
                              log.info("Policy {} removed from Ranger, cleaning up", dbId);
                              deletePolicies(dbId);
                              rangerSyncStateRepository.deleteById(dbId);
                          }
                      }
        );

        policies.forEach(policy -> {
            try {
                final String hash = HashUtils.computeContentHash(policy);
                final Optional<SyncStateDto> saved = rangerSyncStateRepository.findByPolicyId(policy.id());

                if (saved.isEmpty() || !saved.get().contentHash().equals(hash)) {
                    if (policy.isEnabled()) {
                        applyPolicy(policy);
                    } else {
                        deletePolicies(policy.id());
                    }
                    rangerSyncStateRepository.save(new SyncStateDto(policy.id(), hash));
                }
            } catch (Exception e) {
                log.error("Failed to process policy {}", policy.id(), e);
            }
        });
    }

    private void applyPolicy(final RangerPolicyDto policy) {
        deletePolicies(policy.id());
        final List<String> sqlStatements = policyTransformer.transform(policy);
        log.debug("Applying {} SQL statements for policy {}", sqlStatements.size(), policy.id());
        sqlStatements.forEach(sql -> {
            try {
                clickHouseClient.executeQuery(sql);
            } catch (Exception e) {
                log.error("Failed to execute SQL: {}", sql, e);
            }
        });
    }

    private void deletePolicies(final Long policyId) {
        clickHouseClient.dropRolesByPolicyId(policyId);
        clickHouseClient.dropRowPoliciesByPolicyId(policyId);
        clickHouseClient.dropMaskViewsByPolicyId(policyId);
        clickHouseClient.dropQuota(policyId);
    }
}
