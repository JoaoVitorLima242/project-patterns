# Interface Segregation Principle (ISP)

Uma interface não deve obrigar quem a implementa a carregar método que não faz sentido no contexto dela.

---

## O problema

Um contrato de integração raramente nasce grande. Ele engorda: cada coisa nova que o sistema do outro lado passa a expor vira mais um método ali dentro, e ninguém percebe o momento em que ele passou do ponto.

O custo não aparece quando o contrato é escrito. Aparece quando alguém precisa substituí-lo — porque, para ocupar o lugar do que já existe, a implementação nova tem que dar conta de tudo, inclusive das partes que não têm nada a ver com o que se queria trocar.

### O caso que deu origem ao princípio

Robert C. Martin viveu exatamente isso consultando para a **Xerox**, num sistema de impressora novo. Existia uma classe `Job` que fazia tudo: imprimir, grampear, enviar fax. Cada função nova virava mais um método ali, e como todo mundo dependia de `Job`, qualquer mexida obrigava a recompilar e reimplantar o sistema inteiro — uma mudança de uma linha custava um ciclo de uma hora.

A solução foi criar interfaces **por cliente** — `PrintJob`, `StapleJob` — com `Job` implementando todas. Quem imprimia passou a depender só de `PrintJob`.

O enunciado que ele publicou pouco depois, no *The C++ Report*:

> Clientes não devem ser forçados a depender de interfaces que não usam.

E ele tem dois lados, que quase sempre são confundidos:

- **Protege quem implementa** — ninguém é obrigado a escrever método que não sabe cumprir.
- **Protege quem consome** — ninguém carrega dependência de método que nunca chama.

O segundo é o enunciado literal. O primeiro é o que mais dói no dia a dia.

## A ideia

O ISP não é sobre a quantidade de métodos. Interface pequena não é um objetivo em si.

O que eu uso como régua é separar os métodos **pelo contexto** deles, e pelo **ganho** que a separação traz. Se não tem ganho, não separa.

### Quando separar não ganha nada

Pega um adapter de token, que cria e valida token. Não faz sentido virar duas classes.

Os dois métodos são do mesmo contexto — tokenização — e, mais importante que isso: **qualquer implementação vai ter os dois de qualquer jeito.** Quem cria token valida token. Separar aqui não livra ninguém de implementar nada, só espalha em dois arquivos o que sempre anda junto.

Esse é o teste que eu acho mais honesto: **se toda implementação vai acabar implementando as duas partes, a separação não te deu nada.**

### O critério

É um trade-off, e o que funciona pra mim é isto:

1. **Mapear o propósito da interface antes de implementar.** Saber para que ela existe e quem vai consumir, em vez de deixar ela nascer do que a classe por acaso sabe fazer.
2. **Revalidar quando ela crescer.** Contrato grande raramente nasce grande — ele engorda. Quando isso acontecer, olhar de novo e avaliar se vale quebrar em implementações menores.

Não dá para acertar o corte de primeira, e tudo bem. O erro não é começar com uma interface que depois precisa ser dividida — é nunca voltar para dividir.

## Interface também é conjunto

A página do [LSP](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/lsp) começa dizendo que tipo é conjunto. Isso continua valendo aqui, e explica direitinho por que um contrato inchado fica difícil de substituir.

**Uma interface é o conjunto dos objetos que a satisfazem.** E a gangorra reaparece, lida do outro lado:

> Quanto mais o contrato exige, menos objetos cabem nele.

Um contrato com três métodos aceita muita coisa. Um contrato com trinta aceita quase nada. É essa a razão de fundo pela qual trocar uma integração inchada dói: o contrato engordou tanto que o conjunto de implementações possíveis encolheu até caber uma só — a que já estava lá.

E tem uma peça que a herança não oferece: **um objeto pode pertencer a vários conjuntos ao mesmo tempo.** Herança simples obriga a escolher uma linhagem; contratos separados deixam o mesmo objeto ser `Animal` e `Nadante` de uma vez. Implementar várias interfaces é **interseção de conjuntos** — e é isso que torna o ISP possível onde a hierarquia trava.

## Como fica

O exemplo é o mesmo `Animal` da página do LSP, resolvido pelo outro lado.

### Antes — um contrato só, com tudo que algum animal faz

```ts
interface Animal {
  nome: string;
  andar(): string;
  nadar(): string;
  voar(): string;
}

class Peixe implements Animal {
  nome: string;

  // Duas das três capacidades são impossíveis. O contrato exige assim mesmo.
  andar(): string {
    throw new Error("peixe não anda");
  }
  nadar(): string {
    return "nadando";
  }
  voar(): string {
    throw new Error("peixe não voa");
  }
}

// Quem organiza a corrida só precisa de andar(), mas recebe o contrato inteiro
// — e não tem como saber quem realmente anda antes de chamar e quebrar.
function corrida(participantes: Animal[]): string[] {
  return participantes.map((animal) => `${animal.nome} ${animal.andar()}`);
}
```

### Depois — cada capacidade no seu contrato

```ts
interface Animal {
  nome: string;
}
interface Andante {
  andar(): string;
}
interface Nadante {
  nadar(): string;
}
interface Voador {
  voar(): string;
}

class Cachorro implements Animal, Andante, Nadante { /* anda e nada */ }
class Peixe    implements Animal, Nadante          { /* só nada */ }
class Passaro  implements Animal, Andante, Voador  { /* anda e voa */ }

// `&` é interseção: precisa ser as duas coisas ao mesmo tempo.
// Passar um Peixe aqui não compila — o erro saiu do runtime e virou erro de tipo.
function corrida(participantes: (Animal & Andante)[]): string[] {
  return participantes.map((animal) => `${animal.nome} ${animal.andar()}`);
}
```

▸ [Exemplo completo e executável](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/isp/typescript/main.ts)

> **Em outras linguagens:** o mesmo exemplo em [Python](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/isp/python/main.py) e [Java](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/isp/java/Main.java), com saída idêntica. Em Python os contratos são `Protocol`, satisfeitos estruturalmente — a classe não declara nada, basta ter os métodos. Em Java a interseção vira um bound genérico: `<T extends Animal & Andante>`.

### Fatiar ou generalizar?

Vale comparar com o que a página do LSP fez com esse mesmo `Animal`. Lá, a correção foi **generalizar**: `andar()` virou `mover()`, que é verdade para todo o conjunto. Aqui a correção é **fatiar**: cada capacidade vira um contrato à parte.

As duas são legítimas, e o que decide é a forma do problema:

- **Generalizar** serve quando todos fazem a mesma coisa de jeitos diferentes. Todo animal se move — muda o como. Você ganha o polimorfismo: trata todo mundo pela mesma porta.
- **Fatiar** serve quando as capacidades **se cruzam**. Cachorro anda e nada, pássaro anda e voa, peixe só nada. Não existe hierarquia que dê conta disso, porque não é uma árvore — é um grid. Você ganha precisão: o tipo passa a dizer exatamente o que aquele objeto faz.

O preço de fatiar é mais tipos para manter, e a perda da chamada uniforme sobre todos.

## Os sinais de que o contrato engordou

Como perceber a violação sem teoria nenhuma:

- **`UnsupportedOperationException`, `NotImplementedError`, método vazio, método que devolve `null` só para cumprir tabela.** Alguém foi obrigado a assinar o que não cumpre.
- **O mock do teste.** Se para testar *uma* chamada você precisa stubar doze métodos, o contrato não é de ninguém. É o detector mais honesto que existe, porque o teste é o cliente mais exigente que você tem.
- **Ripple de build.** Mexeu na interface e recompilou módulo que não tem nada a ver — a dor original da Xerox.
- **A hora de trocar.** Se substituir a implementação assusta, o contrato está exigindo demais.

## Quando segregar demais

O erro na direção contrária é mais comum hoje do que o contrato gordo, e o critério lá de cima já resolve os três casos: se não tem ganho, não separa.

- **Uma interface por método**, aplicada mecanicamente. Vira indireção sem benefício.
- **`IFoo` / `FooImpl`** com uma implementação só, que nunca terá outra. Se a interface tem exatamente os métodos públicos da única classe que a implementa, ela não segregou nada — é cerimônia.
- **Segregar antes do segundo cliente.** O corte certo é descoberto, não previsto. Com um consumidor só, você não tem informação para saber onde cortar, e vai acertar por sorte.

## Princípios relacionados

- **[Single Responsibility (SRP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/srp)** — <!-- a diferença -->
- **[Liskov Substitution (LSP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/lsp)** — <!-- a diferença -->

> Os demais ainda sem link — estão como 🔜 ou 🚧 no [mapa](https://github.com/JoaoVitorLima242/project-patterns/blob/main/README.md).

## Referências

- Robert C. Martin. [The Interface Segregation Principle](https://d3s.mff.cuni.cz/f/teaching/nprg043/extras/martin96-interface_segregation_principle.pdf) (PDF) — *The C++ Report*, 1996. O artigo que formula o princípio, na coluna "Engineering Notebook". Vale saber antes de abrir: os exemplos dele são `Door`/`TimedDoor` e um caixa eletrônico — o caso da Xerox não está aqui.
- Robert C. Martin. *Agile Software Development, Principles, Patterns, and Practices* — Prentice Hall, 2002. É onde ele conta o caso da Xerox e da classe `Job` que deu origem ao princípio.
