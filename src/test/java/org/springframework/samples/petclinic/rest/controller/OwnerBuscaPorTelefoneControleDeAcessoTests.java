package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A busca de donos por telefone herda o controle de acesso da busca de donos de
 * hoje, sem permissao nova (SPEC-001, C7; RN-001 e RN-004): o papel OWNER_ADMIN
 * passa e o papel sem permissao recebe 403. Usa a cadeia de filtros do Spring
 * Security (src/test/resources liga petclinic.security.enable=true).
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerBuscaPorTelefoneControleDeAcessoTests {

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

    // @verifica: CT-139
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("RN-001 · busca por telefone com o papel da busca de donos e atendida pela busca por telefone")
    void buscaPorTelefoneComPapelDaBuscaDeDonosEAtendida() throws Exception {
        this.mockMvc.perform(get("/api/owners?telefone=6085551023")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        // o parametro telefone nao pode cair na listagem completa de hoje
        verify(clinicService, never()).findAllOwners();
    }

    // @verifica: CT-139
    @Test
    @WithMockUser(roles = "VET_ADMIN")
    @DisplayName("RN-004 · busca por telefone com papel sem permissao de donos devolve 403 como a busca de donos")
    void buscaPorTelefoneComPapelSemPermissaoDevolve403() throws Exception {
        this.mockMvc.perform(get("/api/owners?telefone=6085551023")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
        // a mesma recusa da busca de donos: nem chega ao servico de donos
        verifyNoInteractions(clinicService);
    }
}
