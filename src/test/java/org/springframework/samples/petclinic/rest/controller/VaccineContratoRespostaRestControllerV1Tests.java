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

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato da resposta do registro de vacina do pet (SPEC-001, POST /api/pets/{petId}/vaccines).
 * Usa o pet 3 da massa de teste e a persistencia real, para que o identificador da vacina exista.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@Transactional
class VaccineContratoRespostaRestControllerV1Tests {

    private static final int PET_ID = 3;

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void initVaccineContratoTests() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // @verifica: CT-018
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · registro válido devolve 201 com identificador, petId, nome digitado e datas")
    void registrarVacinaValidaDevolve201ComIdentificadorPetIdNomeDigitadoEDatas() throws Exception {
        LocalDate aplicacao = LocalDate.now().minusDays(10);
        LocalDate vencimento = LocalDate.now().plusDays(355);
        String corpo = """
            {"name": " V-10 ", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(aplicacao, vencimento);

        this.mockMvc.perform(post("/api/pets/{petId}/vaccines", PET_ID)
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.id").value(greaterThan(0)))
            .andExpect(jsonPath("$.petId").value(PET_ID))
            .andExpect(jsonPath("$.name").value(" V-10 "))
            .andExpect(jsonPath("$.applicationDate").value(aplicacao.toString()))
            .andExpect(jsonPath("$.expirationDate").value(vencimento.toString()));
    }

}
