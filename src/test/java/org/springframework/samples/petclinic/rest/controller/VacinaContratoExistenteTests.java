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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Operacoes existentes mantem o contrato apos a nova operacao de vacina (SPEC-001, C8).
 * Registra uma vacina e confere que a consulta do pet segue com os mesmos campos.
 * Os testes falham ate a implementacao existir (o registro da vacina ainda nao existe).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class VacinaContratoExistenteTests {

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
        pet.id(3).name("Rosy").birthDate(LocalDate.of(2020, 1, 10)).type(tipo);
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pet));
    }

    // @verifica: CT-077
    @Test
    @DisplayName("SPEC-001 C8 · consulta do pet mantem o contrato depois de registrar uma vacina")
    void consultaDoPetMantemOContratoAposRegistrarVacina() throws Exception {
        String corpo = "{\"nome\":\"Antirrabica\",\"validade\":\"2027-03-15\"}";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .with(user("vet").roles("VET_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated());

        this.mockMvc.perform(get("/api/pets/3")
                .with(user("recepcao").roles("OWNER_ADMIN"))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Rosy"))
            .andExpect(jsonPath("$.birthDate").value("2020-01-10"))
            .andExpect(jsonPath("$.type.id").value(2))
            .andExpect(jsonPath("$.type.name").value("dog"))
            .andExpect(jsonPath("$.vacinas").doesNotExist());
    }
}
