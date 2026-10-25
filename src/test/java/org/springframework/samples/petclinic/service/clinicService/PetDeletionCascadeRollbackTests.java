package org.springframework.samples.petclinic.service.clinicService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Falha no meio da cascata de exclusao do pet: nada e excluido (RN-022), tudo na mesma transacao.
 * A falha e provocada por um gatilho no banco que recusa a exclusao do proprio pet, que e o
 * ultimo passo da cascata (vacinas, visitas, pet). O teste NAO e transacional: a transacao que
 * precisa desfazer e a da exclusao. A limpeza no fim devolve a massa do banco de teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
class PetDeletionCascadeRollbackTests {

    private static final int PET_WITH_VISITS = 7;
    private static final String TRIGGER = "fail_pet_delete_ct017";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    // @verifica: CT-017
    @Test
    @DisplayName("RN-022 · falha no meio da cascata nao exclui nada do pet")
    void failureInTheMiddleOfTheCascadeDeletesNothing() throws Exception {
        LocalDate today = LocalDate.now();
        String body = String.format(
            "{\"name\":\"V10\",\"applicationDate\":\"%s\",\"expirationDate\":\"%s\"}",
            today.minusDays(10), today.plusDays(355));

        try {
            mockMvc.perform(post("/api/pets/{petId}/vaccines", PET_WITH_VISITS)
                    .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
            mockMvc.perform(post("/api/pets/{petId}/vaccines", PET_WITH_VISITS)
                    .contentType(MediaType.APPLICATION_JSON).content(body.replace("V10", "v-10")))
                .andExpect(status().isCreated());

            int visitsBefore = count("visits", "pet_id", PET_WITH_VISITS);
            int vaccinesBefore = count("vaccines", "pet_id", PET_WITH_VISITS);
            assertThat(visitsBefore).isGreaterThan(0);
            assertThat(vaccinesBefore).isEqualTo(2);

            // a exclusao do proprio pet passa a falhar, depois de vacinas e visitas
            jdbcTemplate.execute("CREATE TRIGGER " + TRIGGER + " BEFORE DELETE ON pets "
                + "REFERENCING OLD ROW AS old FOR EACH ROW "
                + "BEGIN ATOMIC SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'falha provocada pelo teste'; END");

            try {
                mockMvc.perform(delete("/api/pets/{petId}", PET_WITH_VISITS));
            } catch (Exception expected) {
                // a falha pode subir como excecao ou virar 500: o que importa e o estado do banco
            }

            assertThat(count("vaccines", "pet_id", PET_WITH_VISITS)).isEqualTo(vaccinesBefore);
            assertThat(count("visits", "pet_id", PET_WITH_VISITS)).isEqualTo(visitsBefore);
            assertThat(count("pets", "id", PET_WITH_VISITS)).isEqualTo(1);
        } finally {
            jdbcTemplate.execute("DROP TRIGGER " + TRIGGER + " IF EXISTS");
            try {
                jdbcTemplate.update("delete from vaccines where pet_id = ?", PET_WITH_VISITS);
            } catch (Exception ignored) {
                // a tabela de vacinas ainda nao existe enquanto a implementacao nao chega
            }
        }
    }

    private int count(String table, String column, int value) {
        Integer total = jdbcTemplate.queryForObject(
            "select count(*) from " + table + " where " + column + " = ?", Integer.class, value);
        return total == null ? 0 : total;
    }
}
