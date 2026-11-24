package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Resumo de visitas do tutor (SPEC-001, 2.1 e 2.2): o calculo, sem Spring.
 * Os testes falham ate a implementacao existir.
 */
class ResumoDeVisitasTest {

    // @verifica: CT-901
    @Test
    @DisplayName("RN-012 · o resumo soma as visitas dos animais do tutor e traz a data da mais recente")
    void somaAsVisitasDosAnimaisETrazAMaisRecente() {
        Owner tutor = tutor(6);
        tutor.addPet(animal(7, "Samantha", LocalDate.of(2012, 6, 8), LocalDate.of(2013, 1, 4)));
        tutor.addPet(animal(8, "Max", LocalDate.of(2011, 3, 2)));
        tutor.addPet(animal(9, "basil"));

        ResumoDeVisitas resumo = new ResumoDeVisitas(tutor);

        assertEquals(6, resumo.getOwnerId());
        assertEquals(3, resumo.getTotal());
        assertEquals(LocalDate.of(2013, 1, 4), resumo.getUltimaVisita());
        assertEquals(3, resumo.getPorPet().size());
        assertPet(resumo.getPorPet().get(0), 9, "basil", 0);
        assertPet(resumo.getPorPet().get(1), 8, "Max", 1);
        assertPet(resumo.getPorPet().get(2), 7, "Samantha", 2);
    }

    // @verifica: CT-902
    @Test
    @DisplayName("RN-009 · tutor sem visita tem total 0, ultima visita nula e os animais, pelo nome, com 0")
    void tutorSemVisitaTrazOsAnimaisComZero() {
        Owner tutor = tutor(1);
        tutor.addPet(animal(2, "Rosy"));
        tutor.addPet(animal(1, "Leo"));

        ResumoDeVisitas resumo = new ResumoDeVisitas(tutor);

        assertEquals(1, resumo.getOwnerId());
        assertEquals(0, resumo.getTotal());
        assertNull(resumo.getUltimaVisita());
        assertEquals(2, resumo.getPorPet().size());
        assertPet(resumo.getPorPet().get(0), 1, "Leo", 0);
        assertPet(resumo.getPorPet().get(1), 2, "Rosy", 0);
    }

    private static void assertPet(ResumoDeVisitas.VisitasDoPet pet, int id, String nome, int visitas) {
        assertEquals(id, pet.getPetId());
        assertEquals(nome, pet.getNome());
        assertEquals(visitas, pet.getVisitas());
    }

    private static Owner tutor(int id) {
        Owner tutor = new Owner();
        tutor.setId(id);
        return tutor;
    }

    private static Pet animal(int id, String nome, LocalDate... datasDasVisitas) {
        Pet animal = new Pet();
        animal.setId(id);
        animal.setName(nome);
        for (LocalDate data : datasDasVisitas) {
            Visit visita = new Visit();
            visita.setDate(data);
            animal.addVisit(visita);
        }
        return animal;
    }
}
