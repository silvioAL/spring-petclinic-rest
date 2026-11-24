package plataforma.saude;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// @verifica: CT-901, CT-902
class PainelDeSaudeVersaoTest {

    @Test
    @DisplayName("RN-002 · a versão vigente da regra aparece com o deploy que a ativou")
    void versaoVigenteMostraODeployQueAAtivou() {
        var painel = PainelDeSaude.montar("time-a", List.of(
                new PainelDeSaude.Servico("catalogo-service", "time-a", true, "RN-002", "deploy-42")));
        assertThat(painel.get(0).versao()).isEqualTo("RN-002 vigente desde deploy-42");
    }
}
