package org.springframework.samples.petclinic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.samples.petclinic.model.Vacina;
import org.springframework.samples.petclinic.model.VacinaAVencerItem;
import org.springframework.samples.petclinic.repository.VacinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Casos de uso de vacina: registrar (SPEC-001) e listar as a vencer (SPEC-002).
 */
@Service
public class VacinaService {

    private final VacinaRepository vacinaRepository;
    private final int janelaDias;

    public VacinaService(VacinaRepository vacinaRepository,
                         @Value("${petclinic.vacinas.janela-dias:30}") int janelaDias) {
        this.vacinaRepository = vacinaRepository;
        this.janelaDias = janelaDias;
    }

    @Transactional
    public Vacina registrar(int petId, String nome, LocalDate validade) {
        Vacina vacina = new Vacina();
        vacina.setPetId(petId);
        vacina.setNome(nome);
        vacina.setValidade(validade);
        return vacinaRepository.save(vacina);
    }

    @Transactional(readOnly = true)
    public Page<VacinaAVencerItem> listarAVencer(LocalDate hoje, int page, int size) {
        LocalDate limite = hoje.plusDays(janelaDias);
        return vacinaRepository.findAVencer(limite, PageRequest.of(page, size));
    }
}
