# Single Responsibility Principle (SRP)

Uma classe deve ter uma única responsabilidade: realizar uma única tarefa, ser responsável por uma única lógica.

---

## O problema

Já trabalhei com uma classe de relatório que fazia o relatório inteiro sozinha. O cálculo do valor final passava por muitas etapas e muitas regras, e todas moravam ali dentro.

Duas consequências apareceram rápido:

- **Difícil de lidar.** Entender qualquer parte exigia atravessar a classe inteira.
- **Difícil de testar.** Não dava para verificar um cálculo isolado — era preciso montar o relatório todo.

E a pior delas: **qualquer alteração podia gerar um erro na classe inteira**, porque a quantidade de lógica acumulada ali era grande demais para se ter certeza do alcance de uma mudança. Mexer numa regra específica significava reler tudo para garantir que nada mais tinha quebrado.

## A ideia

Separar, de modo que cada classe responda por uma coisa só.

Mas vale ser claro sobre o objetivo, porque ele é o que decide todo o resto: **no fim do dia não queremos só seguir o padrão — queremos deixar o projeto mais simples e mais fácil de manter.** Separar a lógica serve para que quem olhar saiba exatamente o que aquilo faz.

Seguir o SRP não é a meta. A meta é o código legível e sustentável; o SRP é um meio. Sempre que a aplicação do princípio estiver deixando o projeto mais difícil de entender, é a aplicação que está errada.

### Sobre "um só motivo para mudar"

A formulação mais conhecida do princípio é a de Robert C. Martin: *uma classe deve ter um só motivo para mudar*. É uma definição interessante, e mais precisa que "uma tarefa" — mas, na prática, **não fica clara para o dev**.

Na hora de escrever código, "quantos motivos para mudar essa classe tem?" é uma pergunta difícil de responder com honestidade: os motivos são hipotéticos, dependem de um futuro que ninguém conhece, e é fácil justificar qualquer resposta. O princípio está certo e não ajuda a decidir.

O que ajuda a decidir são sinais concretos — e é disso que trata a seção seguinte.

## O trade-off: quando extrair e quando deixar onde está

Este é o ponto que as pessoas mais erram — e não é o de deixar lógica demais numa classe. É o oposto: **criar classes demais gera uma complexidade desnecessária.**

A pergunta certa não é "essa lógica é uma responsabilidade?", e sim: *essa lógica precisa de uma classe só para ela?* A resposta honesta é "às vezes sim, às vezes não". É uma coisa a se colocar na balança, caso a caso.

### Um caso onde geralmente não vale

Uma função de criação de usuário em que todo usuário recebe um username gerado aleatoriamente, no formato `{name-lowercase}-{numero}-{animal}`.

É lógica, é uma tarefa distinta — e ainda assim, na maioria das vezes, não justifica uma classe própria. Ela nasce e morre ali, não é usada em outro lugar, e extrair só adiciona um arquivo a mais para navegar.

### Um caso onde geralmente vale

Uma função de pagamentos com regras de taxa sobre o valor processado.

A lógica pode até ser simples — mais simples que a do username. O que muda a decisão não é a complexidade, são dois outros fatores:

- **É reutilizada.** A mesma regra de taxa aparece em mais de um lugar.
- **Facilita o teste.** Isolada, ela pode ser verificada diretamente, sem passar pelo fluxo de pagamento inteiro.

O tamanho da lógica, sozinho, não decide nada.

## Como refatorar uma classe que já cresceu demais

O que eu faria hoje com aquela classe de relatório, em três passos:

**1. Mapear o que existe ali dentro.** Antes de mexer em qualquer linha, estudar de fato todas as responsabilidades da classe e escrever um levantamento de tudo que aquele método e aquela classe fazem. Sem esse inventário, a refatoração vira adivinhação.

**2. Cobrir com testes unitários antes de quebrar.** Escrever testes em cima da classe inteira, travando os resultados esperados. É a rede de segurança: como o problema original é justamente "qualquer alteração pode quebrar algo distante", só os testes dão a confiança de que o comportamento continua o mesmo depois do corte.

**3. Quebrar pelas lógicas principais**, escolhendo a saída certa para cada uma:

- Cálculo tributário X e cálculo tributário Y → **uma classe cada**. Cada um tem lógica própria e complexidade própria.
- Cálculo de porcentagem e outros cálculos matemáticos → **método separado**, porque são usados em vários lugares.

A ordem importa: mapear antes de testar, testar antes de cortar.

## Implementação

O exemplo é o relatório de faturamento: ele lista as transações com o imposto de cada uma, soma quanto foi de ICMS e quanto foi de ISS, ranqueia as maiores receitas e fecha com o faturamento bruto e líquido.

Cada arquivo traz **as duas versões** — a monolítica e a separada — e o `main` compara os dois relatórios gerados. Eles saem idênticos, caractere por caractere: é o passo 2 acontecendo dentro do próprio exemplo, provando que o corte não mudou o comportamento.

O corte ficou assim:

| Unidade | Forma | Por quê |
| --- | --- | --- |
| `CalculoIcms` | classe | Lógica própria e complexidade própria: alíquota por estado e redução de base |
| `CalculoIss` | classe | Mesma razão — regra municipal própria |
| `ResumoFaturamento` | classe | Consolida os números: totais, ranking, líquido |
| `RelatorioFormatter` | classe | Só vira texto; não calcula nada |
| `RelatorioFaturamento` | orquestra | Junta as peças |
| `percentualDe()` | função | Usado em vários lugares, mas é utilitário matemático sem regra de negócio |

Repare no que acontece com o `gerar()`: na versão monolítica ele tem umas sessenta linhas; na separada, três.

<details>
<summary><b>A saída do programa</b></summary>

```
=== Relatório de Faturamento ===

Transações
  #1  Notebook Pro         SP  produto  R$   4.500,00   ICMS R$    810,00
  #2  Consultoria          SP  serviço  R$   8.000,00   ISS  R$    400,00
  #3  Monitor 27           RJ  produto  R$   1.800,00   ICMS R$    360,00
  #4  Treinamento          MG  serviço  R$   3.200,00   ISS  R$     96,00
  #5  Servidor Rack        SP  produto  R$  12.000,00   ICMS R$  1.728,00

Impostos por tipo
  ICMS   R$   2.898,00
  ISS    R$     496,00
  Total  R$   3.394,00

Maiores receitas
  1. Servidor Rack        R$  12.000,00
  2. Consultoria          R$   8.000,00
  3. Notebook Pro         R$   4.500,00

Faturamento bruto    R$  29.500,00
Faturamento líquido  R$  26.106,00

✓ monolítico e separado produziram um relatório idêntico
```

As três linguagens imprimem exatamente este texto.

</details>

<details>
<summary><b>TypeScript</b></summary>

**Antes** — cálculo, agregação, ordenação e formatação no mesmo método:

```ts
gerar(): string {
  for (let i = 0; i < this.vendas.length; i++) {
    const venda = this.vendas[i];
    bruto += venda.valor;

    if (venda.tipo === "produto") {
      let aliquota = 17;
      if (venda.uf === "SP") aliquota = 18;
      else if (venda.uf === "RJ") aliquota = 20;
      else if (venda.uf === "MG") aliquota = 18;

      let base = venda.valor;
      if (venda.valor > 5000) base = venda.valor - (venda.valor * 20) / 100;

      imposto = (base * aliquota) / 100;
      totalIcms += imposto;
    } else {
      // o mesmo bloco de novo, agora com as alíquotas de ISS por município
    }

    linhas.push(`  #${i + 1}  ${venda.descricao.padEnd(20)} ...`);
  }

  // e ainda seguem, aqui dentro: somatórios, ranking e rodapé
}
```

**Depois** — cada lógica no seu lugar:

```ts
// Utilitário matemático, sem regra de negócio: função, não classe.
function percentualDe(valor: number, percentual: number): number {
  return (valor * percentual) / 100;
}

// Lógica própria e complexidade própria: classe.
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

// Orquestra: não calcula imposto nem monta texto.
class RelatorioFaturamento {
  gerar(): string {
    return new RelatorioFormatter().formatar(new ResumoFaturamento(this.vendas));
  }
}
```

▸ [Exemplo completo e executável](./typescript/main.ts)

</details>

<details>
<summary><b>Python</b></summary>

**Antes** — cálculo, agregação, ordenação e formatação no mesmo método:

```python
def gerar(self) -> str:
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
            total_icms += imposto
        else:
            ...  # o mesmo bloco de novo, agora com as alíquotas de ISS

        linhas.append(f"  #{i}  {venda.descricao:<20} ...")

    # e ainda seguem, aqui dentro: somatórios, ranking e rodapé
```

**Depois** — cada lógica no seu lugar:

```python
def percentual_de(valor: float, percentual: float) -> float:
    """Utilitário matemático, sem regra de negócio: função, não classe."""
    return (valor * percentual) / 100


class CalculoIcms:
    """Lógica própria e complexidade própria: classe."""

    def calcular(self, venda: Venda) -> float:
        aliquota = ALIQUOTAS_ICMS.get(venda.uf, ALIQUOTA_ICMS_PADRAO)
        base = venda.valor
        if venda.valor > LIMITE_REDUCAO_BASE:
            base = venda.valor - percentual_de(venda.valor, PERCENTUAL_REDUCAO_BASE)
        return percentual_de(base, aliquota)


class RelatorioFaturamento:
    """Orquestra: não calcula imposto nem monta texto."""

    def gerar(self) -> str:
        return RelatorioFormatter().formatar(ResumoFaturamento(self._vendas))
```

▸ [Exemplo completo e executável](./python/main.py)

</details>

<details>
<summary><b>Java</b></summary>

**Antes** — cálculo, agregação, ordenação e formatação no mesmo método:

```java
String gerar() {
    for (int i = 0; i < vendas.size(); i++) {
        Venda venda = vendas.get(i);
        bruto += venda.valor();

        if (venda.tipo().equals("produto")) {
            int aliquota = 17;
            if (venda.uf().equals("SP")) aliquota = 18;
            else if (venda.uf().equals("RJ")) aliquota = 20;
            else if (venda.uf().equals("MG")) aliquota = 18;

            double base = venda.valor();
            if (venda.valor() > 5000) base = venda.valor() - (venda.valor() * 20) / 100;

            imposto = (base * aliquota) / 100;
            totalIcms += imposto;
        } else {
            // o mesmo bloco de novo, agora com as alíquotas de ISS por município
        }

        linhas.add(String.format("  #%d  %-20s ...", i + 1, venda.descricao()));
    }

    // e ainda seguem, aqui dentro: somatórios, ranking e rodapé
}
```

**Depois** — cada lógica no seu lugar:

```java
// Em Java não existe função solta: o equivalente do utilitário é um método
// estático. O ponto se mantém — não é classe com estado nem com regra de negócio.
final class Calculos {
    static double percentualDe(double valor, double percentual) {
        return (valor * percentual) / 100;
    }
}

// Lógica própria e complexidade própria: classe.
class CalculoIcms {
    double calcular(Venda venda) {
        int aliquota = ALIQUOTAS.getOrDefault(venda.uf(), ALIQUOTA_PADRAO);
        double base = venda.valor() > LIMITE_REDUCAO_BASE
                ? venda.valor() - Calculos.percentualDe(venda.valor(), PERCENTUAL_REDUCAO_BASE)
                : venda.valor();
        return Calculos.percentualDe(base, aliquota);
    }
}

// Orquestra: não calcula imposto nem monta texto.
class RelatorioFaturamento {
    String gerar() {
        return new RelatorioFormatter().formatar(new ResumoFaturamento(vendas));
    }
}
```

▸ [Exemplo completo e executável](./java/Main.java)

</details>

> As alíquotas são simplificadas para o exemplo. Não é referência fiscal.

## Princípios relacionados

> Ainda sem link — estes tópicos estão como 🔜 ou 🚧 no [mapa](../../../README.md).

<!-- FALTA -->

## Referências

- André Benjamim. [S.O.L.I.D.: Princípio da Responsabilidade Única](https://www.campuscode.com.br/conteudos/s-o-l-i-d-principio-da-responsabilidade-unica) — Campus Code.
- [@jonesroberto](https://medium.com/@jonesroberto). [Os princípios do SOLID: SRP — Princípio da Responsabilidade Única](https://medium.com/@jonesroberto/os-princ%C3%ADpios-do-solid-srp-princ%C3%ADpio-da-responsabilidade-%C3%BAnica-7897c55694fe) — Medium.
