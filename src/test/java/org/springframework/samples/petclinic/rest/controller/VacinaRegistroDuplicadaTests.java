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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Mesma vacina registrada duas vezes com validades diferentes (SPEC-001, C6).
 * As duas aplicacoes sao aceitas; a anterior nao e alterada.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaRegistroDuplicadaTests {

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

    // @verifica: CT-067
    @Test
    @DisplayName("RN-035 · mesma vacina registrada duas vezes com validades diferentes devolve 201 nas duas")
    void mantemAsDuasQuandoAMesmaVacinaEhRegistradaDuasVezes() throws Exception {
        String primeira = "{\"nome\":\"Antirrabica\",\"validade\":\"2026-03-15\"}";
        String renovada = "{\"nome\":\"Antirrabica\",\"validade\":\"2027-03-15\"}";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(primeira))
            .andExpect(status().isCreated());

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(renovada))
            .andExpect(status().isCreated());
    }
}
