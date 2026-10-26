package org.springframework.samples.petclinic.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Domínio sem framework: telefone com caractere que não é dígito nem máscara é recusado
 * (SPEC-001, seções 2.1 e 8).
 */
class BuscaPorTelefoneComLetrasTest {

    // @verifica: CT-111
    @Test
    @DisplayName("RN-008 · telefone com letras é recusado")
    void telefoneComLetrasERecusado() {
        // 10 caracteres, mas nem todos são dígitos
        assertThrows(IllegalArgumentException.class, () -> BuscaPorTelefone.de("608555abcd"));

        // 8 caracteres, com letras
        assertThrows(IllegalArgumentException.class, () -> BuscaPorTelefone.de("8555ab23"));

        // a máscara é tirada, mas a letra que sobra continua recusando
        assertThrows(IllegalArgumentException.class, () -> BuscaPorTelefone.de("(608) 555-10a3"));

        // só letras
        assertThrows(IllegalArgumentException.class, () -> BuscaPorTelefone.de("telefone"));
    }
}
