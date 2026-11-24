package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Resumo de visitas do tutor (SPEC-001): o total, a data da visita mais recente (nula sem visita)
 * e as visitas de cada animal, na ordem dos animais do tutor (pelo nome, RN-009).
 */
public class ResumoDeVisitas {

    private final Integer ownerId;
    private final int total;
    private final LocalDate ultimaVisita;
    private final List<VisitasDoPet> porPet;

    public ResumoDeVisitas(Owner owner) {
        List<Pet> pets = owner.getPets();
        this.ownerId = owner.getId();
        this.porPet = pets.stream().map(VisitasDoPet::new).toList();
        this.total = this.porPet.stream().mapToInt(VisitasDoPet::getVisitas).sum();
        this.ultimaVisita = pets.stream()
            .flatMap(pet -> pet.getVisits().stream())
            .map(Visit::getDate)
            .max(Comparator.naturalOrder())
            .orElse(null);
    }

    public Integer getOwnerId() {
        return this.ownerId;
    }

    public int getTotal() {
        return this.total;
    }

    public LocalDate getUltimaVisita() {
        return this.ultimaVisita;
    }

    public List<VisitasDoPet> getPorPet() {
        return this.porPet;
    }

    /**
     * As visitas de um animal do tutor.
     */
    public static class VisitasDoPet {

        private final Integer petId;
        private final String nome;
        private final int visitas;

        VisitasDoPet(Pet pet) {
            this.petId = pet.getId();
            this.nome = pet.getName();
            this.visitas = pet.getVisits().size();
        }

        public Integer getPetId() {
            return this.petId;
        }

        public String getNome() {
            return this.nome;
        }

        public int getVisitas() {
            return this.visitas;
        }
    }
}
