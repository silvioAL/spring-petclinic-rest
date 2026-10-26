package org.springframework.samples.petclinic.rest.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exclusao do dono leva as vacinas dos pets dele (SPEC-001, RN-019).
 * Usa o servico real (sem mock). Cria o proprio dono, o proprio tipo de animal e o proprio pet,
 * sem tocar no pet 3 nem no dono 4 da massa, porque o banco e o mesmo para todas as classes.
 * O tipo e proprio porque a exclusao do pet cascateia para o tipo (Pet.type com CascadeType.ALL):
 * com um tipo da massa, que outros pets usam, a exclusao do dono falha pela chave estrangeira.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaExclusaoDonoTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private int criarDono() throws Exception {
        String corpo = """
            {"firstName":"DonoExclusaoDono","lastName":"Isolado","address":"Rua Isolada, 2","city":"Cidade","telephone":"0987654321"}
            """;
        String resposta = this.mockMvc.perform(post("/api/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private int criarTipo() throws Exception {
        String corpo = """
            {"name":"tipo-isolado-dono"}
            """;
        String resposta = this.mockMvc.perform(post("/api/pettypes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private int criarPet(int donoId, int tipoId) throws Exception {
        String corpo = """
            {"name":"PetDoDonoIsolado","birthDate":"2020-01-01","type":{"id":%d,"name":"tipo-isolado-dono"}}
            """.formatted(tipoId);
        String resposta = this.mockMvc.perform(post("/api/owners/" + donoId + "/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    // @verifica: CT-076
    @Test
    @DisplayName("RN-019 · exclusao do dono remove as vacinas dos pets dele")
    void excluirDonoRemoveAsVacinasDosPetsDele() throws Exception {
        int donoId = criarDono();
        int tipoId = criarTipo();
        int petId = criarPet(donoId, tipoId);
        String validade = LocalDate.now().plusDays(5).toString();
        String corpo = """
            {"nome":"VacinaDaExclusaoDoDono","validade":"%s"}
            """.formatted(validade);

        this.mockMvc.perform(post("/api/pets/" + petId + "/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated());

        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("VacinaDaExclusaoDoDono")));

        this.mockMvc.perform(delete("/api/owners/" + donoId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("VacinaDaExclusaoDoDono"))));
    }
}
