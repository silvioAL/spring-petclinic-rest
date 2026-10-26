package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lista de vacinas a vencer: size negativo ou acima do limite maximo e recusado com 400
 * (SPEC-002, secao 8: page e size inteiros, nao negativos, com limite maximo de size).
 * Usa o servico real (sem mock), com a seguranca desligada.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaListaSizeForaDoLimiteTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-095
    @Test
    @DisplayName("RN-035 · size negativo devolve 400")
    void sizeNegativoDevolve400() throws Exception {
        consultar("0", "-1");
    }

    // @verifica: CT-095
    @Test
    @DisplayName("RN-035 · size muito negativo devolve 400")
    void sizeMuitoNegativoDevolve400() throws Exception {
        consultar("0", "-1000");
    }

    // @verifica: CT-095
    @Test
    @DisplayName("RN-035 · size acima do limite maximo devolve 400")
    void sizeAcimaDoLimiteDevolve400() throws Exception {
        consultar("0", String.valueOf(Integer.MAX_VALUE));
    }

    private void consultar(String page, String size) throws Exception {
        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .param("page", page)
                .param("size", size)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
