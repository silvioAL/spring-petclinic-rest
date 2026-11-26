package plataforma.saude;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// @verifica: CT-040, CT-041, CT-042, CT-043, CT-044, CT-045, CT-046, CT-056, CT-057, CT-058
class PainelDeSaudeVersaoTest {

    @Test
    @DisplayName("RN-002 · a versão vigente da regra aparece com o deploy que a ativou")
    void versaoVigenteMostraODeployQueAAtivou() {
        var painel = PainelDeSaude.montar("time-a", List.of(
                new PainelDeSaude.Servico("catalogo-service", "time-a", true, "RN-002", "deploy-42")));
        assertThat(painel.get(0).versao()).isEqualTo("RN-002 vigente desde deploy-42");
    }
}
