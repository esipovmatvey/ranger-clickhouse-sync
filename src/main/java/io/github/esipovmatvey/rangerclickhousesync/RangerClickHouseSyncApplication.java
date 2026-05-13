package io.github.esipovmatvey.rangerclickhousesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableAsync
public class RangerClickHouseSyncApplication {
    static void main(final String[] args) {
        SpringApplication.run(RangerClickHouseSyncApplication.class, args);
    }
}
