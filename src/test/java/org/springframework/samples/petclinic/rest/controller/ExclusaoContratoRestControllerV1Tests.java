package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Owner;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato das exclusoes existentes de pet e de dono (SPEC-001 secao 3.1, C8):
 * metodo, caminho e status nao mudam quando a exclusao passa a incluir as vacinas.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class ExclusaoContratoRestControllerV1Tests {

    @MockitoBean
    protected ClinicService clinicService;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private PetMapper petMapper;
    private MockMvc mockMvc;

    private PetDto pet;
    private Owner owner;

    @BeforeEach
    void initExclusaoContratoTests() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        PetTypeDto petType = new PetTypeDto();
        petType.id(2).name("dog");

        pet = new PetDto();
        pet.id(3).name("Rosy").birthDate(LocalDate.now().minusYears(2)).type(petType);

        owner = new Owner();
        owner.setId(1);
    }

    // @verifica: CT-023
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("RN-022 · DELETE /api/pets/{petId} continua devolvendo 204 e aciona a exclusao do pet")
    void excluirPetMantemMetodoCaminhoEStatus204() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pet));

        this.mockMvc.perform(delete("/api/pets/3")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(this.clinicService).deletePet(org.mockito.ArgumentMatchers.any());
    }

    // @verifica: CT-023
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("RN-022 · DELETE /api/pets/{petId} de pet inexistente continua devolvendo 404")
    void excluirPetInexistenteMantemStatus404() throws Exception {
        given(this.clinicService.findPetById(999)).willReturn(null);

        this.mockMvc.perform(delete("/api/pets/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    // @verifica: CT-023
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("RN-019 · DELETE /api/owners/{ownerId} continua devolvendo 204 e aciona a exclusao do dono")
    void excluirDonoMantemMetodoCaminhoEStatus204() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(owner);

        this.mockMvc.perform(delete("/api/owners/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(this.clinicService).deleteOwner(owner);
    }

    // @verifica: CT-023
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("RN-019 · DELETE /api/owners/{ownerId} de dono inexistente continua devolvendo 404")
    void excluirDonoInexistenteMantemStatus404() throws Exception {
        given(this.clinicService.findOwnerById(999)).willReturn(null);

        this.mockMvc.perform(delete("/api/owners/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

}
