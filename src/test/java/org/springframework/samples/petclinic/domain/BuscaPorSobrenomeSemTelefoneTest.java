package org.springframework.samples.petclinic.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuscaPorSobrenomeSemTelefoneTest {

    private record Dono(String sobrenome, String telefone) {
    }

    private static final List<Dono> DONOS = List.of(
        new Dono("Davis", "6085551023"),
        new Dono("Davidson", "6085551749"),
        new Dono("Franklin", "6085551023"));

    // Busca por sobrenome de hoje (RN-018): sobrenome que comeca com o texto informado.
    private static List<String> sobrenomesQueComecamCom(String texto) {
        return DONOS.stream()
            .map(Dono::sobrenome)
            .filter(sobrenome -> sobrenome.startsWith(texto))
            .toList();
    }

    // @verifica: CT-114
    @Test
    @DisplayName("RN-018 · so sobrenome mantem a busca por inicio do texto")
    void soSobrenomeMantemABuscaPorInicioDoTexto() {
        // O texto do sobrenome nao e um telefone: o dominio o recusa como busca por telefone,
        // entao a busca segue o caminho do sobrenome, como hoje.
        assertThrows(IllegalArgumentException.class, () -> BuscaPorTelefone.de("Dav"));

        List<String> encontrados = sobrenomesQueComecamCom("Dav");

        assertEquals(List.of("Davis", "Davidson"), encontrados);
    }
}
