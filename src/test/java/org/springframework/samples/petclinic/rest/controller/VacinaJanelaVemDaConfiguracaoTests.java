package org.springframework.samples.petclinic.rest.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lista de vacinas a vencer: a janela de vencimento vem da configuracao e nao do codigo
 * (SPEC-002, RN-036, C7). Com a janela configurada para 45 dias, a vacina que vence em
 * 40 dias aparece na lista; com a janela fixa em 30 dias ela ficaria de fora.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = {
    "petclinic.security.enable=false",
    "petclinic.vacinas.janela-dias=45"
})
class VacinaJanelaVemDaConfiguracaoTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // @verifica: CT-099
    @Test
    @DisplayName("RN-036 · janela configurada para 45 dias inclui a vacina que vence em 40 dias")
    void janelaDe45DiasIncluiVacinaQueVenceEm40Dias() throws Exception {
        String validade = LocalDate.now().plusDays(40).toString();
        String corpo = "{\"nome\":\"VacinaJanela45\",\"validade\":\"" + validade + "\"}";

        this.mockMvc.perform(post("/api/pets/1/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpo))
            .andExpect(status().isCreated());

        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .param("page", "0")
                .param("size", "50")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itens[?(@.vacinaNome=='VacinaJanela45')]").isNotEmpty());
    }
}
