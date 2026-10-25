package org.springframework.samples.petclinic.service.clinicService;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exclusao em cascata do dono: animais, visitas e vacinas saem junto (RN-019), na mesma transacao.
 * Cada metodo roda na sua transacao, desfeita no fim (a massa do banco de teste nao muda).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
@Transactional
class OwnerDeletionCascadeVaccineTests {

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

    // @verifica: CT-016
    @Test
    @DisplayName("RN-019 · excluir o dono exclui os animais, as visitas e as vacinas dele")
    void deletingOwnerAlsoDeletesPetsVisitsAndVaccines() throws Exception {
        LocalDate today = LocalDate.now();
        String body = """
            {"name":"V10","applicationDate":"%s","expirationDate":"%s"}
            """.formatted(today.minusDays(10), today.plusDays(355));

        // cada animal do dono passa a ter uma vacina
        mockMvc.perform(post("/api/pets/{petId}/vaccines", FIRST_PET)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/api/pets/{petId}/vaccines", SECOND_PET)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
        entityManager.flush();

        assertThat(count("pets", "owner_id", "owner_id = " + OWNER_WITH_PETS)).isEqualTo(2);
        assertThat(count("visits", "pet_id", petsOfOwner())).isGreaterThan(0);
        assertThat(count("vaccines", "pet_id", petsOfOwner())).isEqualTo(2);

        mockMvc.perform(delete("/api/owners/{ownerId}", OWNER_WITH_PETS))
            .andExpect(status().isNoContent());
        entityManager.flush();

        assertThat(count("vaccines", "pet_id", petsOfOwner())).isZero();
        assertThat(count("visits", "pet_id", petsOfOwner())).isZero();
        assertThat(count("pets", "owner_id", "owner_id = " + OWNER_WITH_PETS)).isZero();
        assertThat(count("owners", "id", "id = " + OWNER_WITH_PETS)).isZero();
    }

    private String petsOfOwner() {
        return "pet_id in (" + FIRST_PET + ", " + SECOND_PET + ")";
    }

    private int count(String table, String column, String where) {
        Integer total = jdbcTemplate.queryForObject(
            "select count(*) from " + table + " where " + where, Integer.class);
        return total == null ? 0 : total;
    }
}
