package org.springframework.samples.petclinic.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuscaPorTelefoneSemDonoTest {

    private static final List<String> TELEFONES_CADASTRADOS = List.of(
        "6085551023",
        "6085551749",
        "6085553198");

    private static List<String> donosQueCasam(BuscaPorTelefone busca) {
        return TELEFONES_CADASTRADOS.stream().filter(busca::casa).toList();
    }

    // @verifica: CT-108
    @Test
    @DisplayName("RN-008 · telefone de 10 digitos que nenhum dono tem nao casa com ninguem e a lista fica vazia")
    void telefoneInteiroSemDonoDevolveListaVazia() {
        BuscaPorTelefone busca = BuscaPorTelefone.de("(999) 999-9999");

        assertTrue(busca.ehPorTelefoneInteiro());
        assertEquals("9999999999", busca.digitos());
        assertTrue(donosQueCasam(busca).isEmpty());
    }

    // @verifica: CT-108
    @Test
    @DisplayName("RN-008 · telefone de 8 digitos que nenhum dono tem nos 8 ultimos nao casa com ninguem e a lista fica vazia")
    void telefoneFinalSemDonoDevolveListaVazia() {
        BuscaPorTelefone busca = BuscaPorTelefone.de("99999999");

        assertEquals("99999999", busca.digitos());
        assertTrue(donosQueCasam(busca).isEmpty());
    }
}
