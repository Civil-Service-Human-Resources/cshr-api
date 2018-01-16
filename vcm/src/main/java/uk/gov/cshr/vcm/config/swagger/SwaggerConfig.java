package uk.gov.cshr.vcm.config.swagger;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;

import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Configuration to enable and setup Swagger
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket vacancyApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("uk.gov.cshr.vcm.controller"))
                .paths(vacancyPaths())
                .build()
                .pathMapping("/")
                .directModelSubstitute(LocalDate.class,
                        String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .useDefaultResponseMessages(false)
                .enableUrlTemplating(true)
                .tags(new Tag("Vacancy Service", "Apis relating to jobs and vacancies"))
                ;
    }

    private Predicate<String> vacancyPaths() {
        return regex("/vacancy.*");
    }
}


