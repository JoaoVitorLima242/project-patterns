# Polimorfismo

Uma mesma chamada, comportamentos diferentes — decididos pelo tipo do dado, não por um `if` no meio do caminho.

---

Polimorfismo é o pilar da orientação a objetos que faz os outros valerem a pena. Encapsulamento esconde estado, [herança](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/inheritance) organiza categoria, abstração define contrato — mas é o polimorfismo que transforma isso em código que não precisa ser reescrito a cada caso novo.

E ele é maior do que a versão que costuma ser ensinada. "Classe base e subclasses sobrescrevendo métodos" é **um** tipo de polimorfismo, não o conceito inteiro. Vale conhecer os outros, porque os mais úteis no dia a dia de TypeScript não passam por herança nenhuma.

## Os quatro tipos

A classificação que se usa até hoje é de Luca Cardelli e Peter Wegner (1985), e separa duas famílias:

**Universal** — um código só, funcionando para muitos tipos:

- **Paramétrico.** O código é escrito para um tipo qualquer, e o tipo entra como parâmetro. É `Array<T>`, é `Promise<T>`, é qualquer função genérica. O corpo não muda: a mesma implementação serve para todos os tipos, porque não depende de nenhum deles.
- **De inclusão (subtipo).** O clássico da POO: onde se espera um `Pagamento`, cabe qualquer coisa que seja um `Pagamento`, e a chamada resolve no tipo real do objeto em tempo de execução. É o **despacho dinâmico**.

**Ad-hoc** — implementações diferentes escolhidas por tipo:

- **Sobrecarga.** O mesmo nome com assinaturas diferentes, resolvido em tempo de compilação. Não há decisão em runtime: o compilador escolhe e pronto.
- **Coerção.** A conversão implícita que a linguagem faz para encaixar um tipo onde outro era esperado. É o mais discreto — e, em JavaScript, o mais famoso pelos motivos errados.

A diferença que mais importa na prática é entre **paramétrico** e **de inclusão**. O paramétrico serve para escrever estrutura que não olha o conteúdo (uma lista não precisa saber o que guarda). O de inclusão serve quando o comportamento precisa mudar conforme o tipo — e é dele que o resto desta página trata.

## O que ele resolve

O ganho concreto é o desaparecimento do `switch` sobre tipo:

```ts
// Sem polimorfismo: quem chama precisa conhecer todos os casos,
// e ganha mais um "case" a cada forma nova.
function area(f: FormaData): number {
  switch (f.tipo) {
    case "circulo":   return Math.PI * f.raio ** 2;
    case "retangulo": return f.largura * f.altura;
  }
}

// Com polimorfismo: cada tipo sabe calcular a própria área,
// e quem chama não sabe quantos tipos existem.
interface Forma {
  area(): number;
}

class Circulo implements Forma {
  private raio: number;

  constructor(raio: number) {
    this.raio = raio;
  }

  area(): number {
    return Math.PI * this.raio ** 2;
  }
}
```

O que mudou não foi o número de linhas — foi **onde a mudança acontece**. Adicionar um triângulo, no primeiro caso, significa editar uma função que já funcionava e que outros lugares usam. No segundo, significa escrever uma classe nova e não tocar em nada.

Esse é exatamente o [Open/Closed](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/ocp) em ação, e é por isso que polimorfismo é o mecanismo por trás de boa parte do catálogo do GoF: Strategy, State, Command e Visitor são, no fundo, formas de organizar quem responde a uma chamada.

## Polimorfismo sem herança

Aqui está a parte que a formação clássica costuma deixar de fora — e a que mais aparece em TypeScript.

**Interface basta.** Nenhum dos exemplos acima precisou de classe base. Implementar um contrato dá o polimorfismo sem trazer estado, método protegido nem hierarquia junto.

**Tipagem estrutural.** Java pergunta se o tipo *declarou* que implementa a interface. TypeScript pergunta se o objeto *tem o formato* — se ele tem `area(): number`, ele serve, tenha ou não `implements` escrito. É o mesmo princípio do *duck typing* de Python, com verificação em tempo de compilação: o polimorfismo passa a valer também para objetos literais e funções, não só para classes.

**União discriminada.** Um `type` com uma tag e um `switch` exaustivo dá a mesma flexibilidade por outro caminho — e o compilador reclama quando falta um caso:

```ts
type FormaData =
  | { tipo: "circulo"; raio: number }
  | { tipo: "retangulo"; largura: number; altura: number };
```

A escolha entre os dois tem uma regra conhecida: **classe é fácil de estender com tipos novos; união é fácil de estender com operações novas.** Se o conjunto de formas cresce, a interface ganha. Se o que cresce é a quantidade de coisas que se faz com as formas, a união ganha — porque adicionar uma operação é escrever uma função, não editar todas as classes.

**Função de primeira classe.** Passar uma função é polimorfismo: `array.sort(comparador)` funciona com qualquer critério porque o comportamento variável entrou como argumento. Em muitos casos é o Strategy inteiro, sem nenhuma classe.

## Onde ele cobra

- **O comportamento fica espalhado.** A vantagem — quem chama não sabe quantas implementações existem — é a mesma coisa que dificulta responder "o que roda aqui?". Ler o código deixa de bastar; é preciso saber quem foi injetado.
- **Polimorfismo com uma implementação só** é indireção pura. Uma interface com um único implementador que nunca teve concorrente adiciona um salto de leitura e não entrega flexibilidade nenhuma — é o [YAGNI](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/dry-kiss-yagni) cobrando.
- **Hierarquia como desculpa.** Criar classe base só para ter polimorfismo traz junto todo o custo da [herança](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/inheritance), quando uma interface — ou uma função — resolveria.
- **O `switch` disfarçado.** Se toda implementação precisa perguntar de que tipo ela é, ou se o chamador faz `instanceof` para decidir o que fazer, o polimorfismo está escrito mas não está sendo usado.

## Conclusão

Polimorfismo é o que faz o código parar de crescer no mesmo lugar. Sem ele, cada caso novo é uma edição num arquivo que já funcionava; com ele, é um arquivo novo que ninguém precisa revisar duas vezes.

A confusão comum é achar que isso exige hierarquia de classes. Não exige: a herança é **uma** forma de conseguir polimorfismo de inclusão, e em TypeScript quase nunca é a mais barata — interface, tipagem estrutural, união discriminada e função de primeira classe entregam o mesmo sem carregar base class alguma.

O que continua valendo é o critério: polimorfismo se paga quando existe **variação real** — mais de uma implementação, agora ou de forma previsível. Quando não existe, o que sobra é indireção com nome bonito.

## Princípios relacionados

- **[Herança](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/inheritance)** — o despacho dinâmico que sustenta o polimorfismo de inclusão é o mesmo mecanismo que torna a base frágil. Aqui está o benefício; lá, a conta.
- **[Open/Closed (OCP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/ocp)** — o OCP é o objetivo, o polimorfismo é o meio: é ele que permite estender sem modificar.
- **[Composição vs. Herança](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/composition-vs-inheritance)** — "identidade se resolve com contrato" é a mesma frase vista daqui: o contrato é o que dá polimorfismo sem hierarquia.
- **[Interface Segregation (ISP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/isp)** — quanto menor a interface, mais fácil ter outra implementação dela. Interface grande mata o polimorfismo na prática.

## Referências

- Luca Cardelli e Peter Wegner, *On Understanding Types, Data Abstraction, and Polymorphism* (1985) — a classificação em universal e ad-hoc usada aqui.
- Erich Gamma et al., *Design Patterns* (1994) — Strategy, State e Visitor como organizações diferentes de quem responde à chamada.
