package plataforma.saude;

import java.util.Comparator;
import java.util.List;

/**
 * Painel de saúde do time (SPEC-001): só os serviços do time (RT-002), os fora do ar primeiro e em
 * destaque, cada um com a versão vigente da regra e o deploy que a ativou (RN-002).
 */
public final class PainelDeSaude {

    public record Servico(String id, String time, boolean noAr, String regraVigente, String deploy) {
    }

    public record Linha(String servico, String status, boolean destaque, String versao) {
    }

    private PainelDeSaude() {
    }

    public static List<Linha> montar(String time, List<Servico> servicos) {
        return servicos.stream()
                .filter(s -> s.time().equals(time))
                .sorted(Comparator.comparing(Servico::noAr))
                .map(s -> new Linha(s.id(), s.noAr() ? "no ar" : "fora", !s.noAr(), s.regraVigente() + " vigente desde " + s.deploy()))
                .toList();
    }
}
