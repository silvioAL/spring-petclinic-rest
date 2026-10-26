package org.springframework.samples.petclinic.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Com o perfil hsqldb, registrar vacina nao pode dar 500: o schema do hsqldb precisa criar a tabela vacinas.
 */
class VacinaSchemaHsqldbTest {

    private static String schemaHsqldb() throws IOException {
        try (InputStream in = VacinaSchemaHsqldbTest.class.getResourceAsStream("/db/hsqldb/schema.sql")) {
            assertNotNull(in, "db/hsqldb/schema.sql nao encontrado no classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        }
    }

    private static String blocoDaTabelaVacinas(String sql) {
        int inicio = sql.indexOf("create table vacinas");
        if (inicio < 0) {
            inicio = sql.indexOf("create table if not exists vacinas");
        }
        if (inicio < 0) {
            return "";
        }
        int fim = sql.indexOf(");", inicio);
        return fim < 0 ? sql.substring(inicio) : sql.substring(inicio, fim);
    }

    // @verifica: CT-143
    @Test
    @DisplayName("RF-006 · schema hsqldb cria a tabela vacinas com as colunas do registro")
    void schemaHsqldbCriaTabelaVacinasComAsColunasDoRegistro() throws IOException {
        String bloco = blocoDaTabelaVacinas(schemaHsqldb());
        assertTrue(!bloco.isEmpty(), "o schema hsqldb nao cria a tabela vacinas");
        assertTrue(bloco.contains("pet_id"), "a tabela vacinas do hsqldb nao tem a coluna pet_id");
        assertTrue(bloco.contains("nome"), "a tabela vacinas do hsqldb nao tem a coluna nome");
        assertTrue(bloco.contains("validade"), "a tabela vacinas do hsqldb nao tem a coluna validade");
        assertTrue(bloco.contains("foreign key") && bloco.contains("pets"),
            "a tabela vacinas do hsqldb nao tem a chave estrangeira para pets");
    }
}
