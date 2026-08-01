# Open/Closed Principle (OCP)

Aberto para extensão, fechado para modificação: em vez de alterar uma classe que já funciona, você a estende.

---

## O problema

A classe de e-mail já existe e já funciona. Ela tem um método `enviarEmail`, ele está em produção, ninguém reclama.

Aí chega o pedido: agora também precisa enviar e-mail de aniversário.

O caminho curto é abrir a classe e acrescentar o caso novo — um parâmetro de tipo, um `if`, e pronto. Funciona. O problema é que esse caminho se repete: e-mail de boas-vindas, de cobrança, de recuperação de senha. Cada pedido novo é uma edição na mesma classe, e a cada edição você está mexendo em código que já estava certo, com risco de quebrar o que já funcionava por causa de um caso que nem existia antes.

## A ideia

Em vez de modificar a classe, estender.

O e-mail de aniversário não vira um método novo dentro da classe de e-mail. Ele vira uma extensão que usa a classe de e-mail, passando as informações do aniversário. A classe original não é tocada — ela continua fazendo exatamente o que fazia, e o comportamento novo mora em outro lugar.

Quando o método é simples, isso é simples de fazer.

### Herança ou injeção?

A formulação clássica do princípio fala em **herança**: você cria uma subclasse e sobrescreve o comportamento. É de onde vem a palavra "extensão".

Não é como eu faço. **Eu prefiro injeção.** Por hábito, eu crio um contrato de caso de uso com um método único — `exec()` — e injeto as dependências. Com um método só, sobrescrever não é uma opção que faça sentido: o `exec()` é a coisa inteira. Então o ponto de extensão não é uma subclasse que reescreve o método, é uma dependência que entra pelo construtor.

O princípio é o mesmo — código novo em vez de código editado. O mecanismo é que muda.

## Como fica

O exemplo é o e-mail, em TypeScript.

### Antes — a classe cresce a cada pedido

```ts
type TipoEmail = "boasVindas" | "aniversario";

class EnviarEmail {
  async exec(input: { userId: string; tipo: TipoEmail }): Promise<string> {
    const user = await this.repo.find(input.userId);

    // Cada tipo novo de e-mail é uma edição aqui dentro.
    if (input.tipo === "aniversario") {
      return `Para ${user.nome}: Feliz aniversário!`;
    }
    return `Para ${user.nome}: Bem-vinda!`;
  }
}
```

### Depois — o conteúdo entra injetado

```ts
type Email = { assunto: string; corpo: string };

// O ponto de extensão: quem quiser um e-mail novo implementa isto.
interface ConteudoEmail {
  montar(user: User): Email;
}

class EnviarEmail {
  private conteudo: ConteudoEmail;

  constructor(conteudo: ConteudoEmail) {
    this.conteudo = conteudo;
  }

  // Não sabe QUAL e-mail está montando — só sabe enviar.
  async exec(input: { userId: string }): Promise<Email> {
    const user = await this.repo.find(input.userId);
    return this.conteudo.montar(user);
  }
}

class ConteudoAniversario implements ConteudoEmail {
  montar(user: User): Email {
    return { assunto: "Feliz aniversário!", corpo: `Hoje é seu dia, ${user.nome}.` };
  }
}
```

O e-mail de cobrança, agora, é uma classe nova. `EnviarEmail` não é aberta.

## O que a maioria dos textos não conta

Até aqui é o exemplo bonito, e é onde quase todo texto sobre OCP para. O meu maior desafio com o princípio é outro, e aparece assim que o ponto de extensão precisa de **uma validação a mais** — uma que precisa rodar dentro da classe e cujo *dado* também é necessário depois.

Foi o que aconteceu comigo: o método ficou tão abstrato que as informações necessárias estavam em lógicas distintas, e **a mesma requisição acabou sendo feita duas vezes.**

```ts
interface UseCase<In, Out> {
  exec(input: In): Promise<Out>;
}

class ValidarAssinatura implements UseCase<{ userId: string }, boolean> {
  async exec({ userId }: { userId: string }): Promise<boolean> {
    const user = await this.repo.find(userId); // 1ª busca
    return user.assinaturaAtiva;
  }
}

class EnviarEmail implements UseCase<{ userId: string }, Email> {
  async exec({ userId }: { userId: string }): Promise<Email> {
    if (!(await this.validar.exec({ userId }))) throw new Error("sem assinatura");
    const user = await this.repo.find(userId); // 2ª busca — o mesmo user
    return this.conteudo.montar(user);
  }
}
```

`ValidarAssinatura` tinha o `user` na mão. Mas o contrato devolve `boolean`, então quem chamou busca de novo.

E repare de onde vem o custo: **não é do OCP.** Vem de um contrato que devolve menos do que já sabe. Um `exec()` de método único, com retorno enxuto, empurra exatamente para isso. O preço real não é a indireção de uma chamada a mais — é a engenharia extra necessária para reaproveitar o que já foi buscado.

### As três saídas

| Saída | Como | Custo |
| --- | --- | --- |
| O contrato devolve o que já sabe | `ValidarAssinatura` retorna o `user`, não `boolean` | Engorda o retorno de todo contrato; a validação passa a expor dado |
| **I/O na borda** | Quem orquestra busca uma vez e passa a entidade; os pontos de extensão não fazem I/O | Os contratos deixam de ser autônomos — dependem de receber o dado pronto |
| Um orquestrador busca e distribui | Uma camada acima resolve os dados e alimenta cada extensão | É a "engenharia mais complexa"; só se paga com muitos pontos de extensão |

A do meio, aplicada ao exemplo:

```ts
interface RegraEnvio {
  permite(user: User): boolean;
}

class EnviarEmail implements UseCase<{ userId: string }, Email> {
  // Busca UMA vez. Os pontos de extensão recebem o dado pronto e não fazem I/O.
  async exec({ userId }: { userId: string }): Promise<Email> {
    const user = await this.repo.find(userId);
    if (!this.regra.permite(user)) throw new Error("bloqueado");
    return this.conteudo.montar(user);
  }
}
```

`RegraEnvio` e `ConteudoEmail` continuam sendo pontos de extensão — mas agora são funções puras sobre um `user` que já está em memória. Uma busca, não duas.

## Meu critério: o caminho inverso

O princípio, como se costuma ensinar, pede que você deixe a classe fechada para alteração e aberta para extensão **desde o começo**. Isso exige prever o futuro da classe — saber, antes de precisar, por onde ela vai variar.

Eu acho o caminho inverso mais fácil, e é o que eu faço:

1. Escrevo o método direto, resolvendo o caso que existe.
2. Em algum momento reparo que aquele método ficou muito reutilizado.
3. **Aí** eu analiso e vejo que faz sentido extrair um método mais simples, para servir de ponto de extensão.

A diferença é que no passo 3 eu não estou adivinhando por onde a classe vai variar — eu já vi. O ponto de extensão nasce de evidência, não de previsão.

## Quando OCP deixa de ser a pergunta

Refatorar sempre exige garantir que o código continua de acordo. Quando o que você mexe é consumido por terceiros — uma API pública, uma lib compartilhada — todo retorno alterado gera impacto fora do seu alcance, e o nível de complexidade sobe.

Só que esse tipo de complexidade tem que ser planejado, e **aí o problema deixa de ser OCP.** Passa a ser qual arquitetura usar, quais métodos de teste, qual o alinhamento em cima de cada retorno — porque aquilo está sendo usado por um público. OCP é uma ferramenta de decisão dentro do seu próprio código; ela não tem nada a dizer sobre coordenar quem consome você.

O critério da seção anterior vale onde OCP é a pergunta certa.

## Princípios relacionados

- **[Single Responsibility (SRP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/srp)** — o SRP decide *o que* vira uma unidade separada; o OCP decide *como* uma unidade nova entra sem editar as antigas.
