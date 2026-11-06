package pe.edu.upeu.PharmaBackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import pe.edu.upeu.PharmaBackend.security.JwtService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PharmaBackendApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @MockitoBean(name = "usuarioDetailsService") UserDetailsService usuarios;

	@Test
	void contextLoads() {
	}

    @Test
    void saludPublicaSinDetalles() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.components").doesNotExist())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void metricasExigenAdministrador() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/metrics").header("Authorization", token("vendedor", "ROLE_VENDEDOR")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/actuator/metrics").header("Authorization", token("admin", "ROLE_ADMIN", "USUARIOS_ADMINISTRAR")))
                .andExpect(status().isOk());
    }

    @Test
    void saludDetalladaSoloParaAdmin() throws Exception {
        mvc.perform(get("/actuator/health").header("Authorization", token("vendedor", "ROLE_VENDEDOR")))
                .andExpect(jsonPath("$.components").doesNotExist());
        mvc.perform(get("/actuator/health").header("Authorization", token("admin", "ROLE_ADMIN", "USUARIOS_ADMINISTRAR")))
                .andExpect(jsonPath("$.components.db").exists())
                .andExpect(jsonPath("$.components.redis").exists());
    }

    private String token(String nombre, String... permisos) {
        var usuario = User.withUsername(nombre).password("no-utilizada").authorities(permisos).build();
        when(usuarios.loadUserByUsername(nombre)).thenReturn(usuario);
        return "Bearer " + jwt.generarToken(usuario);
    }

}
