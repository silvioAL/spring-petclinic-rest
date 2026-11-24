package plataforma.saude;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// @verifica: CT-167, CT-168, CT-169, CT-172
class PainelDeSaudeTimeTest {

    @Test
    @DisplayName("RT-002 · o painel de um time só lista os serviços desse time, os fora do ar primeiro")
    void soOsServicosDoTime_foraPrimeiro() {
        var painel = PainelDeSaude.montar("time-a", List.of(
                new PainelDeSaude.Servico("svc-a", "time-a", true, "RN-002", "d-1"),
                new PainelDeSaude.Servico("svc-b", "time-b", false, "RN-002", "d-2"),
                new PainelDeSaude.Servico("svc-c", "time-a", false, "RN-002", "d-3")));
        assertThat(painel).extracting(PainelDeSaude.Linha::servico).containsExactly("svc-c", "svc-a");
        assertThat(painel.get(0).destaque()).isTrue();
        assertThat(painel.get(1).destaque()).isFalse();
    }
}
