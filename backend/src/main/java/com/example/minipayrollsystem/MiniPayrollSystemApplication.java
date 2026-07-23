package com.example.minipayrollsystem;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.net.URI;
import java.util.TimeZone;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class MiniPayrollSystemApplication {

    public static void main(String[] args) {
        applyDatabaseUrlFromEnv();
        SpringApplication.run(MiniPayrollSystemApplication.class, args);
    }

    @PostConstruct
    public void printTimezone() {
        System.out.println("Default JVM TimeZone = " + TimeZone.getDefault().getID());
    }

    /**
     * Render/Heroku provide DATABASE_URL as postgres://user:pass@host:port/db.
     * Spring Boot expects jdbc:postgresql://... plus separate username/password.
     */
    static void applyDatabaseUrlFromEnv() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }
        if (databaseUrl.startsWith("jdbc:")) {
            System.setProperty("spring.datasource.url", databaseUrl);
            return;
        }

        try {
            String normalized = databaseUrl
                    .replace("postgres://", "postgresql://")
                    .replace("postgresql://", "http://");
            URI uri = URI.create(normalized);
            String userInfo = uri.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                String[] parts = userInfo.split(":", 2);
                System.setProperty("spring.datasource.username", parts[0]);
                System.setProperty("spring.datasource.password", parts[1]);
            }
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                    + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                    + uri.getPath();
            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                jdbcUrl += "?" + uri.getQuery();
            }
            System.setProperty("spring.datasource.url", jdbcUrl);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid DATABASE_URL", ex);
        }
    }
}
