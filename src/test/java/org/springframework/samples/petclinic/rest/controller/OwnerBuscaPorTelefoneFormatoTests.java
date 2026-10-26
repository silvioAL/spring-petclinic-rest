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
 * Busca de donos por telefone: 200 com a lista no mesmo formato de hoje
 * (SPEC-001, C2 e C4; RN-008, RN-021, RN-025).
 * O teste localiza por reflexao a consulta por telefone do ClinicService (nome
 * contendo "Telephone" ou "Telefone"), porque o nome ainda nao existe: sem ela
 * nada e devolvido e a listagem responde 404, ou seja, o teste falha ate a
 * implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerBuscaPorTelefoneFormatoTests {

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

        Owner owner = new Owner();
        owner.setId(7);
        owner.setFirstName("Eduardo");
        owner.setLastName("Rodriquez");
        owner.setAddress("2693 Commerce St.");
        owner.setCity("McFarland");
        owner.setTelephone("6085558763");
        List<Owner> donos = List.of(owner);

        for (Method m : ClinicService.class.getMethods()) {
            String nome = m.getName();
            if ((nome.contains("Telephone") || nome.contains("Telefone"))
                && java.util.Collection.class.isAssignableFrom(m.getReturnType())) {
                // O stubbing (doReturn...when) vem primeiro; os matchers so sao
                // criados logo antes da invocacao do metodo no mock, nesta ordem.
                Object stub = Mockito.doReturn(donos).when(this.clinicService);
                Class<?>[] tipos = m.getParameterTypes();
                Object[] args = new Object[tipos.length];
                for (int i = 0; i < args.length; i++) {
                    args[i] = ArgumentMatchers.any(tipos[i]);
                }
                m.invoke(stub, args);
            }
        }
    }

    // @verifica: CT-120
    @Test
    @DisplayName("RN-008 · busca por telefone devolve 200 com o dono no formato de hoje")
    void buscaPorTelefoneDevolveDonoNoFormatoDeHoje() throws Exception {
        this.mockMvc.perform(get("/api/owners?telefone=6085558763")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(7))
            .andExpect(jsonPath("$[0].firstName").value("Eduardo"))
            .andExpect(jsonPath("$[0].lastName").value("Rodriquez"))
            .andExpect(jsonPath("$[0].address").value("2693 Commerce St."))
            .andExpect(jsonPath("$[0].city").value("McFarland"))
            .andExpect(jsonPath("$[0].telephone").value("6085558763"))
            .andExpect(jsonPath("$[0].pets").isArray());
    }
}
