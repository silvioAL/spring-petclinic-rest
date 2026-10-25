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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Registro de vacina do pet (SPEC-001, POST /api/pets/{petId}/vaccines).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class VaccineRestControllerV1Tests {

    @MockitoBean
    protected ClinicService clinicService;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private PetMapper petMapper;
    private MockMvc mockMvc;

    private PetDto pet;

    @BeforeEach
    void initVaccineTests() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        PetTypeDto petType = new PetTypeDto();
        petType.id(2).name("dog");

        pet = new PetDto();
        pet.id(3).name("Rosy").birthDate(LocalDate.now().minusYears(2)).type(petType);
    }

    // @verifica: CT-001
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · vacina válida de pet existente é registrada com 201")
    void registrarVacinaValidaDePetExistenteDevolve201() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pet));

        LocalDate aplicacao = LocalDate.now().minusDays(10);
        LocalDate vencimento = LocalDate.now().plusDays(355);
        String corpo = """
            {"name": "V10", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(aplicacao, vencimento);

        this.mockMvc.perform(post("/api/pets/3/vaccines")
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.petId").value(3))
            .andExpect(jsonPath("$.name").value("V10"))
            .andExpect(jsonPath("$.applicationDate").value(aplicacao.toString()))
            .andExpect(jsonPath("$.expirationDate").value(vencimento.toString()));
    }

    // @verifica: CT-002
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · aplicação no dia de hoje é aceita com 201")
    void registrarVacinaComAplicacaoHojeDevolve201() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pet));

        LocalDate aplicacao = LocalDate.now();
        LocalDate vencimento = LocalDate.now().plusDays(365);
        String corpo = """
            {"name": "V10", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(aplicacao, vencimento);

        this.mockMvc.perform(post("/api/pets/3/vaccines")
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.petId").value(3))
            .andExpect(jsonPath("$.applicationDate").value(aplicacao.toString()))
            .andExpect(jsonPath("$.expirationDate").value(vencimento.toString()));
    }

    // @verifica: CT-003
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-005 · nome vazio ou só com espaços e hífen é recusado com 400 no nome")
    void registrarVacinaComNomeVazioOuSoEspacosEHifenDevolve400NoNome() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pet));

        LocalDate aplicacao = LocalDate.now().minusDays(10);
        LocalDate vencimento = LocalDate.now().plusDays(355);
        String[] nomesInvalidos = {"", "   ", "-", " - - "};

        for (String nome : nomesInvalidos) {
            String corpo = """
                {"name": "%s", "applicationDate": "%s", "expirationDate": "%s"}
                """.formatted(nome, aplicacao, vencimento);

            this.mockMvc.perform(post("/api/pets/3/vaccines")
                    .content(corpo)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("name")));
        }
    }

    // @verifica: CT-004
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · falta da data de aplicação ou do vencimento é recusada com 400 no campo")
    void registrarVacinaSemDataDeAplicacaoOuSemVencimentoDevolve400NoCampo() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pet));

        LocalDate aplicacao = LocalDate.now().minusDays(10);
        LocalDate vencimento = LocalDate.now().plusDays(355);

        String semAplicacao = """
            {"name": "V10", "expirationDate": "%s"}
            """.formatted(vencimento);

        this.mockMvc.perform(post("/api/pets/3/vaccines")
                .content(semAplicacao)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("applicationDate")));

        String semVencimento = """
            {"name": "V10", "applicationDate": "%s"}
            """.formatted(aplicacao);

        this.mockMvc.perform(post("/api/pets/3/vaccines")
                .content(semVencimento)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("expirationDate")));
    }

    // @verifica: CT-005
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-002 · vencimento anterior à aplicação é recusado com 400 no vencimento")
    void registrarVacinaComVencimentoAnteriorAAplicacaoDevolve400NoVencimento() throws Exception {
        given(this.clinicService.findPetById(3)).willReturn(petMapper.toPet(pet));

        LocalDate aplicacao = LocalDate.now().minusDays(10);
        LocalDate vencimento = aplicacao.minusDays(1);
        String corpo = """
            {"name": "V10", "applicationDate": "%s", "expirationDate": "%s"}
            """.formatted(aplicacao, vencimento);

        this.mockMvc.perform(post("/api/pets/3/vaccines")
                .content(corpo)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("expirationDate")));
    }

}
