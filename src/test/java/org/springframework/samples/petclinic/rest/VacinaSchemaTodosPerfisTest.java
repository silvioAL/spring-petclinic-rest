package org.springframework.samples.petclinic.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A tabela vacinas precisa existir no schema de todos os perfis de banco (h2, hsqldb, mysql e postgres),
 * para que registrar vacina nao de 500 em nenhum deles.
 */
class VacinaSchemaTodosPerfisTest {

    private static final List<String> PERFIS = List.of("h2", "hsqldb", "mysql", "postgres");

    private static String schemaDoPerfil(String perfil) throws IOException {
        try (InputStream in = VacinaSchemaTodosPerfisTest.class.getResourceAsStream("/db/" + perfil + "/schema.sql")) {
            if (in == null) {
                return "";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        }
    }

    private static boolean criaTabelaVacinas(String sql) {
        return sql.contains("create table vacinas") || sql.contains("create table if not exists vacinas");
    }

    // @verifica: CT-144
    @Test
    @DisplayName("RF-006 · tabela vacinas existe no schema de todos os perfis de banco")
    void tabelaVacinasExisteNoSchemaDeTodosOsPerfis() throws IOException {
        List<String> semTabela = new ArrayList<>();
        for (String perfil : PERFIS) {
            if (!criaTabelaVacinas(schemaDoPerfil(perfil))) {
                semTabela.add(perfil);
            }
        }
        assertTrue(semTabela.isEmpty(),
            "o schema nao cria a tabela vacinas nos perfis: " + String.join(", ", semTabela));
    }
}
