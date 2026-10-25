package org.springframework.samples.petclinic.service.clinicService;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A exclusao do pet e a do dono, pelo servico de clinica, levam as vacinas junto na mesma
 * transacao (RN-022, RN-019). Cada metodo roda numa unica transacao, desfeita no fim: se as
 * vacinas sumirem antes do fim dela, foram excluidas dentro da transacao da propria exclusao.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
@Transactional
class VaccineDeletionSameTransactionTests {

    // dono da massa com dois animais (7 e 8), ambos com visitas
    private static final int OWNER_WITH_PETS = 6;
    private static final int FIRST_PET = 7;
    private static final int SECOND_PET = 8;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ClinicService clinicService;

    // @verifica: CT-024
    @Test
    @DisplayName("RN-022 · excluir o pet exclui as vacinas dele na mesma transacao")
    void deletingPetDeletesItsVaccinesInTheSameTransaction() throws Exception {
        registerVaccine(FIRST_PET, "V10");
        registerVaccine(FIRST_PET, "v-10");
        entityManager.flush();

        assertThat(count("vaccines", "pet_id = " + FIRST_PET)).isEqualTo(2);
        assertThat(count("visits", "pet_id = " + FIRST_PET)).isGreaterThan(0);

        Pet pet = this.clinicService.findPetById(FIRST_PET);
        this.clinicService.deletePet(pet);
        entityManager.flush();

        assertThat(count("vaccines", "pet_id = " + FIRST_PET)).isZero();
        assertThat(count("visits", "pet_id = " + FIRST_PET)).isZero();
        assertThat(count("pets", "id = " + FIRST_PET)).isZero();
    }

    // @verifica: CT-024
    @Test
    @DisplayName("RN-019 · excluir o dono exclui as vacinas dos animais dele na mesma transacao")
    void deletingOwnerDeletesPetVaccinesInTheSameTransaction() throws Exception {
        registerVaccine(FIRST_PET, "V10");
        registerVaccine(SECOND_PET, "V10");
        entityManager.flush();

        assertThat(count("vaccines", petsOfOwner())).isEqualTo(2);

        Owner owner = this.clinicService.findOwnerById(OWNER_WITH_PETS);
        this.clinicService.deleteOwner(owner);
        entityManager.flush();

        assertThat(count("vaccines", petsOfOwner())).isZero();
        assertThat(count("visits", petsOfOwner())).isZero();
        assertThat(count("pets", "owner_id = " + OWNER_WITH_PETS)).isZero();
        assertThat(count("owners", "id = " + OWNER_WITH_PETS)).isZero();
    }

    private void registerVaccine(int petId, String name) throws Exception {
        LocalDate today = LocalDate.now();
        String body = String.format(
            "{\"name\":\"%s\",\"applicationDate\":\"%s\",\"expirationDate\":\"%s\"}",
            name, today.minusDays(10), today.plusDays(355));
        mockMvc.perform(post("/api/pets/{petId}/vaccines", petId)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
    }

    private String petsOfOwner() {
        return "pet_id in (" + FIRST_PET + ", " + SECOND_PET + ")";
    }

    private int count(String table, String where) {
        Integer total = jdbcTemplate.queryForObject(
            "select count(*) from " + table + " where " + where, Integer.class);
        return total == null ? 0 : total;
    }
}
