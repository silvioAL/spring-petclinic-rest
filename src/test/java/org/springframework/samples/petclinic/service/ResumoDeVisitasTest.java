package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ResumoDeVisitas.VisitasDoPet;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Resumo de visitas do tutor (SPEC-001, 2.1 e 2.2): o calculo, sem Spring e sem banco.
 * Os testes falham ate a implementacao existir.
 */
class ResumoDeVisitasTest {

    // @verifica: CT-901
    @Test
    @DisplayName("SPEC-001 2.2 · soma as visitas dos animais e traz a mais recente")
    void somaAsVisitasDosAnimaisETrazAMaisRecente() {
        Owner tutor = tutor(6);
        tutor.addPet(pet(7, "Samantha", LocalDate.of(2013, 1, 1), LocalDate.of(2013, 1, 4)));
        tutor.addPet(pet(8, "max", LocalDate.of(2013, 1, 3)));
        tutor.addPet(pet(9, "Bidu"));

        ResumoDeVisitas resumo = ResumoDeVisitas.de(tutor);

        assertEquals(6, resumo.ownerId());
        assertEquals(3, resumo.total());
        assertEquals(LocalDate.of(2013, 1, 4), resumo.ultimaVisita());
        assertEquals(List.of(
            new VisitasDoPet(9, "Bidu", 0),
            new VisitasDoPet(8, "max", 1),
            new VisitasDoPet(7, "Samantha", 2)), resumo.porPet(),
            "todos os animais do tutor, na ordem do nome (RN-009), inclusive o sem visita");
    }

    // @verifica: CT-902
    @Test
    @DisplayName("SPEC-001 2.2 · tutor sem visita tem total 0, ultimaVisita nula e os animais com 0")
    void tutorSemVisitaTemTotalZeroUltimaVisitaNulaEAnimaisComZero() {
        Owner tutor = tutor(1);
        tutor.addPet(pet(2, "Rosy"));
        tutor.addPet(pet(1, "Leo"));

        ResumoDeVisitas resumo = ResumoDeVisitas.de(tutor);

        assertEquals(1, resumo.ownerId());
        assertEquals(0, resumo.total());
        assertNull(resumo.ultimaVisita());
        assertEquals(List.of(
            new VisitasDoPet(1, "Leo", 0),
            new VisitasDoPet(2, "Rosy", 0)), resumo.porPet());
    }

    private static Owner tutor(int id) {
        Owner tutor = new Owner();
        tutor.setId(id);
        return tutor;
    }

    private static Pet pet(int id, String nome, LocalDate... datasDasVisitas) {
        Pet pet = new Pet();
        pet.setId(id);
        pet.setName(nome);
        for (LocalDate data : datasDasVisitas) {
            Visit visita = new Visit();
            visita.setDate(data);
            pet.addVisit(visita);
        }
        return pet;
    }
}
