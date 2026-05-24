package rangerclickhousesync.service;

import rangerclickhousesync.client.ClickHouseClient;
import rangerclickhousesync.client.QuickwitClient;
import rangerclickhousesync.config.properties.RangerProperties;
import rangerclickhousesync.dto.RangerAuditEventDto;
import rangerclickhousesync.repository.AuditStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис синхронизации аудита ClickHouse → Quickwit.
 * <p>
 * Периодически забирает новые записи из {@code system.query_log}, преобразует их
 * в объекты {@link RangerAuditEventDto} и отправляет в Quickwit. Для предотвращения
 * дублирования используется метка времени последней синхронизации, хранящаяся в
 * {@link AuditStateRepository}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditSyncService {
    private final ClickHouseClient clickHouseClient;
    private final QuickwitClient quickwitClient;
    private final AuditStateRepository auditStateRepository;
    private final RangerProperties rangerProperties;

    /**
     * Выполняет один цикл синхронизации аудита: извлекает записи из ClickHouse,
     * преобразует их в DTO и отправляет в Quickwit.
     * <p>
     * После успешной отправки обновляет время последней синхронизации.
     */
    public void synchronize() {
        final LocalDateTime lastSync = auditStateRepository.getLastSyncTime();
        final List<Map<String, Object>> records = clickHouseClient.fetchAuditRecords(lastSync);

        if (records.isEmpty()) {
            log.debug("No new audit records");
            return;
        }

        final int repoType = rangerProperties.audit().repoType();
        final List<RangerAuditEventDto> events = records.stream()
                                                        .map(row ->
                                                                     RangerAuditEventDto.fromClickHouseRecord(
                                                                             row,
                                                                             rangerProperties.serviceName(),
                                                                             repoType
                                                                     )
                                                        ).collect(Collectors.toList());

        quickwitClient.ingestEvents(events);
        final Map<String, Object> lastRecord = records.getLast();
        final LocalDateTime lastRecordTime = extractEventTime(lastRecord);
        auditStateRepository.updateLastSyncTime(lastRecordTime);

        log.info("Synced {} audit events. Last event time: {}", events.size(), lastRecordTime);
    }

    private LocalDateTime extractEventTime(final Map<String, Object> record) {
        final Object timeObj = record.get("query_start_time");
        if (timeObj instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        } else if (timeObj instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return LocalDateTime.now();
    }
}
