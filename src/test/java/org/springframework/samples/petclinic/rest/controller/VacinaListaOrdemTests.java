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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lista de vacinas a vencer: ordenada por validade crescente (as mais vencidas primeiro) e, no empate de
 * validade, pelo nome do pet em ordem alfabetica sem diferenciar maiusculas (SPEC-002, C4).
 * Usa o servico real (sem mock): registra a mesma vacina em tres pets e consulta a lista.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaListaOrdemTests {

    private static final String NOME_DA_VACINA = "VacinaOrdemCT087";

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-087
    @Test
    @DisplayName("RN-035 · lista ordenada por validade crescente e, no empate, pelo nome do pet sem diferenciar maiusculas")
    void listaOrdenadaPorValidadeCrescenteEPorNomeDoPetNoEmpate() throws Exception {
        // pets 1, 2 e 3 da massa de dados do petclinic, com nomes diferentes
        String nomePet1 = nomeDoPet(1);
        String nomePet2 = nomeDoPet(2);
        String nomePet3 = nomeDoPet(3);

        String maisAntiga = LocalDate.now().minusDays(10).toString();
        String empate = LocalDate.now().minusDays(2).toString();

        // o pet 3 tem a validade mais antiga; os pets 1 e 2 empatam na validade
        registrar(3, maisAntiga);
        registrar(2, empate);
        registrar(1, empate);

        String resposta = this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<String> nomesNaLista = JsonPath.read(resposta,
            "$..[?(@.vacinaNome == '" + NOME_DA_VACINA + "')].petNome");

        List<String> empatados = new ArrayList<>(List.of(nomePet1, nomePet2));
        empatados.sort(String.CASE_INSENSITIVE_ORDER);
        List<String> esperado = new ArrayList<>();
        esperado.add(nomePet3);
        esperado.addAll(empatados);

        assertEquals(esperado, nomesNaLista,
            "a validade mais antiga vem primeiro e o empate de validade segue o nome do pet");

        List<String> validades = JsonPath.read(resposta,
            "$..[?(@.vacinaNome == '" + NOME_DA_VACINA + "')].validade");
        assertEquals(List.of(maisAntiga, empate, empate), validades,
            "as validades devem estar em ordem crescente");
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

    private String nomeDoPet(int petId) throws Exception {
        String pet = this.mockMvc.perform(get("/api/pets/" + petId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(pet, "$.name");
    }
}
