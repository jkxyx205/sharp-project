package com.rick.report.config;

import com.rick.db.config.GridServiceAutoConfiguration;
import com.rick.db.service.GridService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rick
 * @createdAt 2022-11-03 10:40:00
 */
@Configuration
@ConditionalOnSingleCandidate(GridService.class)
@AutoConfigureAfter({GridServiceAutoConfiguration.class})
public class ReportServiceAutoConfiguration {

    public ReportServiceAutoConfiguration() {
    }

    @Configuration
    @ComponentScan(value = "com.rick.report.core")
    static class ReportServiceConfiguration {

    }
}
