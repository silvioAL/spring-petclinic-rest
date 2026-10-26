package org.springframework.samples.petclinic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.Vacina;
import org.springframework.samples.petclinic.model.VacinaAVencerItem;

import java.time.LocalDate;

/**
 * Repositorio das vacinas. A consulta devolve, para cada par (pet, nome da vacina), so a
 * aplicacao de maior validade (RN-035), desde que ela seja menor ou igual ao limite da janela (RN-036).
 */
public interface VacinaRepository extends JpaRepository<Vacina, Integer> {

    @Query(value = "select new org.springframework.samples.petclinic.model.VacinaAVencerItem(p.name, v.nome, v.validade, o.telephone) "
        + "from Vacina v join Pet p on p.id = v.petId join p.owner o "
        + "where v.validade <= :limite "
        + "and v.validade = (select max(v2.validade) from Vacina v2 where v2.petId = v.petId and lower(v2.nome) = lower(v.nome)) "
        + "order by v.validade asc, lower(p.name) asc, v.id asc",
        countQuery = "select count(v) "
            + "from Vacina v join Pet p on p.id = v.petId join p.owner o "
            + "where v.validade <= :limite "
            + "and v.validade = (select max(v2.validade) from Vacina v2 where v2.petId = v.petId and lower(v2.nome) = lower(v.nome))")
    Page<VacinaAVencerItem> findAVencer(@Param("limite") LocalDate limite, Pageable pageable);
}
