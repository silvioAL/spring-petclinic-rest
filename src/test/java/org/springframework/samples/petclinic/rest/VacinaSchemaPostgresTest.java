package org.springframework.samples.petclinic.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regressao do defeito: com o perfil postgres o registro de vacina dava 500 porque a tabela vacinas
 * so existia no schema do H2. O schema do postgres (spring.sql.init.platform=postgres) precisa criar a tabela
 * com as colunas que o registro grava.
 */
class VacinaSchemaPostgresTest {

    private static String schemaPostgres() throws IOException {
        try (InputStream in = VacinaSchemaPostgresTest.class.getResourceAsStream("/db/postgres/schema.sql")) {
            assertNotNull(in, "db/postgres/schema.sql nao encontrado no classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        }
    }

    // @verifica: CT-142
    @Test
    @DisplayName("RF-006 · registro de vacina com perfil postgres grava sem erro 500")
    void schemaPostgresCriaTabelaVacinasComAsColunasDoRegistro() throws IOException {
        String schema = schemaPostgres();

        Pattern criaTabela = Pattern.compile("create\\s+table\\s+(if\\s+not\\s+exists\\s+)?vacinas\\s*\\(");
        Matcher achou = criaTabela.matcher(schema);
        assertTrue(achou.find(), "o schema postgres nao cria a tabela vacinas");

        int inicio = achou.start();
        int fim = schema.indexOf(");", inicio);
        String corpo = schema.substring(inicio, fim > 0 ? fim : schema.length());
        assertTrue(corpo.contains("pet_id"), "vacinas sem a coluna pet_id");
        assertTrue(corpo.contains("nome"), "vacinas sem a coluna nome");
        assertTrue(corpo.contains("validade"), "vacinas sem a coluna validade");
        assertTrue(corpo.contains("references pets"), "vacinas sem a chave estrangeira para pets");
    }
}
