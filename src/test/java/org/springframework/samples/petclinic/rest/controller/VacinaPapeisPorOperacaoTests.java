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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Papeis por operacao com a seguranca ligada (SPEC-001 C4, SPEC-002 C6):
 * o POST de vacina e permitido so ao VET_ADMIN e o GET da lista a vencer so ao OWNER_ADMIN.
 * A seguranca fica ligada pelo application.properties de teste
 * (petclinic.security.enable=true). Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class VacinaPapeisPorOperacaoTests {

    private static final String CORPO = "{\"nome\":\"Antirrabica\",\"validade\":\"2027-03-15\"}";

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

    // @verifica: CT-075
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · POST de vacina e permitido ao VET_ADMIN com a seguranca ligada")
    void permitePostAoVetAdmin() throws Exception {
        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(CORPO))
            .andExpect(status().isCreated());
    }

    // @verifica: CT-075
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("RN-001 · POST de vacina e negado ao OWNER_ADMIN com a seguranca ligada")
    void negaPostAoOwnerAdmin() throws Exception {
        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(CORPO))
            .andExpect(status().isForbidden());
    }

    // @verifica: CT-075
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("RN-001 · GET da lista a vencer e permitido ao OWNER_ADMIN com a seguranca ligada")
    void permiteGetAoOwnerAdmin() throws Exception {
        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    // @verifica: CT-075
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · GET da lista a vencer e negado ao VET_ADMIN com a seguranca ligada")
    void negaGetAoVetAdmin() throws Exception {
        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
}
