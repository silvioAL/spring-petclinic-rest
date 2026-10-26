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
 * Contrato da chamada de busca por telefone: GET /api/owners com o parametro
 * telefone responde a lista de donos (SPEC-001, secao 2.1; C2 e C4; RN-008, RN-018).
 * Como nos demais testes da demanda, a consulta por telefone do ClinicService e
 * localizada por reflexao (nome contendo "Telephone" ou "Telefone"), porque o nome
 * ainda nao existe: sem ela nada e devolvido e a listagem responde 404, ou seja, o
 * teste falha ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerBuscaPorTelefoneChamadaTests {

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

        Owner primeiro = new Owner();
        primeiro.setId(3);
        primeiro.setFirstName("Eduardo");
        primeiro.setLastName("Rodriquez");
        primeiro.setAddress("2693 Commerce St.");
        primeiro.setCity("McFarland");
        primeiro.setTelephone("6085551023");

        Owner segundo = new Owner();
        segundo.setId(9);
        segundo.setFirstName("Maria");
        segundo.setLastName("Escobito");
        segundo.setAddress("345 Maple St.");
        segundo.setCity("Madison");
        segundo.setTelephone("6085551023");

        List<Owner> donos = List.of(primeiro, segundo);

        for (Method m : ClinicService.class.getMethods()) {
            String nome = m.getName();
            if ((nome.contains("Telephone") || nome.contains("Telefone"))
                && java.util.Collection.class.isAssignableFrom(m.getReturnType())) {
                Object[] args = new Object[m.getParameterCount()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = ArgumentMatchers.any();
                }
                m.invoke(Mockito.doReturn(donos).when(this.clinicService), args);
            }
        }
    }

    // @verifica: CT-135
    @Test
    @DisplayName("RN-008 · GET owners com o parametro telefone responde a lista de donos")
    void getOwnersComTelefoneRespondeListaDeDonos() throws Exception {
        this.mockMvc.perform(get("/api/owners?telefone=6085551023")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(3))
            .andExpect(jsonPath("$[0].telephone").value("6085551023"))
            .andExpect(jsonPath("$[1].id").value(9))
            .andExpect(jsonPath("$[1].telephone").value("6085551023"));
    }
}
