package org.springframework.samples.petclinic.domain;

/**
 * Value Object da busca de donos por telefone (SPEC-001, seções 2.1 e 5; RN-008).
 * Guarda só os dígitos. Com 10 dígitos a busca casa pelo telefone inteiro; com 8 dígitos,
 * pelos 8 últimos dígitos do telefone cadastrado. Espaço, parênteses, hífen e ponto são ignorados.
 * Domínio sem framework.
 */
public final class BuscaPorTelefone {

    static final int DIGITOS_DO_TELEFONE = 10;
    static final int DIGITOS_DO_FINAL = 8;
    static final String MENSAGEM_INVALIDO =
        "O telefone deve ter 8 ou 10 dígitos numéricos (espaço, parênteses, hífen e ponto são ignorados)";

    private final String digitos;

    private BuscaPorTelefone(String digitos) {
        this.digitos = digitos;
    }

    public static BuscaPorTelefone de(String texto) {
        if (texto == null) {
            throw new TelefoneInvalidoException(MENSAGEM_INVALIDO);
        }
        StringBuilder soDigitos = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if (c >= '0' && c <= '9') {
                soDigitos.append(c);
            } else if (!ehMascara(c)) {
                throw new TelefoneInvalidoException(MENSAGEM_INVALIDO);
            }
        }
        int tamanho = soDigitos.length();
        if (tamanho != DIGITOS_DO_TELEFONE && tamanho != DIGITOS_DO_FINAL) {
            throw new TelefoneInvalidoException(MENSAGEM_INVALIDO);
        }
        return new BuscaPorTelefone(soDigitos.toString());
    }

    private static boolean ehMascara(char c) {
        return Character.isWhitespace(c) || c == '(' || c == ')' || c == '-' || c == '.';
    }

    public String digitos() {
        return digitos;
    }

    /**
     * @return true quando a busca é pelo telefone inteiro (10 dígitos); false quando é pelos 8 últimos
     */
    public boolean ehPorTelefoneInteiro() {
        return digitos.length() == DIGITOS_DO_TELEFONE;
    }

    /**
     * @param telefoneCadastrado telefone guardado no cadastro do dono (só dígitos, RN-008)
     * @return se o telefone cadastrado casa com esta busca
     */
    public boolean casa(String telefoneCadastrado) {
        if (telefoneCadastrado == null) {
            return false;
        }
        return ehPorTelefoneInteiro()
            ? telefoneCadastrado.equals(digitos)
            : telefoneCadastrado.endsWith(digitos);
    }
}
