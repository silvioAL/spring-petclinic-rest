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
 * Exclusao do pet leva as vacinas dele, antes do pet (SPEC-001, C7).
 * Usa o servico real (sem mock). Cria o proprio dono e o proprio pet (e apaga os dois),
 * sem tocar no pet 3 nem no dono 4 da massa, porque o banco e o mesmo para todas as classes.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaExclusaoPetTests {

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
            {"firstName":"DonoExclusaoPet","lastName":"Isolado","address":"Rua Isolada, 1","city":"Cidade","telephone":"1234567890"}
            """;
        String resposta = this.mockMvc.perform(post("/api/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private int criarPet(int donoId) throws Exception {
        String corpo = """
            {"name":"PetDaExclusaoIsolado","birthDate":"2020-01-01","type":{"id":1,"name":"cat"}}
            """;
        String resposta = this.mockMvc.perform(post("/api/owners/" + donoId + "/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    // @verifica: CT-068
    @Test
    @DisplayName("RN-022 · exclusao do pet remove as vacinas antes do pet")
    void excluirPetRemoveAsVacinasAntesDoPet() throws Exception {
        int donoId = criarDono();
        int petId = criarPet(donoId);
        String validade = LocalDate.now().plusDays(5).toString();
        String corpo = """
            {"nome":"VacinaDaExclusao","validade":"%s"}
            """.formatted(validade);

        try {
            this.mockMvc.perform(post("/api/pets/" + petId + "/vacinas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(corpo))
                .andExpect(status().isCreated());

            this.mockMvc.perform(get("/api/vacinas/a-vencer")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("VacinaDaExclusao")));

            this.mockMvc.perform(delete("/api/pets/" + petId)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            this.mockMvc.perform(get("/api/vacinas/a-vencer")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("VacinaDaExclusao"))));
        } finally {
            this.mockMvc.perform(delete("/api/owners/" + donoId)
                .accept(MediaType.APPLICATION_JSON));
        }
    }
}
