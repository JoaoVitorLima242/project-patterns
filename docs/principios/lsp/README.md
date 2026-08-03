# Liskov Substitution Principle (LSP)

O LSP serve para organizar as entidades e garantir que os subtipos respeitem o tipo pai.

---

## Antes de falar de Liskov: conjuntos

Pessoal, eu sei que matemática às vezes é chata para algumas pessoas. Mas esse princípio bebe de uma coisa da matemática discreta que a gente usa o tempo todo dentro da programação, mesmo sem chamar pelo nome: **conjuntos**.

Um conjunto é só uma coleção de elementos. `{1, 2, 3}` é um conjunto. "Os números pares" é um conjunto. E o pulo do gato é esse: **um tipo também é um conjunto**. `boolean` é o conjunto `{true, false}`. `Usuario` é o conjunto de todos os valores que são um usuário válido.

```
┌─────────────── Animal ───────────────┐
│   ┌──── Cachorro ────┐   ┌─ Gato ─┐  │
│   │                  │   │        │  │
│   └──────────────────┘   └────────┘  │
└──────────────────────────────────────┘
```

Dessa ideia saem algumas regras — e todas elas reaparecem no LSP.

### Pertinência: `x ∈ A`

Um elemento está no conjunto ou não está, sem meio-termo. É o que a tipagem faz: `rex ∈ Cachorro`. Dizer que um valor "é de um tipo" é dizer que ele pertence àquele conjunto.

### Subconjunto: `B ⊆ A`

`B` é subconjunto de `A` quando **todo** elemento de `B` também é elemento de `A`. Todo cachorro é um animal, então `Cachorro ⊆ Animal`.

É essa a relação que a herança tenta representar. `B extends A` é uma promessa de que `B ⊆ A`.

### A relação é reflexiva e transitiva

- **Reflexiva** — `A ⊆ A`. Todo conjunto é subconjunto de si mesmo.
- **Transitiva** — se `C ⊆ B` e `B ⊆ A`, então `C ⊆ A`.

A transitividade é o que faz uma cadeia de herança funcionar de ponta a ponta. E é também o que torna um erro contagioso: quebrou um degrau, quebrou para todo mundo acima dele.

### Propriedade universal desce para o subconjunto

Aqui está a regra que dá origem ao princípio inteiro:

> Se uma afirmação vale para **todos** os elementos de `A`, e `B ⊆ A`, então ela vale para todos os elementos de `B`.

Se todo animal respira, e todo cachorro é animal, então todo cachorro respira. Não precisa verificar cachorro por cachorro — a garantia vem de graça, junto com o `⊆`.

### Quanto maior o conjunto, menos você pode garantir

E a contrapartida, que é o que dá o custo:

Sobre um elemento de `Animal` você consegue afirmar pouco — respira, e olhe lá. Sobre um elemento de `Cachorro` você afirma muito mais — late, tem quatro patas, é mamífero.

**Conjunto maior, garantias mais fracas. Conjunto menor, garantias mais fortes.** As duas coisas andam em direções opostas, sempre.

## A ideia

E o LSP vem exatamente dessas propriedades.

A gente cria uma classe pai, `User`, e dentro dela ficam as propriedades que todo usuário tem. Daí vêm as subclasses: `Admin`, que tem um tipo e alguma propriedade a mais; e daqui a pouco `Client`, que tem uma propriedade diferente da do `Admin`. Eles são diferentes entre si — mas todos são `User`.

Enquanto isso for verdade no sentido de conjunto, ou seja, enquanto todo `Admin` e todo `Client` estiverem realmente **dentro** de `User`, qualquer código escrito para `User` funciona com os dois sem saber qual dos dois chegou. É a regra da propriedade universal trabalhando a seu favor: o que vale para todo usuário já vale para o admin, de graça.

Eu particularmente gosto desse princípio. Acho muito importante a gente organizar os nossos tipos e definir contratos.

### O difícil é saber o que é do conjunto

O mais complexo, na minha opinião, é separar o que de fato é original daquele conjunto — o que realmente pertence à classe pai.

`Animal` é o exemplo fácil de errar. Dá vontade de dizer que animal anda e colocar `andar()` lá em cima. Mas animal não anda: tem peixe, que nada. No momento em que `andar()` entra em `Animal`, você afirmou uma coisa sobre **todos** os animais que não é verdade — e a primeira subclasse que não anda já nasce quebrando o contrato. E assim vai, para cada comportamento que parece geral e não é.

Repare que aqui é a gangorra dos conjuntos aparecendo de novo. `Animal` é um conjunto grande, e conjunto grande garante pouco. Pôr `andar()` ali é tentar tirar uma garantia forte de um conjunto largo — exatamente o que a regra diz que não dá.

E isso tem uma consequência prática: **nem toda violação de LSP se conserta no filho.** Os exemplos clássicos quase sempre culpam a subclasse — o quadrado que herdou de retângulo e quebrou. Aqui o erro está em cima. Não adianta ajeitar `Peixe`: a correção é tirar `andar()` de `Animal` e deixar lá só o que vale para todos.

Não existe fórmula para acertar isso. O que resolve é sempre tentar ter uma visão clara do que está sendo desenvolvido, para saber organizar as informações de uma forma que seja funcional e organizada.

## Como fica

O exemplo é o `Animal`, em TypeScript.

### Antes — `Animal` promete `andar()`

```ts
abstract class Animal {
  nome: string;

  // Uma promessa feita em nome de todos os filhos, inclusive os que nem existem ainda.
  abstract andar(): string;
}

class Cachorro extends Animal {
  andar(): string {
    return "andando";
  }
}

class Peixe extends Animal {
  // Peixe pertence ao conjunto Animal, mas não cabe no contrato.
  // Não existe implementação honesta aqui — só dá para quebrá-lo.
  andar(): string {
    throw new Error("peixe não anda");
  }
}

// Quem consome só conhece Animal e confia na promessa do pai.
// É este código que a violação quebra, não a subclasse.
function passear(animais: Animal[]): string[] {
  return animais.map((animal) => `${animal.nome} se locomove ${animal.andar()}`);
}
```

### Depois — `Animal` promete `mover()`

```ts
abstract class Animal {
  nome: string;

  // O que vale para TODO animal não é "anda", é "se move".
  // Nesse nível a promessa é verdadeira para o conjunto inteiro.
  abstract mover(): string;
}

class Cachorro extends Animal {
  mover(): string {
    return "andando";
  }
}

class Peixe extends Animal {
  mover(): string {
    return "nadando";
  }
}

// Entrou por último e não exigiu mudança em nada acima dele.
class Passaro extends Animal {
  mover(): string {
    return "voando";
  }
}
```

O `passear` continua igual — mas agora sem `try/catch`, porque não há mais o que dar errado. Repare onde a correção aconteceu: nenhuma linha de `Peixe` foi consertada. O que mudou foi a promessa lá em cima.

▸ [Exemplo completo e executável](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/lsp/typescript/main.ts)

> **Em outras linguagens:** o mesmo exemplo em [Python](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/lsp/python/main.py) e [Java](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/lsp/java/Main.java), com saída idêntica.

## Princípios relacionados

- **[Single Responsibility (SRP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/srp)** — <!-- a diferença -->
- **[Open/Closed (OCP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/ocp)** — <!-- a diferença -->

> Os demais ainda sem link — estão como 🔜 ou 🚧 no [mapa](https://github.com/JoaoVitorLima242/project-patterns/blob/main/README.md).

