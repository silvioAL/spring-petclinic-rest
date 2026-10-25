package org.springframework.samples.petclinic.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.samples.petclinic.model.Vaccine;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Vaccine repository on plain JDBC. It does not depend on the selected repository profile
 * (jpa, jdbc or spring-data-jpa) and takes part in the transaction of the caller.
 */
@Repository
public class JdbcTemplateVaccineRepository implements VaccineRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertVaccine;

    public JdbcTemplateVaccineRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertVaccine = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("vaccines")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public void save(Vaccine vaccine) throws DataAccessException {
        Map<String, Object> values = new HashMap<>();
        values.put("pet_id", vaccine.getPetId());
        values.put("name", vaccine.getName());
        values.put("normalized_name", vaccine.getNormalizedName());
        values.put("application_date", Date.valueOf(vaccine.getApplicationDate()));
        values.put("expiration_date", Date.valueOf(vaccine.getExpirationDate()));
        Number key = this.insertVaccine.executeAndReturnKey(values);
        vaccine.setId(key.intValue());
    }

    @Override
    public void deleteByPetId(int petId) throws DataAccessException {
        this.jdbcTemplate.update("DELETE FROM vaccines WHERE pet_id = ?", petId);
    }

    @Override
    public void deleteByOwnerId(int ownerId) throws DataAccessException {
        this.jdbcTemplate.update(
            "DELETE FROM vaccines WHERE pet_id IN (SELECT id FROM pets WHERE owner_id = ?)", ownerId);
    }
}
