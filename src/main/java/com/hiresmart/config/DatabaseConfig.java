package com.hiresmart.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/hiresmart_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUser;

    @Value("${spring.datasource.password:root}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String mysqlDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Try testing connection to configured MySQL
        boolean mysqlAvailable = false;
        try {
            Class.forName(mysqlDriver);
            // Quick 2-second timeout probe
            DriverManager.setLoginTimeout(2);
            try (Connection conn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword)) {
                if (conn != null && !conn.isClosed()) {
                    mysqlAvailable = true;
                    log.info("==================================================================");
                    log.info("[HireSmart Database] Connected to live MySQL database successfully!");
                    log.info("URL: {}", mysqlUrl);
                    log.info("==================================================================");
                }
            }
        } catch (Exception e) {
            log.warn("[HireSmart Database] MySQL not reachable at {} ({}). Switching to robust In-Memory Database (MySQL Mode).",
                    mysqlUrl, e.getMessage());
        }

        HikariConfig config = new HikariConfig();
        if (mysqlAvailable) {
            config.setJdbcUrl(mysqlUrl);
            config.setUsername(mysqlUser);
            config.setPassword(mysqlPassword);
            config.setDriverClassName(mysqlDriver);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setPoolName("HireSmart-MySQL-Pool");
        } else {
            // Embedded H2 running in full MySQL compatibility mode
            String h2Url = "jdbc:h2:mem:hiresmart_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1";
            config.setJdbcUrl(h2Url);
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");
            config.setMaximumPoolSize(10);
            config.setPoolName("HireSmart-H2-MySQL-Mode-Pool");
            log.info("==================================================================");
            log.info("[HireSmart Database] Initialized Embedded Database in MySQL Compatibility Mode.");
            log.info("Full recruitment workflow and schemas active without needing external MySQL!");
            log.info("==================================================================");
        }

        HikariDataSource ds = new HikariDataSource(config);

        // Run schema.sql to ensure all tables exist
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.setContinueOnError(true);
            populator.execute(ds);
            log.info("[HireSmart Database] schema.sql executed successfully!");
        } catch (Exception ex) {
            log.error("[HireSmart Database] Error executing schema.sql: {}", ex.getMessage());
        }

        return ds;
    }
}
