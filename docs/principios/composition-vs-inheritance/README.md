# Composição vs. Herança

<!-- A frase de abertura, no seu jeito de dizer. -->

---

## O que é herança

Herança é declarar que uma classe **é um** tipo de outra.

```ts
class Animal {
  nome: string;

  respirar(): string {
    return `${this.nome} respirando`;
  }
}

// Cachorro não escreveu respirar() e mesmo assim tem.
class Cachorro extends Animal {
  latir(): string {
    return `${this.nome} latindo`;
  }
}
```

O `extends` dessa linha faz **duas** coisas ao mesmo tempo, e é importante separar quais:

1. **Reúso** — `Cachorro` ganha os campos e os métodos de `Animal` sem reescrever nada.
2. **Identidade** — `Cachorro` pode ser usado em qualquer lugar que espera um `Animal`. É a relação de subtipo.

Herança entrega as duas juntas, num pacote só. E daí vem boa parte dos problemas: quase sempre você quer uma das duas e leva as duas.

Tem uma terceira coisa que vem junto e ninguém pede: a subclasse enxerga o que é `protected` e passa a depender de como o pai funciona por dentro. O GoF chama isso de **reúso caixa-branca** — você reusa vendo as tripas.

## O que é composição

Composição é declarar que uma classe **tem um** objeto de outra, guardado como campo, e usa esse objeto para fazer o trabalho.

```ts
class Motor {
  ligar(): string {
    return "motor ligado";
  }
}

class Carro {
  private motor: Motor;

  constructor(motor: Motor) {
    this.motor = motor;
  }

  // Delegação explícita: o Carro decide o que expor e o que esconder.
  ligar(): string {
    return this.motor.ligar();
  }
}
```

`Carro` **não é** um `Motor`. Não pode ser usado onde se espera um `Motor`, e não herdou nada. Ele só usa.

O que muda em relação à herança:

1. **Só a interface pública.** O `Carro` enxerga do `Motor` o que qualquer um enxergaria. É o **reúso caixa-preta**.
2. **Você escolhe o que expor.** Nada vaza por acidente; o que aparece na fachada do `Carro` é o que ele escreveu ali.
3. **Dá para trocar em runtime.** O motor entra pelo construtor, e pode ser outro amanhã.
4. **Dá para ter vários.** Um `Carro` tem motor, rodas e câmbio. Pais, você só tem um.

## Injetar é compor

Vale dizer com todas as letras, porque nem sempre é óbvio: **quando você injeta uma dependência, você está compondo.** O objeto entra pelo construtor e vira um campo — a classe passa a *ter* aquilo. A injeção é só o jeito de o objeto chegar; a relação é composição.

Ou seja, se você já trabalha com caso de uso recebendo as dependências pelo construtor, você já escreve composição o dia inteiro, mesmo sem chamar assim.

## A pergunta: capacidade ou identidade?

Na prática, o que eu faço é separar duas situações que parecem a mesma e não são.

**Quando eu preciso de uma funcionalidade.** Um caso de uso de criar conta precisa enviar e-mail. O que eu quero na real é **adicionar uma propriedade que a classe não possuía** — e para isso eu não preciso herdar nada. O caso de uso não vira um serviço de e-mail; ele passa a ter um.

**Quando a classe é alguma coisa.** Esse mesmo caso de uso *é* um caso de uso. Ele depende do contrato que obriga a existir um `exec()`. Aí sim eu quero a relação de tipo, porque quem chama precisa poder tratar todos os casos de uso do mesmo jeito.

É a separação da seção lá em cima, em uso: `extends` entrega reúso e identidade no mesmo pacote, e essas duas perguntas quase nunca aparecem juntas. **Capacidade se resolve compondo. Identidade se resolve com contrato.**

### E "herdar o contrato" não é bem herdar

Um detalhe que muda o custo da decisão: quando o contrato de caso de uso é uma **interface**, implementar não é herdar implementação nenhuma. Você ganha a identidade e **não** ganha tripa junto — nada de método protegido, nada de dependência de como o pai funciona por dentro.

Se o contrato fosse uma classe abstrata com código dentro, aí sim voltariam os dois pacotes juntos, com o acoplamento que vem no meio.

E tem um custo que só aparece depois: **a vaga de herança é uma só.** Se você gastar o `extends` para ganhar uma capacidade, ela acabou — e quando o contrato de caso de uso chegar, não sobra lugar para ele.

## O custo da herança é uma afirmação

Herdar tem um custo, e ele não é técnico: ao escrever `extends`, você está definindo que aquela classe **é como a classe pai**. Quando a gente herda alguma coisa, o que a gente quer dizer é que aquela classe **faz parte daquele conjunto**.

E afirmação de conjunto cobra. Como a página do [LSP](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/lsp) registra, tudo que é demonstrável sobre o pai passa a valer para o filho — e essa promessa vale para a classe inteira, para todo mundo que enxergar aquele tipo, para sempre.

Compor é bem diferente. Quando a gente compõe, **não define nada de forma geral**: só pega algo emprestado. O caso de uso pega emprestado o envio de e-mail e usa ali dentro. Ele não disse nada sobre o que ele é, não entrou em conjunto nenhum, não assumiu obrigação com ninguém.

> **Herança é uma afirmação. Composição é um empréstimo.**

Por isso a conta não é a mesma. Emprestar não obriga a nada: se o objeto emprestado mudar de comportamento amanhã, quem compôs decide o que fazer com isso. Quem afirmou está preso à afirmação.

## Como fica

Um caso de uso de criar conta que precisa mandar e-mail de boas-vindas.

### Antes — herda o serviço de e-mail para ganhar `enviar()`

```ts
class ServicoDeEmail {
  enviar(para: string, assunto: string): void {
    console.log(`[email] para=${para} assunto=${assunto}`);
  }
}

// Herdou só para reusar enviar(). Mas levou a identidade junto:
// agora CriarConta É um ServicoDeEmail, o que não é verdade.
class CriarConta extends ServicoDeEmail {
  exec(email: string): string {
    this.enviar(email, "Bem-vinda");
    return `Conta criada: ${email}`;
  }
}
```

Três coisas quebraram de uma vez. O público do pai **vazou para a fachada**: quem tem um `CriarConta` pode chamar `criarConta.enviar()` de fora, e isso não devia ser parte do caso de uso. Trocar o provedor de e-mail virou trocar a superclasse. E a **vaga de herança foi gasta** — quando o contrato de caso de uso chegar, não sobra `extends` para ele.

### Depois — implementa o contrato, injeta a capacidade

```ts
// Identidade: o que faz esta classe SER um caso de uso.
interface CasoDeUso<I, O> {
  exec(input: I): O;
}

// Capacidade: o que a classe precisa TER para fazer o trabalho.
interface EnviadorDeEmail {
  enviar(para: string, assunto: string): void;
}

class CriarConta implements CasoDeUso<string, string> {
  private email: EnviadorDeEmail;

  constructor(email: EnviadorDeEmail) {
    this.email = email;
  }

  exec(email: string): string {
    this.email.enviar(email, "Bem-vindo");
    return `Conta criada: ${email}`;
  }
}
```

A fachada do `CriarConta` agora tem exatamente um método: `exec`. O e-mail é um campo, não parte do que ele é. Trocar o provedor é passar outro objeto no construtor — e no teste, um que não manda e-mail nenhum.

▸ [Exemplo completo e executável](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/composition-vs-inheritance/typescript/main.ts)

> **Em outras linguagens:** o mesmo exemplo em [Python](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/composition-vs-inheritance/python/main.py) e [Java](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/composition-vs-inheritance/java/Main.java), com saída idêntica.

## Princípios relacionados

- **[Liskov Substitution (LSP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/lsp)** — o LSP é a conta que a herança cobra. Ao escrever `extends`, você promete que tudo que vale para o pai vale para o filho; compor não cobra esse preço porque não promete identidade nenhuma.
- **[Interface Segregation (ISP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/isp)** — é lá que aparece a ideia de que capacidades cruzadas não formam uma árvore, e sim um grid. Herança só sabe expressar árvore; é por isso que capacidade se resolve compondo.
- **[Dependency Inversion (DIP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/dip)** — injetar é compor. O DIP acrescenta o que deve entrar pelo construtor: o contrato, não a implementação.
