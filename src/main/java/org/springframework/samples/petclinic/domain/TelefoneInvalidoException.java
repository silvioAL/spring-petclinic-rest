package org.springframework.samples.petclinic.domain;

/**
 * Telefone de busca recusado: tamanho diferente de 8 e 10 dígitos, ou caractere que não é
 * dígito nem máscara (SPEC-001, seções 2.1 e 8). É uma IllegalArgumentException do domínio,
 * sem framework; a camada REST a traduz em 400.
 */
public class TelefoneInvalidoException extends IllegalArgumentException {

    public TelefoneInvalidoException(String message) {
        super(message);
    }
}
