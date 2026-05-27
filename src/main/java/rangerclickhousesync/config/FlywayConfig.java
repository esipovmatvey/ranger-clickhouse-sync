package rangerclickhousesync.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Конфигурация Flyway для управления миграциями схемы синхронизатора.
 * <p>
 * Определяет бин {@link Flyway}, который при старте приложения автоматически
 * применяет SQL-миграции из classpath:db/migration к схеме {@code sync} в PostgreSQL.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(@Qualifier("postgresDataSource") DataSource dataSource) {
        final Flyway flyway = Flyway.configure()
                                    .dataSource(dataSource)
                                    .schemas("sync")
                                    .locations("classpath:db/migration")
                                    .load();
        flyway.migrate();
        return flyway;
    }
}
