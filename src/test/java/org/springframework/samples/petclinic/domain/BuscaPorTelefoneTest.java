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

    // @verifica: CT-105
    @Test
    @DisplayName("RN-008 · 8 dígitos casam pelos 8 últimos dígitos do telefone cadastrado")
    void oitoDigitosCasamPelosOitoUltimosDoTelefoneCadastrado() {
        BuscaPorTelefone busca = BuscaPorTelefone.de("85551023");

        assertEquals("85551023", busca.digitos());
        assertFalse(busca.ehPorTelefoneInteiro());
        assertTrue(busca.casa("6085551023"));
        assertTrue(busca.casa("1185551023"));
        assertFalse(busca.casa("6085551024"));
        assertFalse(busca.casa("6095551023"));
    }

    // @verifica: CT-106
    @Test
    @DisplayName("RN-008 · busca por telefone escolhe inteiro com 10 dígitos e final com 8")
    void buscaPorTelefoneEscolheInteiroComDezDigitosEFinalComOito() {
        BuscaPorTelefone inteiro = BuscaPorTelefone.de("6085551023");
        BuscaPorTelefone final8 = BuscaPorTelefone.de("85551023");

        assertTrue(inteiro.ehPorTelefoneInteiro());
        assertFalse(final8.ehPorTelefoneInteiro());

        // com 10 dígitos o número tem de ser o mesmo, inclusive o início (DDD)
        assertTrue(inteiro.casa("6085551023"));
        assertFalse(inteiro.casa("1185551023"));

        // com 8 dígitos só os 8 últimos contam, o início do cadastrado é livre
        assertTrue(final8.casa("6085551023"));
        assertTrue(final8.casa("1185551023"));

        // a mesma máscara escolhe o mesmo modo que os dígitos puros
        assertTrue(BuscaPorTelefone.de("(608) 555-1023").ehPorTelefoneInteiro());
        assertFalse(BuscaPorTelefone.de("8555-1023").ehPorTelefoneInteiro());
    }
}
