"""SRP — relatório de faturamento: o mesmo resultado, antes e depois de separar.

Rodar: python3 main.py   (Python 3.10+)

As duas versões geram o relatório como texto e o main compara as strings.
Se forem idênticas, a refatoração preservou o comportamento — que é o que os
testes do passo 2 garantem antes de qualquer corte.

Alíquotas simplificadas para o exemplo. Não é referência fiscal.
"""

from dataclasses import dataclass
from typing import Literal

TipoVenda = Literal["produto", "serviço"]


@dataclass(frozen=True)
class Venda:
    descricao: str
    tipo: TipoVenda
    uf: str
    cidade: str
    valor: float


def formatar_moeda(valor: float) -> str:
    """Formatação em pt-BR. É infraestrutura, não a lição do exemplo — as duas
    versões usam a mesma, para a comparação ser justa."""
    inteiro, centavos = f"{valor:.2f}".split(".")
    return f"{int(inteiro):,}".replace(",", ".") + "," + centavos


# ============================================================================
# ANTES — uma classe só
#
# Cálculo fiscal, agregação, ordenação e formatação convivem no mesmo método.
# Mexer na alíquota do ICMS exige reler o ranking e o rodapé para ter certeza
# de que nada mais quebrou.
# ============================================================================


class RelatorioMonolitico:
    def __init__(self, vendas: list[Venda]) -> None:
        self._vendas = vendas

    def gerar(self) -> str:
        linhas = ["=== Relatório de Faturamento ===", "", "Transações"]

        total_icms = 0.0
        total_iss = 0.0
        bruto = 0.0

        for i, venda in enumerate(self._vendas, start=1):
            bruto += venda.valor

            if venda.tipo == "produto":
                aliquota = 17
                if venda.uf == "SP":
                    aliquota = 18
                elif venda.uf == "RJ":
                    aliquota = 20
                elif venda.uf == "MG":
                    aliquota = 18

                base = venda.valor
                if venda.valor > 5000:
                    base = venda.valor - (venda.valor * 20) / 100

                imposto = (base * aliquota) / 100
                nome_imposto = "ICMS"
                total_icms += imposto
            else:
                aliquota = 2
                if venda.cidade == "São Paulo":
                    aliquota = 5
                elif venda.cidade == "Rio de Janeiro":
                    aliquota = 5
                elif venda.cidade == "Belo Horizonte":
                    aliquota = 3

                imposto = (venda.valor * aliquota) / 100
                nome_imposto = "ISS"
                total_iss += imposto

            linhas.append(
                f"  #{i}  {venda.descricao:<20} {venda.uf}  {venda.tipo:<8} "
                f"R$ {formatar_moeda(venda.valor):>10}   "
                f"{nome_imposto:<4} R$ {formatar_moeda(imposto):>9}"
            )

        linhas += ["", "Impostos por tipo"]
        linhas.append(f"  ICMS   R$ {formatar_moeda(total_icms):>10}")
        linhas.append(f"  ISS    R$ {formatar_moeda(total_iss):>10}")
        linhas.append(f"  Total  R$ {formatar_moeda(total_icms + total_iss):>10}")

        linhas += ["", "Maiores receitas"]
        ordenadas = sorted(self._vendas, key=lambda v: v.valor, reverse=True)[:3]
        for i, venda in enumerate(ordenadas, start=1):
            linhas.append(f"  {i}. {venda.descricao:<20} R$ {formatar_moeda(venda.valor):>10}")

        impostos = total_icms + total_iss
        linhas.append("")
        linhas.append(f"Faturamento bruto    R$ {formatar_moeda(bruto):>10}")
        linhas.append(f"Faturamento líquido  R$ {formatar_moeda(bruto - impostos):>10}")

        return "\n".join(linhas)


# ============================================================================
# DEPOIS — cada lógica no seu lugar
# ============================================================================


def percentual_de(valor: float, percentual: float) -> float:
    """Usado por vários cálculos, mas é utilitário matemático sem regra de
    negócio: vira função, não classe."""
    return (valor * percentual) / 100


ALIQUOTAS_ICMS = {"SP": 18, "RJ": 20, "MG": 18}
ALIQUOTA_ICMS_PADRAO = 17
LIMITE_REDUCAO_BASE = 5000
PERCENTUAL_REDUCAO_BASE = 20


class CalculoIcms:
    """Lógica própria e complexidade própria — alíquota por estado e redução de
    base. Por isso vira classe."""

    def calcular(self, venda: Venda) -> float:
        aliquota = ALIQUOTAS_ICMS.get(venda.uf, ALIQUOTA_ICMS_PADRAO)
        base = venda.valor
        if venda.valor > LIMITE_REDUCAO_BASE:
            base = venda.valor - percentual_de(venda.valor, PERCENTUAL_REDUCAO_BASE)
        return percentual_de(base, aliquota)


ALIQUOTAS_ISS = {"São Paulo": 5, "Rio de Janeiro": 5, "Belo Horizonte": 3}
ALIQUOTA_ISS_PADRAO = 2


class CalculoIss:
    """Mesma razão do ICMS: regra municipal própria."""

    def calcular(self, venda: Venda) -> float:
        return percentual_de(venda.valor, ALIQUOTAS_ISS.get(venda.cidade, ALIQUOTA_ISS_PADRAO))


@dataclass(frozen=True)
class LinhaVenda:
    venda: Venda
    imposto: float
    nome_imposto: str


class ResumoFaturamento:
    """Consolida os números. Não sabe formatar nem como cada imposto é calculado."""

    def __init__(self, vendas: list[Venda]) -> None:
        icms = CalculoIcms()
        iss = CalculoIss()

        self.linhas = [
            LinhaVenda(venda, icms.calcular(venda), "ICMS")
            if venda.tipo == "produto"
            else LinhaVenda(venda, iss.calcular(venda), "ISS")
            for venda in vendas
        ]

        self.total_icms = self._somar_imposto("ICMS")
        self.total_iss = self._somar_imposto("ISS")
        self.bruto = sum(venda.valor for venda in vendas)

    @property
    def total_impostos(self) -> float:
        return self.total_icms + self.total_iss

    @property
    def liquido(self) -> float:
        return self.bruto - self.total_impostos

    def maiores_receitas(self, quantidade: int) -> list[Venda]:
        vendas = [linha.venda for linha in self.linhas]
        return sorted(vendas, key=lambda v: v.valor, reverse=True)[:quantidade]

    def _somar_imposto(self, nome: str) -> float:
        return sum(linha.imposto for linha in self.linhas if linha.nome_imposto == nome)


class RelatorioFormatter:
    """Só transforma o resumo em texto. Não calcula nada."""

    def formatar(self, resumo: ResumoFaturamento) -> str:
        linhas = ["=== Relatório de Faturamento ===", "", "Transações"]

        for i, linha in enumerate(resumo.linhas, start=1):
            linhas.append(
                f"  #{i}  {linha.venda.descricao:<20} {linha.venda.uf}  {linha.venda.tipo:<8} "
                f"R$ {formatar_moeda(linha.venda.valor):>10}   "
                f"{linha.nome_imposto:<4} R$ {formatar_moeda(linha.imposto):>9}"
            )

        linhas += ["", "Impostos por tipo"]
        linhas.append(f"  ICMS   R$ {formatar_moeda(resumo.total_icms):>10}")
        linhas.append(f"  ISS    R$ {formatar_moeda(resumo.total_iss):>10}")
        linhas.append(f"  Total  R$ {formatar_moeda(resumo.total_impostos):>10}")

        linhas += ["", "Maiores receitas"]
        for i, venda in enumerate(resumo.maiores_receitas(3), start=1):
            linhas.append(f"  {i}. {venda.descricao:<20} R$ {formatar_moeda(venda.valor):>10}")

        linhas.append("")
        linhas.append(f"Faturamento bruto    R$ {formatar_moeda(resumo.bruto):>10}")
        linhas.append(f"Faturamento líquido  R$ {formatar_moeda(resumo.liquido):>10}")

        return "\n".join(linhas)


class RelatorioFaturamento:
    """Orquestra: junta as peças. Não calcula imposto nem monta texto."""

    def __init__(self, vendas: list[Venda]) -> None:
        self._vendas = vendas

    def gerar(self) -> str:
        return RelatorioFormatter().formatar(ResumoFaturamento(self._vendas))


VENDAS = [
    Venda("Notebook Pro", "produto", "SP", "São Paulo", 4500),
    Venda("Consultoria", "serviço", "SP", "São Paulo", 8000),
    Venda("Monitor 27", "produto", "RJ", "Rio de Janeiro", 1800),
    Venda("Treinamento", "serviço", "MG", "Belo Horizonte", 3200),
    Venda("Servidor Rack", "produto", "SP", "São Paulo", 12000),
]


def main() -> None:
    monolitico = RelatorioMonolitico(VENDAS).gerar()
    separado = RelatorioFaturamento(VENDAS).gerar()

    print(separado)
    print()
    print(
        "✓ monolítico e separado produziram um relatório idêntico"
        if monolitico == separado
        else "✗ os relatórios divergiram"
    )


if __name__ == "__main__":
    main()
