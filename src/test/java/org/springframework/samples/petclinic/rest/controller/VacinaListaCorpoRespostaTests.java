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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lista de vacinas a vencer: a resposta 200 traz, em cada item, petNome, vacinaNome, validade e
 * donoTelefone, e traz o total ao lado dos itens (SPEC-002, C3 e C5).
 * Usa o servico real (sem mock): registra uma vacina vencida e confere o corpo da resposta.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaListaCorpoRespostaTests {

    private static final String NOME_DA_VACINA = "VacinaCorpoCT094";

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-094
    @Test
    @DisplayName("RF-007 · resposta 200 traz petNome, vacinaNome, validade, donoTelefone e total")
    void respostaTrazPetNomeVacinaNomeValidadeDonoTelefoneETotal() throws Exception {
        String validade = LocalDate.now().minusDays(3500).toString();
        registrar(1, validade);

        String corpo = this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .param("page", "0")
                .param("size", "100")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Number total = JsonPath.read(corpo, "$.total");
        assertNotNull(total, "a resposta traz o total ao lado dos itens");
        assertTrue(total.longValue() >= 1, "o total conta a vacina registrada");

        List<Map<String, Object>> itens =
            JsonPath.read(corpo, "$.itens[?(@.vacinaNome=='" + NOME_DA_VACINA + "')]");
        assertFalse(itens.isEmpty(), "a vacina vencida registrada consta da lista");

        Map<String, Object> item = itens.get(0);
        Object petNome = item.get("petNome");
        Object donoTelefone = item.get("donoTelefone");
        assertNotNull(petNome, "o item traz petNome");
        assertFalse(petNome.toString().isBlank(), "petNome nao e vazio");
        assertEquals(NOME_DA_VACINA, item.get("vacinaNome"), "o item traz vacinaNome");
        assertEquals(validade, item.get("validade"), "o item traz a validade em formato ISO");
        assertNotNull(donoTelefone, "o item traz donoTelefone");
        assertFalse(donoTelefone.toString().isBlank(), "donoTelefone nao e vazio");
    }

    private void registrar(int petId, String validade) throws Exception {
        String corpo = """
            {"nome":"%s","validade":"%s"}
            """.formatted(NOME_DA_VACINA, validade);
        this.mockMvc.perform(post("/api/pets/" + petId + "/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated());
    }
}
