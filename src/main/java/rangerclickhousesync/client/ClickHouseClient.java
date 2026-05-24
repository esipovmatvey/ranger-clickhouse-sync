package rangerclickhousesync.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import rangerclickhousesync.enums.RolePrefix;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Клиент для выполнения SQL-запросов и вспомогательных операций в ClickHouse.
 * <p>
 * Использует {@link JdbcTemplate}, настроенный на ClickHouse, для управления ролями,
 * политиками доступа на уровне строк, представлениями маскирования, а также для чтения
 * записей аудита из {@code system.query_log}.
 */
@Component
@Slf4j
public class ClickHouseClient {
    private final JdbcTemplate jdbcTemplate;

    public ClickHouseClient(@Qualifier("clickHouseJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Выполняет произвольный SQL-запрос без возврата результата.
     *
     * @param sql SQL-выражение для выполнения
     */
    public void executeQuery(final String sql) {
        jdbcTemplate.execute(sql);
        log.debug("Executed: {}", sql);
    }

    /**
     * Удаляет все роли, связанные с заданной политикой Ranger.
     * Роли идентифицируются по префиксу {@code ROLE}.
     *
     * @param id идентификатор политики
     */
    public void dropRolesByPolicyId(final Long id) {
        final String rolePrefix = RolePrefix.ROLE.getPrefix() + id + "_%";
        final List<String> roles = jdbcTemplate.queryForList(
                "SELECT name FROM system.roles WHERE name LIKE ?",
                String.class,
                rolePrefix
        );
        roles.forEach(role -> executeQuery("DROP ROLE IF EXISTS " + role));
    }

    /**
     * Удаляет политику доступа на уровне строк (Row Policy), связанную с заданной политикой Ranger.
     * Идентификация происходит по короткому имени {@code ROW_POLICY_<id>}.
     *
     * @param id идентификатор политики
     */
    public void dropRowPoliciesByPolicyId(final Long id) {
        final String rowPolicyName = RolePrefix.ROW_POLICY.getPrefix() + id;
        final List<String> dropSqls = jdbcTemplate.query(
                "SELECT name FROM system.row_policies WHERE short_name = ?",
                (rs, _) -> String.format("DROP ROW POLICY IF EXISTS %s", rs.getString("name")),
                rowPolicyName
        );
        dropSqls.forEach(this::executeQuery);
    }

    /**
     * Удаляет представления маскирования данных, связанные с заданной политикой Ranger.
     * Представления имеют имена {@code MASK_VIEW_<id>}.
     *
     * @param id идентификатор политики
     */
    public void dropMaskViewsByPolicyId(final Long id) {
        final String viewPrefix = RolePrefix.MASK_VIEW.getPrefix() + id;
        final List<String> dropSqls = jdbcTemplate.query(
                "SELECT name FROM system.tables WHERE name = ?",
                (rs, _) -> String.format("DROP VIEW IF EXISTS " + rs.getString("name")),
                viewPrefix
        );
        dropSqls.forEach(this::executeQuery);
    }

    /**
     * Удаляет квоты, связанные с заданным идентификатором.
     * Квоты имеют имена, начинающиеся с префикса {@code RolePrefix.QUOTA.getPrefix() + id}.
     * Выполняет поиск в системной таблице {@code system.quotas} и удаляет найденные квоты командой
     * {@code DROP QUOTA IF EXISTS}.
     *
     * @param id идентификатор, используемый для формирования префикса имени квоты
     */
    public void dropQuota(final Long id) {
        final String quotaPrefix = RolePrefix.QUOTA.getPrefix() + id;
        final List<String> quotas = jdbcTemplate.queryForList(
                "SELECT name FROM system.quotas WHERE name LIKE ?",
                String.class,
                quotaPrefix + "%"
        );
        quotas.forEach(quota -> executeQuery("DROP QUOTA IF EXISTS " + quota));
    }

    /**
     * Извлекает записи аудита из {@code system.query_log} начиная с заданного момента времени.
     * Возвращает список мап (колонка → значение).
     *
     * @param since момент времени, после которого запрашиваются записи
     * @return список записей, где каждая запись представлена {@code Map<String, Object>}
     */
    public List<Map<String, Object>> fetchAuditRecords(final LocalDateTime since) {
        final String sql = """
                SELECT
                    user,
                    query_kind,
                    current_database,
                    address,
                    query_id,
                    query,
                    exception,
                    query_start_time,
                    query_duration_ms
                FROM system.query_log
                WHERE event_time > ?
                ORDER BY event_time
                """;
        return jdbcTemplate.queryForList(sql, Timestamp.valueOf(since));
    }
}
