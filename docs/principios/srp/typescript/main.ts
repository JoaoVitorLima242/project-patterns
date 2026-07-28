// SRP — relatório de faturamento: o mesmo resultado, antes e depois de separar.
// Rodar: node main.ts   (Node 22.18+ ou 23.6+, que executa .ts direto)
//
// As duas versões geram o relatório como texto e o main compara as strings.
// Se forem idênticas, a refatoração preservou o comportamento — que é o que
// os testes do passo 2 garantem antes de qualquer corte.
//
// Alíquotas simplificadas para o exemplo. Não é referência fiscal.

type TipoVenda = "produto" | "serviço";

type Venda = {
  descricao: string;
  tipo: TipoVenda;
  uf: string;
  cidade: string;
  valor: number;
};

// Formatação de moeda em pt-BR. É infraestrutura, não a lição do exemplo —
// as duas versões usam a mesma, para a comparação ser justa.
function formatarMoeda(valor: number): string {
  const [inteiro, centavos] = valor.toFixed(2).split(".");
  return `${inteiro.replace(/\B(?=(\d{3})+(?!\d))/g, ".")},${centavos}`;
}

// ============================================================================
// ANTES — uma classe só
//
// Cálculo fiscal, agregação, ordenação e formatação convivem no mesmo método.
// Mexer na alíquota do ICMS exige reler o ranking e o rodapé para ter certeza
// de que nada mais quebrou.
// ============================================================================

class RelatorioMonolitico {
  private vendas: Venda[];

  constructor(vendas: Venda[]) {
    this.vendas = vendas;
  }

  gerar(): string {
    const linhas: string[] = ["=== Relatório de Faturamento ===", "", "Transações"];

    let totalIcms = 0;
    let totalIss = 0;
    let bruto = 0;

    for (let i = 0; i < this.vendas.length; i++) {
      const venda = this.vendas[i];
      bruto += venda.valor;

      let imposto = 0;
      let nomeImposto = "";

      if (venda.tipo === "produto") {
        let aliquota = 17;
        if (venda.uf === "SP") aliquota = 18;
        else if (venda.uf === "RJ") aliquota = 20;
        else if (venda.uf === "MG") aliquota = 18;

        let base = venda.valor;
        if (venda.valor > 5000) base = venda.valor - (venda.valor * 20) / 100;

        imposto = (base * aliquota) / 100;
        nomeImposto = "ICMS";
        totalIcms += imposto;
      } else {
        let aliquota = 2;
        if (venda.cidade === "São Paulo") aliquota = 5;
        else if (venda.cidade === "Rio de Janeiro") aliquota = 5;
        else if (venda.cidade === "Belo Horizonte") aliquota = 3;

        imposto = (venda.valor * aliquota) / 100;
        nomeImposto = "ISS";
        totalIss += imposto;
      }

      linhas.push(
        `  #${i + 1}  ${venda.descricao.padEnd(20)} ${venda.uf}  ${venda.tipo.padEnd(8)} ` +
          `R$ ${formatarMoeda(venda.valor).padStart(10)}   ` +
          `${nomeImposto.padEnd(4)} R$ ${formatarMoeda(imposto).padStart(9)}`,
      );
    }

    linhas.push("", "Impostos por tipo");
    linhas.push(`  ICMS   R$ ${formatarMoeda(totalIcms).padStart(10)}`);
    linhas.push(`  ISS    R$ ${formatarMoeda(totalIss).padStart(10)}`);
    linhas.push(`  Total  R$ ${formatarMoeda(totalIcms + totalIss).padStart(10)}`);

    linhas.push("", "Maiores receitas");
    const ordenadas = [...this.vendas].sort((a, b) => b.valor - a.valor).slice(0, 3);
    for (let i = 0; i < ordenadas.length; i++) {
      linhas.push(
        `  ${i + 1}. ${ordenadas[i].descricao.padEnd(20)} R$ ${formatarMoeda(ordenadas[i].valor).padStart(10)}`,
      );
    }

    const impostos = totalIcms + totalIss;
    linhas.push("");
    linhas.push(`Faturamento bruto    R$ ${formatarMoeda(bruto).padStart(10)}`);
    linhas.push(`Faturamento líquido  R$ ${formatarMoeda(bruto - impostos).padStart(10)}`);

    return linhas.join("\n");
  }
}

// ============================================================================
// DEPOIS — cada lógica no seu lugar
// ============================================================================

// Usado por vários cálculos, mas é utilitário matemático sem regra de negócio:
// vira função, não classe.
function percentualDe(valor: number, percentual: number): number {
  return (valor * percentual) / 100;
}

const ALIQUOTAS_ICMS: Record<string, number> = { SP: 18, RJ: 20, MG: 18 };
const ALIQUOTA_ICMS_PADRAO = 17;
const LIMITE_REDUCAO_BASE = 5000;
const PERCENTUAL_REDUCAO_BASE = 20;

// Lógica própria e complexidade própria — alíquota por estado e redução de
// base. Por isso vira classe.
class CalculoIcms {
  calcular(venda: Venda): number {
    const aliquota = ALIQUOTAS_ICMS[venda.uf] ?? ALIQUOTA_ICMS_PADRAO;
    const base =
      venda.valor > LIMITE_REDUCAO_BASE
        ? venda.valor - percentualDe(venda.valor, PERCENTUAL_REDUCAO_BASE)
        : venda.valor;
    return percentualDe(base, aliquota);
  }
}

const ALIQUOTAS_ISS: Record<string, number> = {
  "São Paulo": 5,
  "Rio de Janeiro": 5,
  "Belo Horizonte": 3,
};
const ALIQUOTA_ISS_PADRAO = 2;

// Mesma razão do ICMS: regra municipal própria.
class CalculoIss {
  calcular(venda: Venda): number {
    return percentualDe(venda.valor, ALIQUOTAS_ISS[venda.cidade] ?? ALIQUOTA_ISS_PADRAO);
  }
}

type LinhaVenda = { venda: Venda; imposto: number; nomeImposto: string };

// Consolida os números. Não sabe formatar nem como cada imposto é calculado.
class ResumoFaturamento {
  readonly linhas: LinhaVenda[];
  readonly totalIcms: number;
  readonly totalIss: number;
  readonly bruto: number;

  constructor(vendas: Venda[]) {
    const icms = new CalculoIcms();
    const iss = new CalculoIss();

    this.linhas = vendas.map((venda) =>
      venda.tipo === "produto"
        ? { venda, imposto: icms.calcular(venda), nomeImposto: "ICMS" }
        : { venda, imposto: iss.calcular(venda), nomeImposto: "ISS" },
    );

    this.totalIcms = this.somarImposto("ICMS");
    this.totalIss = this.somarImposto("ISS");
    this.bruto = vendas.reduce((total, venda) => total + venda.valor, 0);
  }

  get totalImpostos(): number {
    return this.totalIcms + this.totalIss;
  }

  get liquido(): number {
    return this.bruto - this.totalImpostos;
  }

  maioresReceitas(quantidade: number): Venda[] {
    return this.linhas
      .map((linha) => linha.venda)
      .sort((a, b) => b.valor - a.valor)
      .slice(0, quantidade);
  }

  private somarImposto(nome: string): number {
    return this.linhas
      .filter((linha) => linha.nomeImposto === nome)
      .reduce((total, linha) => total + linha.imposto, 0);
  }
}

// Só transforma o resumo em texto. Não calcula nada.
class RelatorioFormatter {
  formatar(resumo: ResumoFaturamento): string {
    const linhas: string[] = ["=== Relatório de Faturamento ===", "", "Transações"];

    resumo.linhas.forEach((linha, i) => {
      linhas.push(
        `  #${i + 1}  ${linha.venda.descricao.padEnd(20)} ${linha.venda.uf}  ${linha.venda.tipo.padEnd(8)} ` +
          `R$ ${formatarMoeda(linha.venda.valor).padStart(10)}   ` +
          `${linha.nomeImposto.padEnd(4)} R$ ${formatarMoeda(linha.imposto).padStart(9)}`,
      );
    });

    linhas.push("", "Impostos por tipo");
    linhas.push(`  ICMS   R$ ${formatarMoeda(resumo.totalIcms).padStart(10)}`);
    linhas.push(`  ISS    R$ ${formatarMoeda(resumo.totalIss).padStart(10)}`);
    linhas.push(`  Total  R$ ${formatarMoeda(resumo.totalImpostos).padStart(10)}`);

    linhas.push("", "Maiores receitas");
    resumo.maioresReceitas(3).forEach((venda, i) => {
      linhas.push(`  ${i + 1}. ${venda.descricao.padEnd(20)} R$ ${formatarMoeda(venda.valor).padStart(10)}`);
    });

    linhas.push("");
    linhas.push(`Faturamento bruto    R$ ${formatarMoeda(resumo.bruto).padStart(10)}`);
    linhas.push(`Faturamento líquido  R$ ${formatarMoeda(resumo.liquido).padStart(10)}`);

    return linhas.join("\n");
  }
}

// Orquestra: junta as peças. Não calcula imposto nem monta texto.
class RelatorioFaturamento {
  private vendas: Venda[];

  constructor(vendas: Venda[]) {
    this.vendas = vendas;
  }

  gerar(): string {
    return new RelatorioFormatter().formatar(new ResumoFaturamento(this.vendas));
  }
}

// --- demonstração ---

const VENDAS: Venda[] = [
  { descricao: "Notebook Pro", tipo: "produto", uf: "SP", cidade: "São Paulo", valor: 4500 },
  { descricao: "Consultoria", tipo: "serviço", uf: "SP", cidade: "São Paulo", valor: 8000 },
  { descricao: "Monitor 27", tipo: "produto", uf: "RJ", cidade: "Rio de Janeiro", valor: 1800 },
  { descricao: "Treinamento", tipo: "serviço", uf: "MG", cidade: "Belo Horizonte", valor: 3200 },
  { descricao: "Servidor Rack", tipo: "produto", uf: "SP", cidade: "São Paulo", valor: 12000 },
];

const monolitico = new RelatorioMonolitico(VENDAS).gerar();
const separado = new RelatorioFaturamento(VENDAS).gerar();

console.log(separado);
console.log();
console.log(
  monolitico === separado
    ? "✓ monolítico e separado produziram um relatório idêntico"
    : "✗ os relatórios divergiram",
);
