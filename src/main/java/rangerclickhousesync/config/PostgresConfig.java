package rangerclickhousesync.config;

import rangerclickhousesync.config.properties.PostgresProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Конфигурация подключения к PostgreSQL.
 * <p>
 * Определяет бины {@link DataSource} и {@link JdbcTemplate},
 * используемые для хранения состояния синхронизатора в схеме {@code sync}.
 */
@Configuration
public class PostgresConfig {

    @Bean(name = "postgresDataSource")
    public DataSource postgresDataSource(final PostgresProperties properties) {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(properties.url());
        dataSource.setUsername(properties.username());
        dataSource.setPassword(properties.password());
        return dataSource;
    }

    @Bean(name = "postgresJdbcTemplate")
    public JdbcTemplate postgresJdbcTemplate(@Qualifier("postgresDataSource") final DataSource postgresDataSource) {
        return new JdbcTemplate(postgresDataSource);
    }
}
