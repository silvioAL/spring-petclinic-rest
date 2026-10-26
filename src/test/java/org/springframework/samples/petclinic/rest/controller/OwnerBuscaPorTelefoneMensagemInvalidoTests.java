package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.controller.v1.OwnerRestControllerV1;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Busca de donos por telefone invalido: qualquer telefone que, sem a mascara,
 * nao tenha 8 ou 10 digitos (ou tenha caractere que nao seja digito) devolve 400
 * com mensagem que cita o tamanho esperado (SPEC-001, secao 2.1 e C6).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerBuscaPorTelefoneMensagemInvalidoTests {

    @Autowired
    private OwnerRestControllerV1 ownerRestControllerV1;

    @MockitoBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void initMockMvc() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerRestControllerV1)
            .setControllerAdvice(new ExceptionControllerAdvice())
            .build();
    }

    // @verifica: CT-121
    @Test
    @DisplayName("RN-008 · telefone invalido devolve 400 com mensagem citando 8 ou 10 digitos")
    @WithMockUser(roles = "OWNER_ADMIN")
    void telefoneInvalidoDevolve400ComMensagem() throws Exception {
        String[] invalidos = {"1", "6085551", "608555102", "(608) 555-102", "608.555.1023.4"};
        for (String telefone : invalidos) {
            this.mockMvc.perform(get("/api/owners")
                    .queryParam("telefone", telefone)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("8 ou 10")));
        }
    }

    // @verifica: CT-121
    @Test
    @DisplayName("RN-008 · telefone com caractere que nao e digito nem mascara devolve 400 com mensagem")
    @WithMockUser(roles = "OWNER_ADMIN")
    void telefoneComCaractereInvalidoDevolve400ComMensagem() throws Exception {
        this.mockMvc.perform(get("/api/owners")
                .queryParam("telefone", "60855a1023")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("8 ou 10")));
    }
}
