package rangerclickhousesync.repository;

import rangerclickhousesync.dto.SyncStateDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Репозиторий для хранения состояния синхронизированных политик Ranger.
 * <p>
 * Оперирует таблицей {@code sync.ranger_sync_state} в PostgreSQL, в которой для каждой
 * политики Ranger сохраняется SHA-256 хеш её значимых полей. Это позволяет при последующих
 * циклах синхронизации определять, изменилась ли политика, и применять только необходимые
 * изменения, а не пересоздавать все роли заново.
 */
@Repository
public class RangerSyncStateRepository {
    private final JdbcTemplate jdbcTemplate;

    public RangerSyncStateRepository(@Qualifier("postgresJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Сохраняет или обновляет состояние политики.
     * Если запись с таким {@code policyId} уже существует, хеш обновляется;
     * в противном случае создаётся новая запись.
     *
     * @param dto объект с идентификатором политики и её текущим хешем
     */
    public void save(final SyncStateDto dto) {
        jdbcTemplate.update("""
                                    INSERT INTO sync.ranger_sync_state (policy_id, content_hash)
                                    VALUES (?, ?)
                                    ON CONFLICT (policy_id) DO UPDATE SET content_hash = EXCLUDED.content_hash
                                    """,
                            dto.policyId(),
                            dto.contentHash()
        );
    }

    /**
     * Находит запись о состоянии политики по её идентификатору.
     *
     * @param policyId идентификатор политики в Ranger
     * @return {@code Optional<SyncStateDto>} с хешем политики, либо пустой, если политика не найдена
     */
    public Optional<SyncStateDto> findByPolicyId(final Long policyId) {
        return jdbcTemplate.query(
                "SELECT * FROM sync.ranger_sync_state WHERE policy_id = ?",
                (rs, _) -> new SyncStateDto(rs.getLong("policy_id"), rs.getString("content_hash")),
                policyId
        ).stream().findFirst();
    }

    /**
     * Возвращает множество идентификаторов всех политик, сохранённых в таблице состояния.
     * Используется для обнаружения политик, удалённых из Ranger, но ещё присутствующих в БД.
     *
     * @return множество {@code policyId}
     */
    public Set<Long> findAllPolicyIds() {
        return new HashSet<>(jdbcTemplate.queryForList("SELECT policy_id FROM sync.ranger_sync_state", Long.class));
    }

    /**
     * Удаляет запись о состоянии политики из таблицы.
     *
     * @param policyId идентификатор политики
     */
    public void deleteById(final Long policyId) {
        jdbcTemplate.update("DELETE FROM sync.ranger_sync_state WHERE policy_id = ?", policyId);
    }
}
