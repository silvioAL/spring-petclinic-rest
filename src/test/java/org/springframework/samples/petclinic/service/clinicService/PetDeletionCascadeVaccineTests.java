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
 * Exclusao em cascata do pet: visitas e vacinas saem junto (RN-022), na mesma transacao.
 * Cada metodo roda na sua transacao, desfeita no fim (a massa do banco de teste nao muda).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
@Transactional
class PetDeletionCascadeVaccineTests {

    private static final int PET_WITH_VISITS = 7;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EntityManager entityManager;

    // @verifica: CT-015
    @Test
    @DisplayName("RN-022 · excluir o pet exclui as visitas e as vacinas dele")
    void deletingPetAlsoDeletesItsVisitsAndVaccines() throws Exception {
        LocalDate today = LocalDate.now();
        String body = String.format(
            "{\"name\":\"V10\",\"applicationDate\":\"%s\",\"expirationDate\":\"%s\"}",
            today.minusDays(10), today.plusDays(355));

        // o pet de partida tem visitas e passa a ter vacinas (duas, uma delas revacinacao)
        mockMvc.perform(post("/api/pets/{petId}/vaccines", PET_WITH_VISITS)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/api/pets/{petId}/vaccines", PET_WITH_VISITS)
                .contentType(MediaType.APPLICATION_JSON).content(body.replace("V10", "v-10")))
            .andExpect(status().isCreated());
        entityManager.flush();

        assertThat(count("visits", PET_WITH_VISITS)).isGreaterThan(0);
        assertThat(count("vaccines", PET_WITH_VISITS)).isEqualTo(2);

        mockMvc.perform(delete("/api/pets/{petId}", PET_WITH_VISITS))
            .andExpect(status().isNoContent());
        entityManager.flush();

        assertThat(count("vaccines", PET_WITH_VISITS)).isZero();
        assertThat(count("visits", PET_WITH_VISITS)).isZero();
        assertThat(count("pets", "id", PET_WITH_VISITS)).isZero();
    }

    private int count(String table, int petId) {
        return count(table, "pet_id", petId);
    }

    private int count(String table, String column, int value) {
        Integer total = jdbcTemplate.queryForObject(
            "select count(*) from " + table + " where " + column + " = ?", Integer.class, value);
        return total == null ? 0 : total;
    }
}
