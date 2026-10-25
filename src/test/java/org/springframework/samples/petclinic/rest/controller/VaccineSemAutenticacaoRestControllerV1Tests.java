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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Registro de vacina do pet com a segurança ligada e sem autenticação (SPEC-001, POST /api/pets/{petId}/vaccines).
 * A segurança fica ligada em src/test/resources/application.properties (petclinic.security.enable=true).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class VaccineSemAutenticacaoRestControllerV1Tests {

    @MockitoBean
    protected ClinicService clinicService;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private PetMapper petMapper;

    private PetDto pet;

    @BeforeEach
    void initVaccineSemAutenticacaoTests() {
        PetTypeDto petType = new PetTypeDto();
        petType.id(2).name("dog");

        pet = new PetDto();
        pet.id(3).name("Rosy").birthDate(LocalDate.now().minusYears(2)).type(petType);
    }

    // @verifica: CT-013
    @Test
    @DisplayName("RN-004 · segurança ligada sem autenticação recebe 401")
    void registrarVacinaComSegurancaLigadaSemAutenticacaoDevolve401() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pet));

        LocalDate aplicacao = LocalDate.now().minusDays(10);
        LocalDate vencimento = LocalDate.now().plusDays(355);
        String corpo = "{\"name\": \"V10\", \"applicationDate\": \"%s\", \"expirationDate\": \"%s\"}"
            .formatted(aplicacao, vencimento);

        MockMvc mockMvcComSeguranca = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        mockMvcComSeguranca.perform(post("/api/pets/3/vaccines")
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    // @verifica: CT-021
    @Test
    @DisplayName("RN-004 · segurança ligada sem autenticação devolve 401 e não chega ao caso de uso")
    void registrarVacinaSemAutenticacaoDevolve401SemTocarNoPet() throws Exception {
        LocalDate aplicacao = LocalDate.now().minusDays(5);
        LocalDate vencimento = LocalDate.now().plusDays(360);
        String corpo = "{\"name\": \"Antirrabica\", \"applicationDate\": \"%s\", \"expirationDate\": \"%s\"}"
            .formatted(aplicacao, vencimento);

        MockMvc mockMvcComSeguranca = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        mockMvcComSeguranca.perform(post("/api/pets/3/vaccines")
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(this.clinicService);
    }

}
