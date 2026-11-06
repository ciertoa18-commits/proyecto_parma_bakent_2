package pe.edu.upeu.PharmaBackend.config;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.*;

class RequestIdFilterTest {
    @Test
    void comparteIdConLaRespuestaYLimpiaMdc() throws Exception {
        var filter = new RequestIdFilter();
        var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/categorias"), response,
                (req, res) -> assertThat(MDC.get(RequestIdFilter.CLAVE_MDC))
                        .isEqualTo(response.getHeader(RequestIdFilter.CABECERA))
                        .matches("[a-f0-9]{8}"));
        assertThat(MDC.get(RequestIdFilter.CLAVE_MDC)).isNull();
    }

    @Test
    void limpiaMdcInclusoCuandoFallaLaPeticion() {
        assertThatThrownBy(() -> new RequestIdFilter().doFilter(
                new MockHttpServletRequest("GET", "/api/v1/productos"),
                new MockHttpServletResponse(), (req, res) -> {
                    throw new ServletException("fallo");
                })).isInstanceOf(ServletException.class);
        assertThat(MDC.get(RequestIdFilter.CLAVE_MDC)).isNull();
    }
}
