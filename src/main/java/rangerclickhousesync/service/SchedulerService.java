package rangerclickhousesync.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Сервис-планировщик для периодического запуска синхронизации политик и аудита.
 * <p>
 * Содержит две независимые задачи, выполняемые асинхронно с заданными интервалами:
 * <ul>
 *   <li>{@code syncPolicyPeriodically} – синхронизация политик Ranger → ClickHouse;</li>
 *   <li>{@code syncAuditPeriodically} – выгрузка событий аудита из ClickHouse в Quickwit.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SchedulerService {
    private final PolicySyncService policySyncService;
    private final AuditSyncService auditSyncService;

    @Scheduled(fixedDelayString = "${ranger.sync.interval-ms}")
    @Async
    public void syncPolicyPeriodically() {
        policySyncService.synchronize();
    }

    @Scheduled(fixedDelayString = "${ranger.audit.sync.interval-ms}")
    @Async
    public void syncAuditPeriodically() {
        auditSyncService.synchronize();
    }
}
