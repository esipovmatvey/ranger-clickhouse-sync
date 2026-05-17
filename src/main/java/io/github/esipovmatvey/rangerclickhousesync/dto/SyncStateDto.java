package io.github.esipovmatvey.rangerclickhousesync.dto;

/**
 * DTO для хранения состояния синхронизированной политики в PostgreSQL.
 * <p>
 * Используется для отслеживания применённых политик: {@code policyId} однозначно идентифицирует
 * политику Ranger, а {@code contentHash} хранит SHA-256 хеш её значимых полей для быстрого
 * определения изменений.
 */
public record SyncStateDto(
        Long policyId,
        String contentHash
) {
}
