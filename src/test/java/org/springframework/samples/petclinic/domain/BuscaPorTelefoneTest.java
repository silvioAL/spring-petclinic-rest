package org.springframework.samples.petclinic.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Domínio sem framework: a busca de donos por telefone (SPEC-001, seções 2.1 e 5).
 */
class BuscaPorTelefoneTest {

    // @verifica: CT-102
    @Test
    @DisplayName("RN-008 · telefone de 10 dígitos casa pelo telefone inteiro e devolve o dono")
    void telefoneDeDezDigitosCasaPeloTelefoneInteiro() {
        BuscaPorTelefone busca = BuscaPorTelefone.de("6085551023");

        assertEquals("6085551023", busca.digitos());
        assertTrue(busca.ehPorTelefoneInteiro());
        assertTrue(busca.casa("6085551023"));
    }

    // @verifica: CT-103
    @Test
    @DisplayName("RN-008 · espaço, parênteses, hífen e ponto são ignorados ao normalizar o telefone")
    void mascaraEIgnoradaAoNormalizarOTelefone() {
        BuscaPorTelefone comParenteses = BuscaPorTelefone.de("(608) 555-1023");
        BuscaPorTelefone comPontos = BuscaPorTelefone.de("608.555.1023");
        BuscaPorTelefone comEspacos = BuscaPorTelefone.de(" 608 555 1023 ");

        assertEquals("6085551023", comParenteses.digitos());
        assertEquals("6085551023", comPontos.digitos());
        assertEquals("6085551023", comEspacos.digitos());
        assertTrue(comParenteses.ehPorTelefoneInteiro());
        assertTrue(comParenteses.casa("6085551023"));
    }

    // @verifica: CT-104
    @Test
    @DisplayName("RN-008 · telefone com máscara casa o mesmo dono que o telefone sem máscara")
    void telefoneComMascaraCasaOMesmoDonoDoTelefoneSemMascara() {
        String telefoneCadastrado = "6085551023";
        BuscaPorTelefone semMascara = BuscaPorTelefone.de("6085551023");
        BuscaPorTelefone comMascara = BuscaPorTelefone.de("(608) 555-1023");

        assertEquals(semMascara.digitos(), comMascara.digitos());
        assertTrue(semMascara.casa(telefoneCadastrado));
        assertTrue(comMascara.casa(telefoneCadastrado));
        assertFalse(comMascara.casa("6085551024"));
    }
}
