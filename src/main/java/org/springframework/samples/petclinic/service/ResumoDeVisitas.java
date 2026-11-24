package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Resumo de visitas do tutor (SPEC-001): quantas visitas os animais dele tem, a mais recente e o total por animal.
 */
public record ResumoDeVisitas(Integer ownerId, int total, LocalDate ultimaVisita, List<VisitasDoPet> porPet) {

    public record VisitasDoPet(Integer petId, String nome, int visitas) {
    }

    public static ResumoDeVisitas de(Owner tutor) {
        List<Pet> pets = tutor.getPets();
        List<VisitasDoPet> porPet = pets.stream()
            .map(pet -> new VisitasDoPet(pet.getId(), pet.getName(), pet.getVisits().size()))
            .toList();
        int total = porPet.stream().mapToInt(VisitasDoPet::visitas).sum();
        LocalDate ultimaVisita = pets.stream()
            .flatMap(pet -> pet.getVisits().stream())
            .map(Visit::getDate)
            .max(Comparator.naturalOrder())
            .orElse(null);
        return new ResumoDeVisitas(tutor.getId(), total, ultimaVisita, porPet);
    }
}
