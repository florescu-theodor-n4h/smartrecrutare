package com.samplus.smartrecrutare;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerEroare implements ErrorController {

    @RequestMapping("${server.error.path:${error.path:/error}}")
    public ProblemDetail error(HttpServletRequest request) {
        HttpStatus status = status(request);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, "Cererea nu a putut fi procesata");
        detail.setTitle(status.is5xxServerError() ? "Eroare server" : "Eroare cerere");
        return detail;
    }

    private HttpStatus status(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode instanceof Integer code) {
            HttpStatus status = HttpStatus.resolve(code);
            if (status != null) {
                return status;
            }
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
