package io.github.esipovmatvey.rangerclickhousesync.engine;

import io.github.esipovmatvey.rangerclickhousesync.dto.*;
import io.github.esipovmatvey.rangerclickhousesync.enums.PolicyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Преобразует политики, полученные из Ranger Admin, в последовательность нативных SQL-инструкций
 * для управления доступом в ClickHouse. Поддерживает три типа политик:
 * <ul>
 *   <li><b>Access</b> – создание ролей и выдача привилегий пользователям;</li>
 *   <li><b>Masking</b> – создание ролей и представлений с маскированными колонками;</li>
 *   <li><b>Row Level Filter</b> – создание политик строковой фильтрации ({@code CREATE ROW POLICY}).</li>
 * </ul>
 * Все создаваемые объекты используют префиксы:
 * <ul>
 *   <li>{@value #ROLE_PREFIX} – для ролей;</li>
 *   <li>{@value #ROW_FILTER_PREFIX} – для row policy;</li>
 *   <li>{@value #MASK_FILTER_PREFIX} – для маскирующих представлений (view).</li>
 * </ul>
 */
@Component
@Slf4j
public class PolicyTransformer {
    public static final String ROLE_PREFIX = "ranger_role_";
    public static final String ROW_FILTER_PREFIX = "ranger_row_policy_";
    public static final String MASK_FILTER_PREFIX = "ranger_mask_";

    /**
     * Преобразует список активных политик Ranger в список SQL-команд.
     * <p>
     * Обрабатываются только включённые политики. Для каждого типа политики генерируются
     * соответствующие команды:
     * <ul>
     *   <li><b>ACCESS</b> – создание роли, выдача прав на ресурс, назначение роли пользователям.</li>
     *   <li><b>MASKING</b> – создание роли и представления, замена оригинальной колонки на
     *       маскированное выражение, выдача прав на представление, назначение роли.</li>
     *   <li><b>ROW_LEVEL</b> – создание {@code CREATE ROW POLICY} с фильтрующим выражением.</li>
     * </ul>
     *
     * @param policies список политик из Ranger
     * @return список SQL-команд для выполнения в ClickHouse
     */
    public List<String> transform(final List<RangerPolicyDto> policies) {
        final List<String> sqlStatements = new ArrayList<>();
        policies.stream().filter(RangerPolicyDto::isEnabled).forEach(policy -> {
            final String database = extractFirstValue(policy.resources(), "database");
            final String table = extractFirstValue(policy.resources(), "table");
            final PolicyType policyType = PolicyType.getPolicyType(policy.policyType());
            switch (policyType) {
                case ACCESS -> policy.policyItems().forEach(policyItem -> {
                    policyItem.accesses().stream().filter(AccessItemDto::isAllowed).forEach(accessItem -> {
                        final String roleName = ROLE_PREFIX + policy.id() + "_" + accessItem.type();
                        sqlStatements.add("CREATE ROLE IF NOT EXISTS " + roleName);
                        sqlStatements.add(buildGrantStatement(accessItem.type(), database, table, roleName));
                        policyItem.users().forEach(user -> sqlStatements.add("GRANT " + roleName + " TO " + user));
                    });
                });
                case MASKING -> policy.dataMaskPolicyItems().forEach(maskPolicyItem -> {
                    final DataMaskInfoDto maskInfo = maskPolicyItem.dataMaskInfo();
                    final String column = extractFirstValue(policy.resources(), "column");
                    assert column != null;
                    final String expr = maskInfo.valueExpr().replace("{col}", column);
                    final String roleName = ROLE_PREFIX + policy.id() + "_select";
                    final String viewName = MASK_FILTER_PREFIX + policy.id();
                    sqlStatements.add("CREATE ROLE IF NOT EXISTS " + roleName);
                    sqlStatements.add(
                            String.format(
                                    "CREATE VIEW %s.%s AS SELECT * EXCEPT %s, %s AS %s FROM %s.%s",
                                    database,
                                    viewName,
                                    column,
                                    expr,
                                    column,
                                    database,
                                    table
                            )
                    );
                    sqlStatements.add(buildGrantStatement("select", database, viewName, roleName));
                    maskPolicyItem.users().forEach(user -> sqlStatements.add("GRANT " + roleName + " TO " + user));
                });
                case ROW_LEVEL -> policy.rowFilterPolicyItems().forEach(policyItem -> {
                    final RowFilterInfoDto rowFilterInfo = policyItem.rowFilterInfo();
                    if (rowFilterInfo.filterExpr().isBlank()) {
                        log.warn("Row filter policy {} has empty expression, skipping", policy.id());
                        return;
                    }
                    final String policyName = ROW_FILTER_PREFIX + policy.id();
                    final String users = String.join(", ", policyItem.users());
                    sqlStatements.add(
                            String.format(
                                    "CREATE ROW POLICY IF NOT EXISTS %s ON %s.%s FOR SELECT USING %s TO %s",
                                    policyName,
                                    database,
                                    table,
                                    rowFilterInfo.filterExpr(),
                                    users
                            )
                    );
                });
            }
        });

        return sqlStatements;
    }

    /**
     * Формирует GRANT-выражение с учётом wildcard-политик.
     * <ul>
     *   <li>{@code database=*} → {@code GRANT ... ON *.*}</li>
     *   <li>{@code table=*} или {@code table=null} → {@code GRANT ... ON database.*}</li>
     *   <li>Конкретная таблица → {@code GRANT ... ON database.table}</li>
     * </ul>
     *
     * @param accessType тип доступа (SELECT, INSERT, …)
     * @param database   имя базы данных
     * @param table      имя таблицы или {@code null}, если доступ на всю БД
     * @param roleName   имя роли
     * @return готовое SQL-выражение
     */
    private String buildGrantStatement(
            final String accessType,
            final String database,
            final String table,
            final String roleName
    ) {
        String resource;
        if ("*".equals(database)) {
            resource = "*.*";
        } else if (table == null || "*".equals(table)) {
            resource = database + ".*";
        } else {
            resource = database + "." + table;
        }
        return "GRANT " + accessType.toUpperCase() + " ON " + resource + " TO " + roleName;
    }

    private String extractFirstValue(final Map<String, ResourceDto> resources, final String key) {
        final ResourceDto spec = resources.get(key);
        if (spec != null && spec.values() != null && !spec.values().isEmpty()) {
            return spec.values().getFirst();
        }
        return null;
    }
}
