package org.springframework.samples.petclinic.rest.controller;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.controller.v2.OwnerRestControllerV2;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
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
 * Listagem paginada de donos com o parametro telefone: mantem a ordem pelo
 * identificador e devolve o total junto com a pagina (SPEC-001, secao 2.1;
 * RN-021, RN-025). O teste localiza por reflexao a consulta paginada por
 * telefone do ClinicService (nome contendo "Telephone" ou "Telefone" e retorno
 * Page), porque o nome ainda nao existe: sem ela o teste falha, ou seja, falha
 * ate a implementacao existir.
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerListagemPaginadaComTelefoneTests {

    @Autowired
    private OwnerRestControllerV2 ownerRestControllerV2;

    @MockitoBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void initMockMvc() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerRestControllerV2)
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

    private Method consultaPaginadaPorTelefone() {
        for (Method m : ClinicService.class.getMethods()) {
            String nome = m.getName();
            if ((nome.contains("Telephone") || nome.contains("Telefone"))
                && Page.class.isAssignableFrom(m.getReturnType())) {
                return m;
            }
        }
        return null;
    }

    private Object[] argumentos(Method metodo, String telefone, Pageable pageable) {
        Class<?>[] tipos = metodo.getParameterTypes();
        Object[] args = new Object[tipos.length];
        for (int i = 0; i < tipos.length; i++) {
            if (Pageable.class.isAssignableFrom(tipos[i])) {
                args[i] = pageable;
            } else if (tipos[i] == String.class) {
                args[i] = telefone;
            }
        }
        return args;
    }

    // @verifica: CT-123
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("RN-021 · listagem paginada com telefone mantem a ordem pelo identificador e o total")
    void listagemPaginadaComTelefoneMantemOrdemPorIdentificadorETotal() throws Exception {
        Method porTelefone = consultaPaginadaPorTelefone();
        assertNotNull(porTelefone,
            "o ClinicService deve ter a consulta paginada de donos por telefone (SPEC-001)");

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("id"));
        List<Owner> donos = List.of(
            dono(1, "George", "Franklin", "6085551023"),
            dono(2, "Betty", "Davis", "6085551023"));
        Page<Owner> pagina = new PageImpl<>(donos, pageRequest, 3);

        Object stub = Mockito.doReturn(pagina).when(this.clinicService);
        porTelefone.invoke(stub, argumentos(porTelefone, "6085551023", pageRequest));

        this.mockMvc.perform(get("/api/v2/owners?telefone=6085551023&page=0&size=2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.content[0].telephone").value("6085551023"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2));
    }
}
