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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lista de vacinas a vencer: o mesmo pet com duas vacinas de nomes diferentes, uma vencida e outra em dia,
 * so a vencida aparece (SPEC-002, C1 e C2; a vigencia e decidida por par pet e nome da vacina).
 * Usa o servico real (sem mock): registra as duas vacinas e consulta a lista.
 * Os testes falham ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = "petclinic.security.enable=false")
class VacinaVencidaComOutraEmDiaTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    void iniciar() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-084
    @Test
    @DisplayName("RN-035 · so a vacina vencida aparece quando o pet tem outra vacina de nome diferente em dia")
    void soAVencidaApareceQuandoOutraVacinaDoPetEstaEmDia() throws Exception {
        String vencida = LocalDate.now().minusDays(5).toString();
        String emDia = LocalDate.now().plusDays(365).toString();
        String corpoVencida = """
            {"nome":"VacinaVencidaCT084","validade":"%s"}
            """.formatted(vencida);
        String corpoEmDia = """
            {"nome":"VacinaEmDiaCT084","validade":"%s"}
            """.formatted(emDia);

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpoVencida))
            .andExpect(status().isCreated());

        this.mockMvc.perform(post("/api/pets/3/vacinas")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(corpoEmDia))
            .andExpect(status().isCreated());

        this.mockMvc.perform(get("/api/vacinas/a-vencer")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("VacinaVencidaCT084")))
            .andExpect(content().string(not(containsString("VacinaEmDiaCT084"))));
    }
}
