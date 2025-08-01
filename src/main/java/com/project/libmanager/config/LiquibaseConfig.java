package com.project.libmanager.config;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfig {
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        liquibase.setShouldRun(false);
        return liquibase;
    }

    @Bean
    public ApplicationRunner runLiquibase(SpringLiquibase liquibase) {
        return args -> {
            liquibase.setShouldRun(true);
            liquibase.afterPropertiesSet();
        };
    }
}
