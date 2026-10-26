package org.springframework.samples.petclinic.model;

import java.time.LocalDate;

/**
 * Linha da lista de vacinas a vencer: projecao de leitura sobre Vacina, Pet e Owner (SPEC-002).
 */
public record VacinaAVencerItem(String petNome, String vacinaNome, LocalDate validade, String donoTelefone) {
}
