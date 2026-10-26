package org.springframework.samples.petclinic.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Domínio sem framework: quando telefone e sobrenome chegam juntos, o telefone prevalece
 * e o sobrenome é ignorado (SPEC-001, seção 2.1).
 */
class BuscaPorTelefoneESobrenomeJuntosTest {

    private record Dono(String sobrenome, String telefone) {
    }

    private static final List<Dono> DONOS = List.of(
        new Dono("Davis", "6085551023"),
        new Dono("Davidson", "6085551749"),
        new Dono("Franklin", "6085551023"));

    // Escolha da busca (SPEC-001, seção 2.1): com telefone informado, ele prevalece e o
    // sobrenome nao e usado; sem telefone, vale a busca por inicio do sobrenome (RN-018).
    private static List<String> buscar(String telefone, String sobrenome) {
        if (telefone != null && !telefone.isBlank()) {
            BuscaPorTelefone busca = BuscaPorTelefone.de(telefone);
            return DONOS.stream()
                .filter(dono -> busca.casa(dono.telefone()))
                .map(Dono::sobrenome)
                .toList();
        }
        return DONOS.stream()
            .map(Dono::sobrenome)
            .filter(nome -> sobrenome != null && nome.startsWith(sobrenome))
            .toList();
    }

    // @verifica: CT-115
    @Test
    @DisplayName("RN-008 · telefone e sobrenome juntos buscam pelo telefone e ignoram o sobrenome")
    void telefoneESobrenomeJuntosBuscamPeloTelefone() {
        // O sobrenome "Davidson" sozinho devolveria so Davidson; com telefone junto,
        // o resultado e o dos donos com o telefone informado.
        List<String> encontrados = buscar("(608) 555-1023", "Davidson");

        assertEquals(List.of("Davis", "Franklin"), encontrados);

        // o mesmo vale com 8 digitos (final do numero)
        assertEquals(List.of("Davis", "Franklin"), buscar("85551023", "Davidson"));

        // um sobrenome que nao casa com ninguem tambem e ignorado
        assertEquals(List.of("Davis", "Franklin"), buscar("6085551023", "Inexistente"));
    }
}
