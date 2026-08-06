# Encapsulamento

Guardar junto o dado e quem pode mexer nele — e não deixar mais ninguém entrar.

---

É o pilar mais antigo dos quatro e o mais mal aplicado. Quase todo mundo aprende como "usar `private` e criar getter e setter", e essa versão não só perde o ponto como produz exatamente o que o encapsulamento existe para evitar: um objeto cujo estado interno está aberto para o mundo, só que com mais cerimônia.

O conceito de verdade é anterior à orientação a objetos e mais amplo que ela.

## O que é

Encapsulamento junta duas ideias que costumam ser tratadas como uma:

1. **Agrupar** — o dado e o comportamento que opera sobre ele ficam na mesma unidade. O saldo mora junto das operações que alteram o saldo.
2. **Esconder** — quem está de fora enxerga só o que precisa para usar a unidade. O resto é assunto interno, e ninguém pode depender dele.

A segunda é a que carrega o valor, e tem nome próprio: **ocultação de informação** (*information hiding*). Ela vem de David Parnas, em 1972, num artigo sobre como decompor sistemas em módulos — e a tese dele era que a divisão não deveria seguir o fluxo do programa, mas sim **esconder as decisões que têm chance de mudar**. Cada módulo guarda um segredo; quando o segredo muda, só ele muda.

Isso é anterior à orientação a objetos e não depende dela. Um módulo com funções exportadas e estado interno encapsula tanto quanto uma classe.

### Para que serve

Duas coisas concretas, e vale separá-las porque uma é sobre correção e a outra sobre manutenção.

**Manter o objeto sempre válido.** Se qualquer código pode escrever no estado, qualquer código pode deixá-lo inconsistente — um pedido sem itens marcado como pago, um saldo negativo numa conta que não permite. Encapsular é dizer que existe **uma porta**, e que quem passa por ela é obrigado a respeitar a regra. A garantia deixa de depender de disciplina de quem chama.

```ts
class Conta {
  #saldo: number;

  constructor(saldoInicial: number) {
    this.#saldo = saldoInicial;
  }

  // A única porta de saída de dinheiro — e ela carrega a regra.
  sacar(valor: number): void {
    if (valor > this.#saldo) throw new Error("Saldo insuficiente");
    this.#saldo -= valor;
  }

  get saldo(): number {
    return this.#saldo;
  }
}
```

Com `#saldo` privado, "conta com saldo negativo" deixa de ser um estado possível. Não é uma convenção que a equipe combina — é a linguagem impedindo.

**Poder mudar por dentro sem quebrar ninguém.** Tudo que está exposto vira contrato: alguém vai depender daquilo, e a partir daí você não muda mais. O que fica escondido continua seu. É o mesmo raciocínio de [acoplamento](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/coupling-and-cohesion) visto de dentro da unidade: **a superfície pública é o tamanho da sua dívida futura.**

## Como aparece no código

O mecanismo mais conhecido é o modificador de acesso, mas ele é só um dos caminhos — e, em TypeScript, nem é o mais forte:

- **`private` do TypeScript** existe só em tempo de compilação. Ele é apagado junto com os tipos: em runtime, o campo está lá, acessível por quem souber o nome.
- **`#campo` do JavaScript** é privado de verdade, garantido pela linguagem em execução. É a diferença entre um aviso e uma tranca.
- **Closure** encapsula sem classe nenhuma: a variável existe dentro da função e só as funções retornadas alcançam.
- **Módulo** é encapsulamento em outra escala. O que não é exportado não existe para o resto do sistema — e essa é, na prática, a fronteira mais importante num projeto TypeScript, mais do que o `private` de qualquer classe.

Vale dizer o que encapsulamento **não** é: proteção contra código malicioso. Não é segurança, é limite de dependência. O objetivo é que ninguém *precise* mexer, não que ninguém *consiga*.

## O encapsulamento aparente

Aqui mora o erro mais comum, e ele é fácil de reconhecer:

```ts
// Nada disso está encapsulado. O estado é público, com mais passos.
class Pedido {
  private itens: Item[] = [];
  private status: string = "aberto";

  getItens(): Item[] { return this.itens; }
  setItens(itens: Item[]): void { this.itens = itens; }
  getStatus(): string { return this.status; }
  setStatus(status: string): void { this.status = status; }
}
```

Se cada campo privado tem um par de `get`/`set` público, o `private` não está escondendo nada — a regra de negócio continua fora, espalhada por quem chama. Quem decide se um pedido pode ser fechado é outro arquivo, e nada impede que dois arquivos decidam diferente.

Dois agravantes que aparecem juntos:

- **Vazamento por referência.** `getItens()` devolve o array interno. Quem recebe pode dar `push` e alterar o pedido por fora, sem passar por método nenhum — o `private` virou decoração.
- **Modelo anêmico.** Classes que só têm dado, com toda a regra em serviços. Martin Fowler chama isso de *anemic domain model*: tem a forma de orientação a objetos e a substância de programação procedural.

A correção é a mesma da [Lei de Demeter](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/law-of-demeter): **peça, não pergunte.** Em vez de `pedido.setStatus("fechado")`, `pedido.fechar()` — e a regra de quando um pedido pode ser fechado mora dentro do pedido, num lugar só.

## Onde ele cobra

- **Nem tudo merece uma classe com invariante.** Um DTO, uma resposta de API, um objeto de configuração — esses existem para carregar dado. Encapsular o que não tem regra só adiciona cerimônia; a página de [Demeter](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/law-of-demeter) faz a mesma distinção entre objeto e estrutura de dados.
- **Teste que precisa do estado interno.** É desconfortável por um motivo: quando testar exige espiar dentro, normalmente a unidade está fazendo mais de uma coisa. O encapsulamento não está atrapalhando o teste, está denunciando o desenho.
- **Serialização atravessa a fronteira.** Salvar no banco, mandar por HTTP e ler de volta exige transformar o objeto em dado cru — e é aí que o estado privado precisa sair e voltar. Some com o custo do mapeamento, não com o encapsulamento.

## Conclusão

Encapsulamento não é sobre `private`. É sobre decidir **o que o resto do sistema tem permissão de depender** — e essa decisão vale para classe, para módulo e para serviço, com o mesmo raciocínio em cada escala.

O teste que separa o real do aparente é simples: se dá para deixar o objeto num estado inválido de fora, ou se a regra que protege o dado mora em outro arquivo, não há encapsulamento — há campo privado. `get`/`set` para tudo é exatamente esse caso.

E o retorno é o que Parnas descreveu em 1972 e continua valendo: o que está escondido pode mudar sem negociação. **Tudo que vaza vira contrato**, e contrato só se rompe quebrando alguém.

## Princípios relacionados

- **[Acoplamento e Coesão](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/coupling-and-cohesion)** — encapsular é reduzir a superfície pela qual outros podem se acoplar a você. Agrupar dado e comportamento é a definição de coesão.
- **[Lei de Demeter](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/law-of-demeter)** — o *Tell, Don't Ask* é a correção do encapsulamento aparente: mande fazer em vez de puxar o estado e decidir no lugar do objeto.
- **[Herança](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/inheritance)** — `protected` é o furo mais legítimo do encapsulamento: abre o estado interno para todas as subclasses, presentes e futuras.
- **[Interface Segregation (ISP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/isp)** — os dois cuidam do tamanho da superfície exposta: o ISP pelo lado do contrato, o encapsulamento pelo lado da implementação.

## Referências

- David Parnas, *On the Criteria To Be Used in Decomposing Systems into Modules* (1972) — a origem da ocultação de informação: cada módulo esconde uma decisão que pode mudar.
- Martin Fowler, *AnemicDomainModel* (2003) — o modelo com dado sem comportamento, e por que ele desperdiça o paradigma.
