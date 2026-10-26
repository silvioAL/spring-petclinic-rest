package org.springframework.samples.petclinic.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuscaPorTelefoneDonoSemAnimaisTest {

    private record Dono(String nome, String telefone, List<String> animais) {
    }

    private static final Dono SEM_ANIMAIS = new Dono("Davis", "6085551023", List.of());
    private static final Dono COM_ANIMAIS = new Dono("Franklin", "6085551749", List.of("Leo"));

    private static List<Dono> donosQueCasam(BuscaPorTelefone busca) {
        return List.of(SEM_ANIMAIS, COM_ANIMAIS).stream()
            .filter(dono -> busca.casa(dono.telefone()))
            .toList();
    }

    // @verifica: CT-113
    @Test
    @DisplayName("RN-024 · dono sem animais aparece na busca por telefone inteiro")
    void donoSemAnimaisApareceNaBuscaPorTelefoneInteiro() {
        BuscaPorTelefone busca = BuscaPorTelefone.de("(608) 555-1023");

        List<Dono> encontrados = donosQueCasam(busca);

        assertEquals(1, encontrados.size());
        assertEquals("Davis", encontrados.get(0).nome());
        assertTrue(encontrados.get(0).animais().isEmpty());
    }

    // @verifica: CT-113
    @Test
    @DisplayName("RN-024 · dono sem animais aparece na busca pelos 8 ultimos digitos")
    void donoSemAnimaisApareceNaBuscaPorFinal() {
        BuscaPorTelefone busca = BuscaPorTelefone.de("55551023");

        List<Dono> encontrados = donosQueCasam(busca);

        assertEquals(1, encontrados.size());
        assertEquals("Davis", encontrados.get(0).nome());
        assertTrue(encontrados.get(0).animais().isEmpty());
    }
}
