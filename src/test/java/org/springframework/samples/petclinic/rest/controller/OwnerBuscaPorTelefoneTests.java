package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.controller.v1.OwnerRestControllerV1;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Busca de donos pelo telefone completo (SPEC-001).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerBuscaPorTelefoneTests {

    @Autowired
    private OwnerRestControllerV1 ownerRestControllerV1;

    @Autowired
    private OwnerMapper ownerMapper;

    @MockitoBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerRestControllerV1)
            .setControllerAdvice(new ExceptionControllerAdvice())
            .build();
    }

    private OwnerDto donoComTelefoneEUmAnimal() {
        PetTypeDto tipo = new PetTypeDto();
        tipo.id(2).name("dog");
        PetDto animal = new PetDto();
        animal.id(1).name("Rosy").birthDate(LocalDate.now()).type(tipo);
        OwnerDto dono = new OwnerDto();
        return dono.id(1).firstName("George").lastName("Franklin").address("110 W. Liberty St.")
            .city("Madison").telephone("6085551023").addPetsItem(animal);
    }

    // @verifica: CT-026
    @Test
    @DisplayName("RN-008 · telefone de 10 digitos devolve o unico dono com seus animais")
    @WithMockUser(roles = "OWNER_ADMIN")
    void buscaPorTelefoneDevolveOUnicoDonoComSeusAnimais() throws Exception {
        given(this.clinicService.findOwnerByTelephone("6085551023"))
            .willReturn(ownerMapper.toOwners(List.of(donoComTelefoneEUmAnimal())));
        this.mockMvc.perform(get("/api/owners?telephone=6085551023")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[0].id").value(1))
            .andExpect(jsonPath("$.[0].telephone").value("6085551023"))
            .andExpect(jsonPath("$.[0].pets.length()").value(1))
            .andExpect(jsonPath("$.[0].pets.[0].name").value("Rosy"));
    }
}
