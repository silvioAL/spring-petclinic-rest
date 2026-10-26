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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Registro de vacina aplicada a um pet: POST /api/pets/{petId}/vacinas (SPEC-001).
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class VacinaRegistroTests {

    @MockitoBean
    protected ClinicService clinicService;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private PetMapper petMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        PetTypeDto tipo = new PetTypeDto();
        tipo.id(2).name("dog");
        PetDto pet = new PetDto();
        pet.id(3).name("Rosy").birthDate(LocalDate.now()).type(tipo);
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pet));
    }

    // @verifica: CT-059
    @Test
    @DisplayName("RF-006 · vacina com nome e validade validos registrada pelo VET_ADMIN devolve 201")
    @WithMockUser(roles = "VET_ADMIN")
    void registraVacinaComNomeEValidadeValidos() throws Exception {
        String corpo = "{\"nome\":\"Antirrabica\",\"validade\":\"2027-03-15\"}";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.petId").value(3))
            .andExpect(jsonPath("$.nome").value("Antirrabica"))
            .andExpect(jsonPath("$.validade").value("2027-03-15"));
    }

    // @verifica: CT-060
    @Test
    @DisplayName("RN-037 · registro sem o campo nome devolve 400")
    @WithMockUser(roles = "VET_ADMIN")
    void recusaVacinaSemNome() throws Exception {
        String corpo = """
            {"validade":"2027-03-15"}""";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isBadRequest());
    }

    // @verifica: CT-060
    @Test
    @DisplayName("RN-037 · registro com nome vazio devolve 400")
    @WithMockUser(roles = "VET_ADMIN")
    void recusaVacinaComNomeVazio() throws Exception {
        String corpo = """
            {"nome":"","validade":"2027-03-15"}""";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isBadRequest());
    }

    // @verifica: CT-060
    @Test
    @DisplayName("RN-037 · registro com nome so de espacos devolve 400")
    @WithMockUser(roles = "VET_ADMIN")
    void recusaVacinaComNomeSoDeEspacos() throws Exception {
        String corpo = """
            {"nome":"   ","validade":"2027-03-15"}""";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isBadRequest());
    }

    // @verifica: CT-061
    @Test
    @DisplayName("RN-037 · registro sem o campo validade devolve 400")
    @WithMockUser(roles = "VET_ADMIN")
    void recusaVacinaSemValidade() throws Exception {
        String corpo = """
            {"nome":"Antirrabica"}""";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isBadRequest());
    }

    // @verifica: CT-062
    @Test
    @DisplayName("RN-037 · registro com validade fora do formato yyyy-MM-dd devolve 400")
    @WithMockUser(roles = "VET_ADMIN")
    void recusaVacinaComValidadeEmFormatoInvalido() throws Exception {
        String corpo = """
            {"nome":"Antirrabica","validade":"15/03/2027"}""";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isBadRequest());
    }

    // @verifica: CT-062
    @Test
    @DisplayName("RN-037 · registro com validade que nao e data devolve 400")
    @WithMockUser(roles = "VET_ADMIN")
    void recusaVacinaComValidadeQueNaoEData() throws Exception {
        String corpo = """
            {"nome":"Antirrabica","validade":"amanha"}""";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isBadRequest());
    }

    // @verifica: CT-063
    @Test
    @DisplayName("RN-023 · registro de vacina para pet inexistente devolve 404")
    @WithMockUser(roles = "VET_ADMIN")
    void recusaVacinaParaPetInexistente() throws Exception {
        given(this.clinicService.findPetById(99)).willReturn(null);
        String corpo = """
            {"nome":"Antirrabica","validade":"2027-03-15"}""";

        this.mockMvc.perform(post("/api/pets/99/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isNotFound());
    }
}
