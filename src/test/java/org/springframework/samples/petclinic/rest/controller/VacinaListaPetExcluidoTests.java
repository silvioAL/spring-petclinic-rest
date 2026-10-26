package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.test.annotation.DirtiesContext;
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
 * Vacina de pet excluido deixa de constar da lista de vacinas a vencer (SPEC-002, RN-022, C7 da SPEC-001).
 * Registra uma vacina vencida para um pet, confere que ela consta da lista, exclui o pet
 * e confere que a lista nao a traz mais, sem erro na exclusao nem na consulta.
 * Contexto proprio (DirtiesContext) para nao depender da massa alterada por outros testes.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class VacinaListaPetExcluidoTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-100
    @Test
    @DisplayName("RN-022 · vacina de pet excluido deixa de constar da lista a vencer")
    void vacinaDePetExcluidoDeixaDeConstarDaLista() throws Exception {
        String validade = LocalDate.now().minusDays(1).toString();
        String corpo = """
            {"nome":"VacinaDoPetExcluidoCt100","validade":"%s"}
            """.formatted(validade);

        this.mockMvc.perform(post("/api/pets/2/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated());

        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("VacinaDoPetExcluidoCt100")));

        this.mockMvc.perform(delete("/api/pets/2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("VacinaDoPetExcluidoCt100"))));
    }
}
