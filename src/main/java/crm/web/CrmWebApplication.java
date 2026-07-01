package crm.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Spring Boot entry point for the CRM web application.
 *
 * <p>This class exposes the existing Java domain (services, DAOs, design
 * patterns) as a REST API. The domain keeps managing its own database
 * connection pool through {@link crm.config.DatabaseConnection}, so Spring's
 * automatic {@code DataSource} configuration is explicitly disabled.</p>
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class CrmWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmWebApplication.class, args);
    }
}
