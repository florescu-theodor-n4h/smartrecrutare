package com.samplus.smartrecrutare.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class AWSCloudProxyFilterConfig {

    @Bean
    public FilterRegistrationBean<CloudDeletePrefixFilter> backendPrefixStripFilterRegistration() {
        FilterRegistrationBean<CloudDeletePrefixFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(new CloudDeletePrefixFilter());
        registration.addUrlPatterns("/*");
        registration.setName("backendPrefixStripFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}