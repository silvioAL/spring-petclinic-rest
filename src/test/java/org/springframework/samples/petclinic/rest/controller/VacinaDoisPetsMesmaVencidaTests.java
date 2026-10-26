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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lista de vacinas a vencer: dois pets de donos diferentes com a mesma vacina vencida aparecem os dois,
 * cada um com o telefone do seu dono (SPEC-002, C1 e C3).
 * Usa o servico real (sem mock): registra a vacina nos dois pets e consulta a lista.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaDoisPetsMesmaVencidaTests {

    private static final String NOME_DA_VACINA = "VacinaVencidaCT085";

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-085
    @Test
    @DisplayName("RN-035 · dois pets com a mesma vacina vencida aparecem cada um com o telefone do seu dono")
    void doisPetsComAMesmaVacinaVencidaApareceCadaUmComOTelefoneDoSeuDono() throws Exception {
        // pet 1 e dono 1; pet 3 e dono 2 (massa de dados do petclinic): donos diferentes
        String telefoneDono1 = telefoneDoDono(1);
        String telefoneDono2 = telefoneDoDono(2);
        assertTrue(!telefoneDono1.equals(telefoneDono2), "a massa deve ter donos com telefones diferentes");

        String vencida = LocalDate.now().minusDays(5).toString();
        String corpo = """
            {"nome":"%s","validade":"%s"}
            """.formatted(NOME_DA_VACINA, vencida);

        for (int petId : new int[]{1, 3}) {
            this.mockMvc.perform(post("/api/pets/" + petId + "/vacinas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(corpo))
                .andExpect(status().isCreated());
        }

        String resposta = this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        java.util.List<String> telefones = JsonPath.read(resposta,
            "$..[?(@.vacinaNome == '" + NOME_DA_VACINA + "')].donoTelefone");
        assertEquals(2, telefones.size(), "os dois pets devem aparecer com a vacina vencida");
        assertTrue(telefones.contains(telefoneDono1), "falta o telefone do dono do pet 1");
        assertTrue(telefones.contains(telefoneDono2), "falta o telefone do dono do pet 3");
    }

    private String telefoneDoDono(int donoId) throws Exception {
        String dono = this.mockMvc.perform(get("/api/owners/" + donoId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(dono, "$.telephone");
    }
}
