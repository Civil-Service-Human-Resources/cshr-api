package uk.gov.cshr.vcm.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import uk.gov.cshr.vcm.repository.VacancyRepository;

import java.net.SocketTimeoutException;

@Configuration
@ComponentScan
@EntityScan("uk.gov.cshr.vcm.model")
@EnableJpaRepositories("uk.gov.cshr.vcm.repository")
@EnableAutoConfiguration
public class RepositoryTestConfiguration {

}
