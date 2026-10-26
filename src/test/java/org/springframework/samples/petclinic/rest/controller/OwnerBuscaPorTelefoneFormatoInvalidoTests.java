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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Formato invalido do telefone na busca de donos: o servidor recusa com 400 e
 * devolve no corpo a mensagem que a tela exibe junto ao campo
 * (SPEC-001, secao 2.1 e C6; SPEC-002, secao 2.1 e C6).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerBuscaPorTelefoneFormatoInvalidoTests {

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

    // @verifica: CT-136
    @Test
    @DisplayName("RN-008 · formato invalido devolve 400 com a mensagem que a tela exibe")
    @WithMockUser(roles = "OWNER_ADMIN")
    void formatoInvalidoDevolve400ComMensagemParaATela() throws Exception {
        String[] invalidos = {"123456789", "12345678901"};
        for (String telefone : invalidos) {
            this.mockMvc.perform(get("/api/owners")
                    .queryParam("telefone", telefone)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(emptyOrNullString())))
                .andExpect(content().string(containsString("8 ou 10")));
        }
    }

    // @verifica: CT-136
    @Test
    @DisplayName("RN-008 · telefone com letras devolve 400 com a mensagem que a tela exibe")
    @WithMockUser(roles = "OWNER_ADMIN")
    void telefoneComLetrasDevolve400ComMensagemParaATela() throws Exception {
        this.mockMvc.perform(get("/api/owners")
                .queryParam("telefone", "60855a1023")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(not(emptyOrNullString())))
            .andExpect(content().string(containsString("8 ou 10")));
    }
}
