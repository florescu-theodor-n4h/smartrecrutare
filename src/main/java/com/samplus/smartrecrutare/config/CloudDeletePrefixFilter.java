package com.samplus.smartrecrutare.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CloudDeletePrefixFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(CloudDeletePrefixFilter.class);

    private static final String PREFIX = "/backend";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String uri = request.getRequestURI();
        return !uri.startsWith(PREFIX + "/")  &&
                /*!request.getRequestURI()
                        .strip()
                        .stripTrailing()
                        .trim()
                        .endsWith(PREFIX);*/
                !uri.equals(PREFIX);
    }

    @Override
    @NullMarked
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String stripped = uri.substring(PREFIX.length());

        if (request.getQueryString() != null) {
            stripped += "?" + request.getQueryString();
        }

        log.info("Stripping proxy prefix: {} -> {}", uri, stripped);

        request.getRequestDispatcher(stripped).forward(request, response);
    }
}
