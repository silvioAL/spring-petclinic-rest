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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Resumo de visitas do tutor: o tutor que nao existe responde 404, sem corpo (SPEC-001, 2.1).
 * Usa o servico real (sem mock). O tutor 6 da massa e o controle: mostra que a rota existe e que o
 * 404 e do tutor, nao da rota.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class ResumoDeVisitasTutorInexistenteTest {

    private static final int TUTOR_DA_MASSA = 6;
    private static final int TUTOR_INEXISTENTE = 999999;

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
    @DisplayName("SPEC-001 2.1 · resumo de visitas de tutor que nao existe responde 404 sem corpo")
    void tutorQueNaoExisteResponde404SemCorpo() throws Exception {
        this.mockMvc.perform(get("/api/owners/{ownerId}/visits/summary", TUTOR_DA_MASSA)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ownerId").value(TUTOR_DA_MASSA));

        this.mockMvc.perform(get("/api/owners/{ownerId}/visits/summary", TUTOR_INEXISTENTE)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }
}
