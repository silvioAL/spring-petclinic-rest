package org.springframework.samples.petclinic.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Injectable clock: the use cases ask it for "today", so the limit tests do not depend on the real day.
 * Until the clinic time zone is confirmed (SPEC-001, section 8), "today" is the server date.
 */
@Configuration
public class ClockConfig {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
