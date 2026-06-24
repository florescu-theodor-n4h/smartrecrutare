package com.samplus.smartrecrutare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;

@Tag(
        name = "SPA Routing Controller",
        description = "Handles fallback routing for Single Page Application (Vue) to ensure " +
                "client-side routing works correctly in history mode without server 404 errors."
)
@Controller(value = "ControlForwardSpaBean")
public class ControlForwardSpa {
    private static final Logger log = LoggerFactory.getLogger(ControlForwardSpa.class);
    @RequestMapping(value = "/{path:[^\\.]*}")
    @Operation(
            summary = "SPA fallback redirect",
            description = "Intercepts all non-mapped backend routes and forwards them internally " +
                    "to index.html so that the Vue application can handle routing on the client side."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Request successfully forwarded to SPA entry point (index.html)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Route not matched and not handled by SPA fallback (unexpected case)"
            )
    })
    public String redirect(@PathVariable @SuppressWarnings("unused") String path) {
        log.debug("Redirect {}", path);
        return "forward:/index.html";
    }
}