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
 * Registro de vacina com validade ja vencida: POST /api/pets/{petId}/vacinas (SPEC-001).
 * A validade vencida e aceita para permitir lancar historico (INCERTEZA da SPEC, ainda a confirmar pela bruna-po).
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class VacinaRegistroValidadeVencidaTests {

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

    // @verifica: CT-078
    @Test
    @DisplayName("RN-037 · validade ja vencida e aceita no registro e devolve 201")
    @WithMockUser(roles = "VET_ADMIN")
    void aceitaVacinaComValidadeJaVencida() throws Exception {
        String corpo = """
            {"nome":"Antirrabica","validade":"2020-01-15"}""";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.petId").value(3))
            .andExpect(jsonPath("$.nome").value("Antirrabica"))
            .andExpect(jsonPath("$.validade").value("2020-01-15"));
    }

    // @verifica: CT-078
    @Test
    @DisplayName("RN-037 · validade de ontem tambem e aceita no registro e devolve 201")
    @WithMockUser(roles = "VET_ADMIN")
    void aceitaVacinaComValidadeDeOntem() throws Exception {
        String ontem = LocalDate.now().minusDays(1).toString();
        String corpo = "{\"nome\":\"Antirrabica\",\"validade\":\"" + ontem + "\"}";

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.validade").value(ontem));
    }
}
