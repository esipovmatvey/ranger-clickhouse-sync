package rangerclickhousesync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

/**
 * DTO для события аудита, отправляемого в Quickwit.
 * <p>
 * Содержит все поля, предусмотренные схемой индекса Quickwit.
 * Поля, значения которых не могут быть заполнены в текущей версии,
 * помечены как {@code null} и исключаются из JSON-сериализации.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RangerAuditEventDto(
        String id,
        String access,
        String action,
        String agent,
        String agentHost,
        String cliIP,
        String cluster,
        String enforcer,
        String evtTime,
        String logType,
        Long policy,
        String reason,
        String repo,
        Integer repoType,
        String reqData,
        String reqUser,
        String resType,
        String resource,
        Integer result,
        Long seqNum,
        String sess,
        Long eventCount,
        BigInteger eventDurMs,
        String tags,
        String zoneName,
        Long policyVersion
) {
    /**
     * Создаёт событие аудита из строки таблицы {@code system.query_log}.
     *
     * @param row       строка из системной таблицы ClickHouse
     * @param repoName  имя экземпляра сервиса Ranger
     * @param repoType  числовой код типа репозитория
     * @return готовое событие {@code RangerAuditEventDto}
     */
    public static RangerAuditEventDto fromClickHouseRecord(
            final Map<String, Object> row,
            final String repoName,
            final int repoType
    ) {
        final String queryKind = getString(row, "query_kind");
        final String address = getString(row, "address");
        final String cliIP = address != null ? address.replaceFirst("^/", "") : null;
        final Timestamp startTime = (Timestamp) row.get("query_start_time");
        final String evtTime = startTime.toInstant().toString();
        final String exception = getString(row, "exception");
        final String query = getString(row, "query");
        final String user = getString(row, "user");
        final String queryId = getString(row, "query_id");
        final BigInteger durationMs = (BigInteger) row.get("query_duration_ms");
        final String currentDatabase = getString(row, "current_database");
        final int result = (exception == null || exception.isEmpty()) ? 1 : 0;

        return new RangerAuditEventDto(
                UUID.randomUUID().toString(),
                queryKind,
                "QUERY",
                "clickhouse",
                "clickhouse-server",
                cliIP,
                null,
                "ranger-acl",
                evtTime,
                "ClickHouse audit",
                0L,
                exception,
                repoName,
                repoType,
                query,
                user,
                null,
                currentDatabase,
                result,
                null,
                queryId,
                null,
                durationMs,
                null,
                null,
                null
        );
    }

    private static String getString(final Map<String, Object> row, final String key) {
        final Object value = row.get(key);
        return value != null ? value.toString() : null;
    }
}