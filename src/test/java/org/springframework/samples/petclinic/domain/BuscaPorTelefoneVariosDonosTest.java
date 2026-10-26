package org.springframework.samples.petclinic.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Domínio sem framework: vários donos com o mesmo telefone voltam todos, na ordem da listagem de hoje
 * (SPEC-001, seção 2.1 e critério C4).
 */
class BuscaPorTelefoneVariosDonosTest {

    private record DonoFicticio(int id, String telefone) {
    }

    private static List<Integer> idsQueCasam(BuscaPorTelefone busca, List<DonoFicticio> donosEmOrdemDeId) {
        return donosEmOrdemDeId.stream()
            .filter(dono -> busca.casa(dono.telefone()))
            .map(DonoFicticio::id)
            .toList();
    }

    // @verifica: CT-107
    @Test
    @DisplayName("RN-025 · donos com o mesmo telefone voltam todos na ordem de hoje")
    void donosComOMesmoTelefoneVoltamTodosNaOrdemDeHoje() {
        List<DonoFicticio> donosEmOrdemDeId = List.of(
            new DonoFicticio(1, "6085551023"),
            new DonoFicticio(2, "6085559999"),
            new DonoFicticio(3, "6085551023"),
            new DonoFicticio(4, "1185551023"),
            new DonoFicticio(5, "6085551023"));

        List<Integer> porTelefoneInteiro = idsQueCasam(BuscaPorTelefone.de("(608) 555-1023"), donosEmOrdemDeId);
        List<Integer> porOitoUltimos = idsQueCasam(BuscaPorTelefone.de("8555-1023"), donosEmOrdemDeId);

        // 10 dígitos: todos os donos com esse número, sem perder nenhum e na ordem do identificador
        assertEquals(List.of(1, 3, 5), porTelefoneInteiro);
        // 8 dígitos: todos os que terminam igual, também na ordem do identificador
        assertEquals(List.of(1, 3, 4, 5), porOitoUltimos);
    }
}
