package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Resumo de visitas de um tutor que nao existe: GET /api/owners/{ownerId}/visits/summary
 * devolve 404 sem corpo (SPEC-001, 2.1), com o banco de teste e sem dubles.
 * A seguranca fica ligada pelo application.properties de teste.
 * O teste falha ate a implementacao existir: hoje a rota nao existe e a resposta e 500.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class ResumoDeVisitasTutorInexistenteTest {

    private static final int TUTOR_INEXISTENTE = 99999;

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-903
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("RN-001 · resumo de visitas de tutor que nao existe responde 404 sem corpo")
    void devolve404ParaTutorQueNaoExiste() throws Exception {
        this.mockMvc.perform(get("/api/owners/{ownerId}/visits/summary", TUTOR_INEXISTENTE)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }
}
