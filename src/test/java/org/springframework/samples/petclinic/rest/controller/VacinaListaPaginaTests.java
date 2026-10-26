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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lista de vacinas a vencer: a pagina pedida (page e size) traz so os seus itens e o total geral
 * de vacinas na lista, independente do tamanho da pagina (SPEC-002, C5).
 * Usa o servico real (sem mock): registra tres vacinas vencidas e consulta duas paginas de tamanho 2.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaListaPaginaTests {

    private static final String NOME_DA_VACINA = "VacinaPaginaCT088";

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-088
    @Test
    @DisplayName("RN-035 · pagina pedida traz seus itens e o total geral")
    void paginaPedidaTrazSeusItensEOTotalGeral() throws Exception {
        // tres vacinas vencidas ha muito tempo: ficam sempre na lista e vem antes das demais (validade crescente)
        registrar(1, LocalDate.now().minusDays(3000).toString());
        registrar(2, LocalDate.now().minusDays(2999).toString());
        registrar(3, LocalDate.now().minusDays(2998).toString());

        String primeira = consultar(0, 2);
        List<Object> itensPrimeira = JsonPath.read(primeira, "$.itens");
        Number totalPrimeira = JsonPath.read(primeira, "$.total");

        assertEquals(2, itensPrimeira.size(), "a primeira pagina de tamanho 2 traz exatamente 2 itens");
        assertTrue(totalPrimeira.longValue() >= 3,
            "o total e geral (todas as vacinas da lista), nao o tamanho da pagina");

        String segunda = consultar(1, 2);
        List<Object> itensSegunda = JsonPath.read(segunda, "$.itens");
        Number totalSegunda = JsonPath.read(segunda, "$.total");

        assertTrue(itensSegunda.size() >= 1 && itensSegunda.size() <= 2,
            "a segunda pagina traz o restante dos itens, no maximo o tamanho pedido");
        assertEquals(totalPrimeira.longValue(), totalSegunda.longValue(),
            "o total geral e o mesmo em qualquer pagina");

        // a segunda pagina continua a primeira: a terceira vacina mais antiga abre a pagina 1
        List<String> validadesSegunda = JsonPath.read(segunda, "$.itens[*].validade");
        assertEquals(LocalDate.now().minusDays(2998).toString(), validadesSegunda.get(0),
            "a pagina 1 comeca onde a pagina 0 terminou");
    }

    private String consultar(int page, int size) throws Exception {
        return this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
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
