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
 * Exclusao do pet leva as vacinas dele, antes do pet (SPEC-001, C7).
 * Usa o servico real (sem mock): registra uma vacina, exclui o pet e confere que
 * a vacina nao consta mais da lista de vacinas a vencer e que a exclusao nao falha
 * por causa da chave estrangeira.
 * Descarta o contexto ao terminar (DirtiesContext), para que a exclusao do pet 3
 * nao apareca para as outras classes, que usam o mesmo pet.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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

    // @verifica: CT-068
    @Test
    @DisplayName("RN-022 · exclusao do pet remove as vacinas antes do pet")
    void excluirPetRemoveAsVacinasAntesDoPet() throws Exception {
        String validade = LocalDate.now().plusDays(5).toString();
        String corpo = """
            {"nome":"VacinaDaExclusao","validade":"%s"}
            """.formatted(validade);

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated());

        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("VacinaDaExclusao")));

        this.mockMvc.perform(delete("/api/pets/3")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("VacinaDaExclusao"))));
    }
}
