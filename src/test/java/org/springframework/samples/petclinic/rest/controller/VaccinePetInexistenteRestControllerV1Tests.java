package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pet inexistente no registro de vacina (SPEC-001, POST /api/pets/{petId}/vaccines, 404).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class VaccinePetInexistenteRestControllerV1Tests {

    @MockitoBean
    protected ClinicService clinicService;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void initVaccinePetInexistenteTests() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // @verifica: CT-020
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-023 · pet inexistente devolve 404")
    void registrarVacinaParaPetInexistenteDevolve404() throws Exception {
        given(this.clinicService.findPetById(999)).willReturn(null);

        LocalDate aplicacao = LocalDate.now().minusDays(10);
        LocalDate vencimento = LocalDate.now().plusDays(355);
        String corpo = """
            {"name": "V10", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(aplicacao, vencimento);

        this.mockMvc.perform(post("/api/pets/999/vaccines")
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

}
