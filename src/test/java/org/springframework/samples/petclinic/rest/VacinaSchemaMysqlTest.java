package org.springframework.samples.petclinic.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regressao do defeito: com o perfil mysql o registro de vacina dava 500 porque a tabela vacinas
 * so existia no schema do H2. O schema do mysql (spring.sql.init.platform=mysql) precisa criar a tabela
 * com as colunas que o registro grava.
 */
class VacinaSchemaMysqlTest {

    private static String schemaMysql() throws IOException {
        try (InputStream in = VacinaSchemaMysqlTest.class.getResourceAsStream("/db/mysql/schema.sql")) {
            assertNotNull(in, "db/mysql/schema.sql nao encontrado no classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        }
    }

    // @verifica: CT-141
    @Test
    @DisplayName("RF-006 · registro de vacina com perfil mysql grava sem erro 500")
    void schemaMysqlCriaTabelaVacinasComAsColunasDoRegistro() throws IOException {
        String schema = schemaMysql();

        Pattern criaTabela = Pattern.compile("create\\s+table\\s+(if\\s+not\\s+exists\\s+)?vacinas\\s*\\(");
        assertTrue(criaTabela.matcher(schema).find(), "o schema mysql nao cria a tabela vacinas");

        int inicio = schema.indexOf("vacinas");
        String corpo = schema.substring(inicio, schema.indexOf("engine", inicio) > 0 ? schema.indexOf("engine", inicio) : schema.length());
        assertTrue(corpo.contains("pet_id"), "vacinas sem a coluna pet_id");
        assertTrue(corpo.contains("nome"), "vacinas sem a coluna nome");
        assertTrue(corpo.contains("validade"), "vacinas sem a coluna validade");
        assertTrue(corpo.contains("references pets"), "vacinas sem a chave estrangeira para pets");
    }
}
