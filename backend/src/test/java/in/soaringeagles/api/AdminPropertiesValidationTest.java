package in.soaringeagles.api;

import static org.assertj.core.api.Assertions.assertThat;

import in.soaringeagles.api.config.AdminProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class AdminPropertiesValidationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(AdminPropertiesConfiguration.class);

    @Test
    void startupConfigurationRejectsMissingInitialAdminCredentials() {
        contextRunner
                .withPropertyValues("app.admin.email=", "app.admin.password-hash=")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasMessageContaining("app.admin");
                });
    }

    @Test
    void startupConfigurationRejectsNonBcryptPasswordHash() {
        contextRunner
                .withPropertyValues(
                        "app.admin.email=admin@example.invalid",
                        "app.admin.password-hash=plaintext")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AdminProperties.class)
    static class AdminPropertiesConfiguration {
    }
}
