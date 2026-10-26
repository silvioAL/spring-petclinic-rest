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
 * Lista de vacinas a vencer: page ou size invalidos (negativos ou que nao sao inteiros) sao
 * recusados com 400 (SPEC-002, secao 8, validacoes de paginacao).
 * Usa o servico real (sem mock), com a seguranca desligada.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaListaParametrosInvalidosTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-089
    @Test
    @DisplayName("RN-035 · page negativo devolve 400")
    void pageNegativoDevolve400() throws Exception {
        consultar("-1", "10");
    }

    // @verifica: CT-089
    @Test
    @DisplayName("RN-035 · size negativo devolve 400")
    void sizeNegativoDevolve400() throws Exception {
        consultar("0", "-5");
    }

    // @verifica: CT-089
    @Test
    @DisplayName("RN-035 · page que nao e inteiro devolve 400")
    void pageNaoInteiroDevolve400() throws Exception {
        consultar("abc", "10");
    }

    // @verifica: CT-089
    @Test
    @DisplayName("RN-035 · size que nao e inteiro devolve 400")
    void sizeNaoInteiroDevolve400() throws Exception {
        consultar("0", "1.5");
    }

    private void consultar(String page, String size) throws Exception {
        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .param("page", page)
                .param("size", size)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
