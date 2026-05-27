package rangerclickhousesync.config;

import rangerclickhousesync.config.properties.ClickHouseProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Конфигурация подключения к ClickHouse.
 * <p>
 * Определяет бины {@link DataSource} и {@link JdbcTemplate},
 * используемые для выполнения SQL-запросов к ClickHouse.
 */
@Configuration
public class ClickHouseConfig {

    @Bean(name = "clickHouseDataSource")
    public DataSource clickHouseDataSource(final ClickHouseProperties clickHouseProperties) {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(clickHouseProperties.url());
        dataSource.setUsername(clickHouseProperties.username());
        dataSource.setPassword(clickHouseProperties.password());
        return dataSource;
    }

    @Bean(name = "clickHouseJdbcTemplate")
    public JdbcTemplate clickHouseJdbcTemplate(@Qualifier("clickHouseDataSource") final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
