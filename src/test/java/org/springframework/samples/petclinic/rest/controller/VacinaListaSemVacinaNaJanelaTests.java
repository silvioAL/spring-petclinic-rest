package org.springframework.samples.petclinic.rest.controller;

import com.jayway.jsonpath.JsonPath;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lista de vacinas a vencer: sem nenhuma vacina na janela, a resposta e 200 com pagina vazia e total 0
 * (SPEC-002, caso de teste "Lista sem nenhuma vacina na janela").
 * Parte de um contexto novo e sem vacinas registradas (a massa do petclinic nao tem vacinas).
 * O teste falha ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = {"petclinic.security.enable=false", "petclinic.teste.lista-vazia-ct086=true"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class VacinaListaSemVacinaNaJanelaTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-086
    @Test
    @DisplayName("RN-035 · lista sem vacina na janela devolve pagina vazia e total 0")
    void listaSemVacinaNaJanelaDevolvePaginaVaziaETotalZero() throws Exception {
        String resposta = this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<Object> itens = JsonPath.read(resposta, "$.itens");
        assertTrue(itens.isEmpty(), "a pagina deve vir vazia quando nao ha vacina na janela");
        Number total = JsonPath.read(resposta, "$.total");
        assertEquals(0, total.intValue(), "o total deve ser 0 quando nao ha vacina na janela");
    }
}
