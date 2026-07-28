// SRP — relatório de faturamento: o mesmo resultado, antes e depois de separar.
// Rodar: java Main.java   (JDK 17+, que executa um .java direto, sem compilar antes)
//
// As duas versões geram o relatório como texto e o main compara as strings.
// Se forem idênticas, a refatoração preservou o comportamento — que é o que os
// testes do passo 2 garantem antes de qualquer corte.
//
// Main vem primeiro porque o launcher de arquivo único executa a PRIMEIRA
// classe declarada.
//
// Alíquotas simplificadas para o exemplo. Não é referência fiscal.

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Main {
    static final List<Venda> VENDAS = List.of(
            new Venda("Notebook Pro", "produto", "SP", "São Paulo", 4500),
            new Venda("Consultoria", "serviço", "SP", "São Paulo", 8000),
            new Venda("Monitor 27", "produto", "RJ", "Rio de Janeiro", 1800),
            new Venda("Treinamento", "serviço", "MG", "Belo Horizonte", 3200),
            new Venda("Servidor Rack", "produto", "SP", "São Paulo", 12000));

    public static void main(String[] args) {
        String monolitico = new RelatorioMonolitico(VENDAS).gerar();
        String separado = new RelatorioFaturamento(VENDAS).gerar();

        System.out.println(separado);
        System.out.println();
        System.out.println(monolitico.equals(separado)
                ? "✓ monolítico e separado produziram um relatório idêntico"
                : "✗ os relatórios divergiram");
    }
}

record Venda(String descricao, String tipo, String uf, String cidade, double valor) {}

// Formatação em pt-BR. É infraestrutura, não a lição do exemplo — as duas
// versões usam a mesma, para a comparação ser justa.
final class Moeda {
    static String formatar(double valor) {
        String[] partes = String.format(Locale.US, "%.2f", valor).split("\\.");
        return partes[0].replaceAll("\\B(?=(\\d{3})+(?!\\d))", ".") + "," + partes[1];
    }
}

// ============================================================================
// ANTES — uma classe só
//
// Cálculo fiscal, agregação, ordenação e formatação convivem no mesmo método.
// Mexer na alíquota do ICMS exige reler o ranking e o rodapé para ter certeza
// de que nada mais quebrou.
// ============================================================================

class RelatorioMonolitico {
    private final List<Venda> vendas;

    RelatorioMonolitico(List<Venda> vendas) {
        this.vendas = vendas;
    }

    String gerar() {
        List<String> linhas = new ArrayList<>(List.of("=== Relatório de Faturamento ===", "", "Transações"));

        double totalIcms = 0;
        double totalIss = 0;
        double bruto = 0;

        for (int i = 0; i < vendas.size(); i++) {
            Venda venda = vendas.get(i);
            bruto += venda.valor();

            double imposto;
            String nomeImposto;

            if (venda.tipo().equals("produto")) {
                int aliquota = 17;
                if (venda.uf().equals("SP")) aliquota = 18;
                else if (venda.uf().equals("RJ")) aliquota = 20;
                else if (venda.uf().equals("MG")) aliquota = 18;

                double base = venda.valor();
                if (venda.valor() > 5000) base = venda.valor() - (venda.valor() * 20) / 100;

                imposto = (base * aliquota) / 100;
                nomeImposto = "ICMS";
                totalIcms += imposto;
            } else {
                int aliquota = 2;
                if (venda.cidade().equals("São Paulo")) aliquota = 5;
                else if (venda.cidade().equals("Rio de Janeiro")) aliquota = 5;
                else if (venda.cidade().equals("Belo Horizonte")) aliquota = 3;

                imposto = (venda.valor() * aliquota) / 100;
                nomeImposto = "ISS";
                totalIss += imposto;
            }

            linhas.add(String.format("  #%d  %-20s %s  %-8s R$ %10s   %-4s R$ %9s",
                    i + 1, venda.descricao(), venda.uf(), venda.tipo(),
                    Moeda.formatar(venda.valor()), nomeImposto, Moeda.formatar(imposto)));
        }

        linhas.addAll(List.of("", "Impostos por tipo"));
        linhas.add(String.format("  ICMS   R$ %10s", Moeda.formatar(totalIcms)));
        linhas.add(String.format("  ISS    R$ %10s", Moeda.formatar(totalIss)));
        linhas.add(String.format("  Total  R$ %10s", Moeda.formatar(totalIcms + totalIss)));

        linhas.addAll(List.of("", "Maiores receitas"));
        List<Venda> ordenadas = vendas.stream()
                .sorted(Comparator.comparingDouble(Venda::valor).reversed())
                .limit(3)
                .toList();
        for (int i = 0; i < ordenadas.size(); i++) {
            linhas.add(String.format("  %d. %-20s R$ %10s",
                    i + 1, ordenadas.get(i).descricao(), Moeda.formatar(ordenadas.get(i).valor())));
        }

        double impostos = totalIcms + totalIss;
        linhas.add("");
        linhas.add(String.format("Faturamento bruto    R$ %10s", Moeda.formatar(bruto)));
        linhas.add(String.format("Faturamento líquido  R$ %10s", Moeda.formatar(bruto - impostos)));

        return String.join("\n", linhas);
    }
}

// ============================================================================
// DEPOIS — cada lógica no seu lugar
// ============================================================================

// Em Java não existe função solta: o equivalente do utilitário é um método
// estático. O ponto se mantém — não é uma classe com estado nem com regra de
// negócio, é só um cálculo compartilhado por quem precisar.
final class Calculos {
    static double percentualDe(double valor, double percentual) {
        return (valor * percentual) / 100;
    }
}

// Lógica própria e complexidade própria — alíquota por estado e redução de
// base. Por isso vira classe.
class CalculoIcms {
    private static final Map<String, Integer> ALIQUOTAS = Map.of("SP", 18, "RJ", 20, "MG", 18);
    private static final int ALIQUOTA_PADRAO = 17;
    private static final double LIMITE_REDUCAO_BASE = 5000;
    private static final double PERCENTUAL_REDUCAO_BASE = 20;

    double calcular(Venda venda) {
        int aliquota = ALIQUOTAS.getOrDefault(venda.uf(), ALIQUOTA_PADRAO);
        double base = venda.valor() > LIMITE_REDUCAO_BASE
                ? venda.valor() - Calculos.percentualDe(venda.valor(), PERCENTUAL_REDUCAO_BASE)
                : venda.valor();
        return Calculos.percentualDe(base, aliquota);
    }
}

// Mesma razão do ICMS: regra municipal própria.
class CalculoIss {
    private static final Map<String, Integer> ALIQUOTAS =
            Map.of("São Paulo", 5, "Rio de Janeiro", 5, "Belo Horizonte", 3);
    private static final int ALIQUOTA_PADRAO = 2;

    double calcular(Venda venda) {
        return Calculos.percentualDe(venda.valor(), ALIQUOTAS.getOrDefault(venda.cidade(), ALIQUOTA_PADRAO));
    }
}

record LinhaVenda(Venda venda, double imposto, String nomeImposto) {}

// Consolida os números. Não sabe formatar nem como cada imposto é calculado.
class ResumoFaturamento {
    final List<LinhaVenda> linhas;
    final double totalIcms;
    final double totalIss;
    final double bruto;

    ResumoFaturamento(List<Venda> vendas) {
        CalculoIcms icms = new CalculoIcms();
        CalculoIss iss = new CalculoIss();

        this.linhas = vendas.stream()
                .map(venda -> venda.tipo().equals("produto")
                        ? new LinhaVenda(venda, icms.calcular(venda), "ICMS")
                        : new LinhaVenda(venda, iss.calcular(venda), "ISS"))
                .toList();

        this.totalIcms = somarImposto("ICMS");
        this.totalIss = somarImposto("ISS");
        this.bruto = vendas.stream().mapToDouble(Venda::valor).sum();
    }

    double totalImpostos() {
        return totalIcms + totalIss;
    }

    double liquido() {
        return bruto - totalImpostos();
    }

    List<Venda> maioresReceitas(int quantidade) {
        return linhas.stream()
                .map(LinhaVenda::venda)
                .sorted(Comparator.comparingDouble(Venda::valor).reversed())
                .limit(quantidade)
                .toList();
    }

    private double somarImposto(String nome) {
        return linhas.stream()
                .filter(linha -> linha.nomeImposto().equals(nome))
                .mapToDouble(LinhaVenda::imposto)
                .sum();
    }
}

// Só transforma o resumo em texto. Não calcula nada.
class RelatorioFormatter {
    String formatar(ResumoFaturamento resumo) {
        List<String> linhas = new ArrayList<>(List.of("=== Relatório de Faturamento ===", "", "Transações"));

        for (int i = 0; i < resumo.linhas.size(); i++) {
            LinhaVenda linha = resumo.linhas.get(i);
            linhas.add(String.format("  #%d  %-20s %s  %-8s R$ %10s   %-4s R$ %9s",
                    i + 1, linha.venda().descricao(), linha.venda().uf(), linha.venda().tipo(),
                    Moeda.formatar(linha.venda().valor()), linha.nomeImposto(),
                    Moeda.formatar(linha.imposto())));
        }

        linhas.addAll(List.of("", "Impostos por tipo"));
        linhas.add(String.format("  ICMS   R$ %10s", Moeda.formatar(resumo.totalIcms)));
        linhas.add(String.format("  ISS    R$ %10s", Moeda.formatar(resumo.totalIss)));
        linhas.add(String.format("  Total  R$ %10s", Moeda.formatar(resumo.totalImpostos())));

        linhas.addAll(List.of("", "Maiores receitas"));
        List<Venda> maiores = resumo.maioresReceitas(3);
        for (int i = 0; i < maiores.size(); i++) {
            linhas.add(String.format("  %d. %-20s R$ %10s",
                    i + 1, maiores.get(i).descricao(), Moeda.formatar(maiores.get(i).valor())));
        }

        linhas.add("");
        linhas.add(String.format("Faturamento bruto    R$ %10s", Moeda.formatar(resumo.bruto)));
        linhas.add(String.format("Faturamento líquido  R$ %10s", Moeda.formatar(resumo.liquido())));

        return String.join("\n", linhas);
    }
}

// Orquestra: junta as peças. Não calcula imposto nem monta texto.
class RelatorioFaturamento {
    private final List<Venda> vendas;

    RelatorioFaturamento(List<Venda> vendas) {
        this.vendas = vendas;
    }

    String gerar() {
        return new RelatorioFormatter().formatar(new ResumoFaturamento(vendas));
    }
}
