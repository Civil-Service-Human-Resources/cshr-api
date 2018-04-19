package uk.gov.cshr.vcm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VcmApplication {

    private static final Logger log = LoggerFactory.getLogger(VcmApplication.class);

    public static void main(String[] args) {
        log.debug("VcmApplication starting");
        SpringApplication.run(VcmApplication.class, args);
    }
}
