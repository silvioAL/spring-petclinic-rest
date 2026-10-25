package org.springframework.samples.petclinic.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Vaccine;

/**
 * Output port for the <code>Vaccine</code> records of a pet (SPEC-001).
 */
public interface VaccineRepository {

    /**
     * Inserts the vaccine and fills its identifier.
     */
    void save(Vaccine vaccine) throws DataAccessException;

    /**
     * Deletes every vaccine of the pet.
     */
    void deleteByPetId(int petId) throws DataAccessException;

    /**
     * Deletes every vaccine of every pet of the owner.
     */
    void deleteByOwnerId(int ownerId) throws DataAccessException;
}
