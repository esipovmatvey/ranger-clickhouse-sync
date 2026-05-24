package rangerclickhousesync.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Репозиторий для хранения и обновления метки времени последней синхронизации аудита.
 * <p>
 * Использует таблицу {@code sync.audit_state} в PostgreSQL для предотвращения дублирования
 * событий аудита при повторных циклах синхронизации.
 */
@Repository
public class AuditStateRepository {
    private final JdbcTemplate jdbcTemplate;

    public AuditStateRepository(@Qualifier("postgresJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Возвращает метку времени последней успешной синхронизации аудита.
     *
     * @return время последней синхронизации
     */
    public LocalDateTime getLastSyncTime() {
        return jdbcTemplate.queryForObject(
                "SELECT last_sync_time FROM sync.audit_state WHERE id = 1",
                LocalDateTime.class
        );
    }

    /**
     * Обновляет метку времени последней синхронизации аудита.
     *
     * @param time новое значение времени
     */
    public void updateLastSyncTime(final LocalDateTime time) {
        jdbcTemplate.update("UPDATE sync.audit_state SET last_sync_time = ? WHERE id = 1", time);
    }
}
