package org.springframework.samples.petclinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Vacina registrada para um pet (SPEC-001). Depois de registrada, nao muda: a renovacao
 * e uma nova vacina com o mesmo nome e validade posterior (RN-035).
 */
@Entity
@Table(name = "vacinas")
public class Vacina extends BaseEntity {

    @Column(name = "pet_id")
    private Integer petId;

    @Column(name = "nome")
    private String nome;

    @Column(name = "validade", columnDefinition = "DATE")
    private LocalDate validade;

    public Integer getPetId() {
        return petId;
    }

    public void setPetId(Integer petId) {
        this.petId = petId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }
}
