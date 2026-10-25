package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Independencia da vacina entre pets (SPEC-001, POST /api/pets/{petId}/vaccines).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class VaccineIndependentePorPetTests {

    @MockitoBean
    protected ClinicService clinicService;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private PetMapper petMapper;
    private MockMvc mockMvc;

    private PetDto primeiroPet;
    private PetDto segundoPet;

    @BeforeEach
    void initVaccineTests() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        PetTypeDto petType = new PetTypeDto();
        petType.id(2).name("dog");

        primeiroPet = new PetDto();
        primeiroPet.id(3).name("Rosy").birthDate(LocalDate.now().minusYears(2)).type(petType);

        segundoPet = new PetDto();
        segundoPet.id(4).name("Jewel").birthDate(LocalDate.now().minusYears(3)).type(petType);
    }

    // @verifica: CT-010
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · mesma vacina em dois pets fica independente em cada um")
    void registrarV10EmDoisPetsDiferentesFicaIndependenteEmCada() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(primeiroPet));
        given(this.clinicService.findPetById(4)).willReturn(petMapper.toPet(segundoPet));

        LocalDate aplicacao = LocalDate.now().minusDays(10);
        LocalDate vencimentoDoPrimeiro = LocalDate.now().plusDays(355);
        LocalDate vencimentoDoSegundo = LocalDate.now().plusDays(200);
        String corpoDoPrimeiro = """
            {"name": "V10", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(aplicacao, vencimentoDoPrimeiro);
        String corpoDoSegundo = """
            {"name": "V10", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(aplicacao, vencimentoDoSegundo);

        // cada pet fica com o seu registro: o petId e o vencimento de um nao vazam para o outro
        this.mockMvc.perform(post("/api/pets/3/vaccines")
                .content(corpoDoPrimeiro)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.petId").value(3))
            .andExpect(jsonPath("$.name").value("V10"))
            .andExpect(jsonPath("$.expirationDate").value(vencimentoDoPrimeiro.toString()));

        this.mockMvc.perform(post("/api/pets/4/vaccines")
                .content(corpoDoSegundo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.petId").value(4))
            .andExpect(jsonPath("$.name").value("V10"))
            .andExpect(jsonPath("$.expirationDate").value(vencimentoDoSegundo.toString()));
    }

}
