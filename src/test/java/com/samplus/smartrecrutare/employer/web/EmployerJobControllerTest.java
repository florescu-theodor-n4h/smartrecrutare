package com.samplus.smartrecrutare.employer.web;

import com.samplus.smartrecrutare.ServiciuJoburi;
import com.samplus.smartrecrutare.job.dto.JobPublishRequest;
import com.samplus.smartrecrutare.job.dto.JobResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmployerJobControllerTest {

    private final ServiciuJoburi serviciuJoburi = mock(ServiciuJoburi.class);
    private final EmployerJobController controller = new EmployerJobController(serviciuJoburi);

    @Test
    void publicaTrimiteAngajatorulDinRutaCatreServiciu() {
        JobPublishRequest request = new JobPublishRequest(
                "Java Developer",
                "API backend",
                null,
                "Remote",
                "5000 EUR",
                "Full-time",
                true
        );
        JobResponse asteptat = new JobResponse();
        asteptat.setId(42L);
        when(serviciuJoburi.crearePentruAngajator(7L, request)).thenReturn(asteptat);

        var response = controller.publica(7L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(asteptat);
        verify(serviciuJoburi).crearePentruAngajator(7L, request);
    }
}
