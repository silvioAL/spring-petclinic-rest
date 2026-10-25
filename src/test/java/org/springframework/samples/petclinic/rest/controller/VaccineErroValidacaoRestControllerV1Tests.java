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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Erro de validacao do registro de vacina do pet (SPEC-001, POST /api/pets/{petId}/vaccines):
 * a recusa devolve 400 e o corpo aponta o campo e o motivo.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@Transactional
class VaccineErroValidacaoRestControllerV1Tests {

    private static final int PET_ID = 3;

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void initVaccineErroValidacaoTests() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // @verifica: CT-019
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-005 · nome vazio devolve 400 com o campo name no corpo")
    void nomeVazioDevolve400ComCampoNoCorpo() throws Exception {
        String corpo = """
            {"name": "", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(LocalDate.now().minusDays(10), LocalDate.now().plusDays(355));

        this.mockMvc.perform(post("/api/pets/{petId}/vaccines", PET_ID)
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("name")));
    }

    // @verifica: CT-019
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · vencimento anterior a aplicacao devolve 400 com o campo expirationDate no corpo")
    void vencimentoAnteriorADaAplicacaoDevolve400ComCampoNoCorpo() throws Exception {
        String corpo = """
            {"name": "V10", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(LocalDate.now().minusDays(1), LocalDate.now().minusDays(5));

        this.mockMvc.perform(post("/api/pets/{petId}/vaccines", PET_ID)
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("expirationDate")));
    }

    // @verifica: CT-019
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · aplicacao no futuro devolve 400 com o campo applicationDate no corpo")
    void aplicacaoNoFuturoDevolve400ComCampoNoCorpo() throws Exception {
        String corpo = """
            {"name": "V10", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(LocalDate.now().plusDays(3), LocalDate.now().plusDays(365));

        this.mockMvc.perform(post("/api/pets/{petId}/vaccines", PET_ID)
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("applicationDate")));
    }

}
