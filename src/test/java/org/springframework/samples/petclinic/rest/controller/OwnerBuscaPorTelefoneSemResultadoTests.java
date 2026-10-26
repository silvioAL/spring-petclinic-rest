package org.springframework.samples.petclinic.rest.controller;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.controller.v1.OwnerRestControllerV1;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Busca por telefone sem nenhum dono: a API responde 200 com lista vazia, nao erro
 * (SPEC-001, secao 2.1; C5; RN-008). E a lista vazia que permite a tela mostrar o
 * aviso "Nenhum dono tem esse telefone" (SPEC-002, C4). Como nos demais testes da
 * demanda, a consulta por telefone do ClinicService e localizada por reflexao (nome
 * contendo "Telephone" ou "Telefone"), porque o nome ainda nao existe: sem ela a
 * listagem atual responde 404 para lista vazia, ou seja, o teste falha ate a
 * implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerBuscaPorTelefoneSemResultadoTests {

    @Autowired
    private OwnerRestControllerV1 ownerRestControllerV1;

    @MockitoBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void initMockMvc() throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerRestControllerV1)
            .setControllerAdvice(new ExceptionControllerAdvice())
            .build();

        List<Owner> nenhum = List.of();

        for (Method m : ClinicService.class.getMethods()) {
            String nome = m.getName();
            if ((nome.contains("Telephone") || nome.contains("Telefone"))
                && java.util.Collection.class.isAssignableFrom(m.getReturnType())) {
                // O stubbing (doReturn...when) vem primeiro; os matchers so sao
                // criados logo antes da invocacao do metodo no mock, nesta ordem.
                Object stub = Mockito.doReturn(nenhum).when(this.clinicService);
                Class<?>[] tipos = m.getParameterTypes();
                Object[] args = new Object[tipos.length];
                for (int i = 0; i < args.length; i++) {
                    args[i] = ArgumentMatchers.any(tipos[i]);
                }
                m.invoke(stub, args);
            }
        }
    }

    // @verifica: CT-137
    @Test
    @DisplayName("RN-008 · telefone que nenhum dono tem responde 200 com lista vazia")
    void getOwnersComTelefoneSemDonoRespondeListaVazia() throws Exception {
        this.mockMvc.perform(get("/api/owners?telefone=6085550000")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(0));
    }
}
