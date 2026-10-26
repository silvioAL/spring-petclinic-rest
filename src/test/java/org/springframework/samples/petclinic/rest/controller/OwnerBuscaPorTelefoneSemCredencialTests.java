package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Busca de donos por telefone com a seguranca ligada e sem credencial: a mesma
 * resposta 401 da busca de donos de hoje (SPEC-001, C7; RN-001 e RN-004).
 * Usa a cadeia de filtros do Spring Security (src/test/resources liga
 * petclinic.security.enable=true).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerBuscaPorTelefoneSemCredencialTests {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void initMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    // @verifica: CT-116
    @Test
    @DisplayName("RN-004 · busca por telefone sem credencial devolve 401")
    void buscaPorTelefoneSemCredencialDevolve401() throws Exception {
        this.mockMvc.perform(get("/api/owners?telefone=6085551023")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
        // sem credencial a busca nem chega ao servico de donos
        verifyNoInteractions(clinicService);
    }
}
