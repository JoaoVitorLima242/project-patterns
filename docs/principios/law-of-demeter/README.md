# Lei de Demeter

<!-- A frase de abertura, no seu jeito de dizer. Uma linha: o que a lei te dá. -->

---

É a regra mais fácil de verificar de toda esta seção: dá para vê-la sendo violada no diff, sem entender o domínio, sem ler o resto da classe. Sempre que uma linha encadeia acessos para atravessar objetos — `a.b.c.fazer()` —, ela está avisando que aquele código sabe demais sobre a estrutura de coisas que não são dele.

E o motivo de conhecer a lei é esse: ela transforma um incômodo vago com aquela linha comprida em um critério objetivo, que se aplica na hora de escrever, não meses depois.

## O que a lei diz

Nasceu em 1987 no **projeto Demeter**, na Northeastern University, com Ian Holland e Karl Lieberherr — o nome vem da deusa grega da agricultura, porque o projeto tratava de "cultivar" software em camadas. A formulação popular é curta:

> **Só fale com seus amigos imediatos.**

A versão formal é uma lista. Um método `m` de um objeto `O` só deveria chamar métodos de:

1. o próprio `O`;
2. objetos passados como **parâmetro** de `m`;
3. objetos **criados dentro** de `m`;
4. objetos que são **atributos diretos** de `O`.

O que sobra de fora é justamente o que a lei proíbe: chamar método de um objeto que você obteve **através de outro objeto**. Ou seja, não é sobre a quantidade de pontos numa linha — é sobre quantos **saltos de conhecimento** o seu código dá para chegar onde quer.

## O sintoma

O nome clássico é *train wreck* — o trem descarrilando:

```ts
// A classe que escreve isso conhece: Pedido, Cliente, Endereco e Cidade.
const cidade = pedido.cliente.endereco.cidade.nome;
```

Uma linha, quatro tipos conhecidos, três saltos. E o preço não é estético: **qualquer mudança em qualquer um desses quatro tipos pode quebrar essa linha.** Se `Endereco` deixar de ter `Cidade` e passar a ter um `Localizacao`, quem paga é um código que nem deveria saber que `Endereco` existe.

É por isso que a Lei de Demeter é, no fundo, uma régua de acoplamento: cada salto é uma dependência a mais, e ela aparece escrita na linha, contável a olho nu.

## A saída: peça, não pergunte

A correção quase nunca é criar um atalho `pedido.cidadeDoCliente`. É inverter quem faz o trabalho:

```ts
// Em vez de puxar os dados até aqui para decidir...
if (pedido.cliente.endereco.cidade.nome === "São Paulo") {
  frete = 0;
}

// ...peça ao objeto que sabe.
if (pedido.temEntregaGratuita()) {
  frete = 0;
}
```

Isso tem nome próprio — **Tell, Don't Ask** — e é o mesmo movimento: em vez de perguntar o estado de alguém para tomar a decisão no lugar dele, mande fazer e deixe a decisão morar junto dos dados.

O ganho real aparece quando a regra muda. Entrega gratuita virou "acima de R$ 200 na região Sul"? Muda dentro de `Pedido`, e nenhum dos chamadores fica sabendo.

## O que a lei não proíbe

Aqui mora a maior parte dos falsos positivos:

- **Interface fluente e builder.** `sb.append("a").append("b")` não viola nada: cada chamada devolve **o mesmo objeto**, não um vizinho novo. Não há salto, há repetição.
- **Estrutura de dados pura.** A lei fala de objetos, que escondem estado e expõem comportamento. Um DTO, um `record`, um JSON desserializado — esses existem justamente para ter os dados acessíveis, e navegar por eles não é violação. Como Robert Martin coloca, a confusão vem de tratar como objeto algo que é só uma estrutura de dados com métodos.
- **Encadeamento dentro do próprio agregado**, quando os objetos internos existem só para organizar o estado da raiz e não são conhecidos por ninguém de fora.

O teste que separa os casos: pergunte se a linha está **navegando pela estrutura de outro objeto** ou apenas **encadeando operações sobre o mesmo dado**. Só o primeiro é o problema.

## O custo de aplicar ao pé da letra

A crítica mais conhecida à lei é real e vale registrar: seguida literalmente, ela produz **delegação em cascata**. Para não navegar até `Cidade`, cria-se um método em `Endereco`, outro em `Cliente`, outro em `Pedido` — três métodos que não fazem nada além de repassar a chamada.

Trocou-se um acoplamento visível por um monte de código de encanamento, e a estrutura continua lá, só que espalhada. Por isso o próprio Lieberherr sempre tratou aquilo como **heurística de estilo**, não como lei — o nome pegou melhor do que merecia.

O sinal de que a delegação está errada é quando ela não tem nome de negócio: `pedido.nomeDaCidadeDoCliente()` é o train wreck disfarçado. `pedido.temEntregaGratuita()` é a lei aplicada de verdade.

## Na prática

<!-- SUA OPINIÃO — a seção que carrega a página, desenvolvida na conversa.
     O que ainda precisa de você:

     - Você vê isso no dia a dia? Onde a cadeia de gets mais aparece nos
       projetos em que você trabalha — domínio, resposta de API, ORM?
     - A lei te parece aplicável de verdade, ou é daquelas que soa bem e não
       sobrevive ao prazo?
     - Onde você aceitaria o encadeamento sem reclamar? -->

## Trade-offs

<!-- Preencher junto com a seção acima. -->

| Ganha | Paga |
| --- | --- |
|  |  |

## Princípios relacionados

- **[Acoplamento e Coesão](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/coupling-and-cohesion)** — a Lei de Demeter é acoplamento medido em saltos. É o caso raro em que dá para contar a dependência a olho nu, na própria linha.
- **[Single Responsibility (SRP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/srp)** — o *Tell, Don't Ask* devolve a decisão para quem tem os dados; quem estava perguntando tinha assumido uma responsabilidade que não era dele.
- **[Dependency Inversion (DIP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/dip)** — os dois reduzem o que uma classe precisa conhecer: o DIP troca implementação por contrato, Demeter corta os intermediários que ela nem deveria enxergar.

## Referências

- Karl Lieberherr e Ian Holland, *Assuring Good Style for Object-Oriented Programs* (1989) — a formulação original, como heurística de estilo.
- Robert C. Martin, *Clean Code* (2008), capítulo 6 — a distinção entre objeto e estrutura de dados, que resolve boa parte dos falsos positivos.
- Martin Fowler, *GetterEradicator* (2005) — sobre quando mover o comportamento para junto dos dados vale a pena.
