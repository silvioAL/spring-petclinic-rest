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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Listagem de donos sem o parametro telefone: identica a de hoje, na ordem do
 * identificador, sem acionar a busca por telefone (SPEC-001, C1; RN-021, RN-025).
 * O teste localiza por reflexao a consulta por telefone do ClinicService (nome
 * contendo "Telephone" ou "Telefone"), porque o nome ainda nao existe: sem ela
 * o teste falha, ou seja, falha ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerListagemSemTelefoneTests {

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

    private Owner dono(int id, String nome, String sobrenome, String telefone) {
        Owner owner = new Owner();
        owner.setId(id);
        owner.setFirstName(nome);
        owner.setLastName(sobrenome);
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone(telefone);
        return owner;
    }

    private Method consultaPorTelefone() {
        for (Method m : ClinicService.class.getMethods()) {
            String nome = m.getName();
            if ((nome.contains("Telephone") || nome.contains("Telefone"))
                && java.util.Collection.class.isAssignableFrom(m.getReturnType())) {
                return m;
            }
        }
        return null;
    }

    // @verifica: CT-122
    @Test
    @DisplayName("RN-025 · listagem sem telefone e identica a de hoje e ordenada pelo identificador")
    void listagemSemTelefoneMantemOrdemPorIdentificadorEnaoUsaBuscaPorTelefone() throws Exception {
        Method porTelefone = consultaPorTelefone();
        assertNotNull(porTelefone,
            "o ClinicService deve ter a consulta de donos por telefone (SPEC-001)");

        List<Owner> donos = List.of(
            dono(1, "George", "Franklin", "6085551023"),
            dono(2, "Betty", "Davis", "6085551749"),
            dono(3, "Eduardo", "Rodriquez", "6085558763"));
        Mockito.doReturn(donos).when(this.clinicService).findAllOwners();

        this.mockMvc.perform(get("/api/owners")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[2].id").value(3))
            .andExpect(jsonPath("$[0].firstName").value("George"))
            .andExpect(jsonPath("$[0].lastName").value("Franklin"))
            .andExpect(jsonPath("$[0].telephone").value("6085551023"))
            .andExpect(jsonPath("$[0].pets").isArray());

        Mockito.verify(this.clinicService).findAllOwners();

        Object[] args = new Object[porTelefone.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            args[i] = ArgumentMatchers.any();
        }
        porTelefone.invoke(Mockito.verify(this.clinicService, Mockito.never()), args);
    }
}
