# Acoplamento e Coesão

Coesão é o que faz os membros de uma classe estarem juntos; acoplamento é o quanto ela precisa saber das outras para funcionar.

---

## A prática vem antes do fundamento

Boa parte do que a gente faz todo dia é a aplicação de um princípio que não sabemos nomear. Quem estrutura sistema com classe de responsabilidade única e dependência externa reduzida ao mínimo **já aplica** coesão e acoplamento — mesmo sem nunca ter usado as palavras.

Fazer sem saber o termo não é problema: o código sai certo do mesmo jeito. Mas ficar só na prática cobra depois.

Sem o conceito, o que se tem é **hábito** — você repete o que funcionou porque funcionou. Com o fundamento, você sabe *por que* funcionou, e é isso que permite decidir num caso que não se parece com nenhum dos anteriores, reconhecer quando aquilo não se aplica, e discutir a decisão com alguém que discorda dela.

Ou seja: não tem técnica nova aqui, e ainda assim vale entender. O que muda não é o que você escreve, é o que você **sabe** sobre o que escreve.

Uma das consequências aparece no review, e é a mais visível: "essa classe está com coesão baixa" e "isso é acoplamento de controle" dizem *o que* está errado e *onde*; "não gostei" não diz. É a diferença entre um apontamento que o autor consegue agir em cima e um que ele só consegue discordar. Mas o review é só onde o entendimento fica aparente — o ganho é do programador, antes de ser da equipe.

## Coesão: o que está junto pertence junto?

Coesão mede **o que faz os membros de uma classe estarem no mesmo arquivo**. Não é sobre tamanho — é sobre motivo.

Do pior para o melhor:

| Nível | O que junta os membros | Como aparece |
| --- | --- | --- |
| **Coincidental** | Nada. Estão juntos por acaso | `Utils`, `Helpers`, `Commons` |
| **Lógica** | São "do mesmo tipo" de coisa, e uma flag escolhe qual roda | `processar(tipo)` com um switch grande |
| **Temporal** | Rodam no mesmo momento | `inicializar()` que faz oito coisas sem relação entre si |
| **Funcional** | Tudo contribui para uma única tarefa | O alvo |

A **coesão lógica** é a que mais engana. A classe tem nome de responsabilidade única, o cabeçalho promete uma coisa só, e por dentro é um `switch` decidindo entre três comportamentos que não se falam. Pelo nome, coesa. Pelo conteúdo, três classes.

## Acoplamento: quanto uma classe depende das outras

Acoplamento mede **o quanto uma classe precisa saber sobre outra** para funcionar.

Do pior para o melhor:

| Nível | O que é | Como aparece |
| --- | --- | --- |
| **Conteúdo** | Uma classe mexe nas tripas da outra | Acessa campo privado, depende de como a outra funciona por dentro |
| **Comum** | Duas classes compartilham estado global mutável | Singleton com estado, cache compartilhado |
| **Controle** | Uma passa uma flag que decide o caminho da outra | `salvar(pedido, true)` — o `true` liga um `if` lá dentro |
| **Stamp** | Passa o objeto inteiro quando só precisa de um campo | Recebe `Pedido` para ler só o `cep` |
| **Dados** | Passa só o que a outra precisa | `calcularFrete(cep, peso)` |

### Zero acoplamento não é o alvo

Repare no que a escala mede: ela não vai de "acoplado" a "não acoplado". Vai de **pior tipo** a **melhor tipo** — e o melhor nível, acoplamento de dados, ainda é acoplamento.

Projeto grande sempre vai ter dependência entre classes, e isso não é defeito: **o sistema existe para se comunicar consigo mesmo.** Uma classe que não depende de ninguém e da qual ninguém depende não está desacoplada, está sobrando.

Então a pergunta útil nunca foi "como eliminar a dependência". É **de que tipo ela vai ser**.

## O movimento: a dependência vira contrato

O que se faz na prática para diminuir acoplamento é deixar a dependência da classe como um **contrato**. A classe passa a depender do que precisa que exista, não de quem vai fazer. Quando surge a necessidade de trocar a implementação, cria-se uma nova e injeta.

```ts
// Não é do provedor de e-mail que o caso de uso depende — é do contrato.
interface EnviadorDeEmail {
  enviar(para: string, assunto: string): void;
}

class CriarConta {
  private email: EnviadorDeEmail;

  constructor(email: EnviadorDeEmail) {
    this.email = email;
  }
  // Trocar o provedor é passar outro objeto aqui. A classe não muda.
}
```

Vale dizer com todas as letras que isso **não é uma técnica deste tópico**. É o [DIP](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/dip) chegando pelo outro lado: lá o contrato aparece como regra de direção da dependência; aqui ele aparece como a resposta à pergunta "como se reduz acoplamento". Mesmo movimento, duas motivações.

## A régua é o teste

O problema prático de "mantenha alta coesão" é que ninguém diz quanto é alta. Não existe número.

O que dá para medir é **caminho**. Cada alternativa booleana dentro de um método é mais um expoente no número de caminhos possíveis daquela classe — dois `if` independentes já são quatro fluxos, três são oito.

E o instrumento que expõe isso é o **teste unitário**. Escrever o teste do método obriga a enumerar as possibilidades: não dá para cobrir o que não se enxergou. É por isso que o teste mede a coesão sem que ninguém tenha combinado isso — a classe incoerente aparece como uma bateria de casos que não se parecem.

Daí sai o critério, sem parâmetro exato: **quando está muito difícil de testar e de pegar todos os cenários, provavelmente a classe podia ser quebrada em outras mais simples.**

> Não é "a classe está grande demais". É "eu não consigo enumerar o que ela faz".

E os dois lados se encontram aqui: o acoplamento de controle — a flag que decide o caminho lá dentro — é exatamente o que multiplica os caminhos a testar. O nível ruim de acoplamento *produz* a classe difícil de testar.

## O que a teoria cobra

Acoplamento e coesão **não são as únicas formas** de dizer se um código está ruim, e a página perde o valor se for lida assim. Como parâmetro de organização em nível de classe e de método, valem. Como critério único de qualidade, não.

E há um custo que raramente é dito: **manter um projeto nessa linha exige processo.** O time precisa estar muito alinhado sobre como as coisas devem ser feitas, e é preciso supervisão técnica ativa para o código continuar no que foi planejado. Não é um princípio que se adota escrevendo no documento de padrões — sem esse acompanhamento, o código volta ao normal em poucos sprints.

### A falta de régua é um fato, não um problema a resolver

Existem responsabilidades grandes que acabam precisando ser quebradas, e aí fica genuinamente complexo definir o quão coeso é coeso o bastante.

Isso não tem solução, e não precisa ter. É **teoria**: a tese de que código mais desacoplado organiza melhor é real e se sustenta, mas o que define coesão e acoplamento diante de um número infinito de situações vai dar **respostas distintas conforme a situação do projeto**. Duas equipes competentes podem discordar sobre a mesma classe, e as duas terem razão dentro do próprio contexto.

Aceitar isso é o que impede a régua de virar burocracia.

## Trade-offs

| Ganha | Paga |
| --- | --- |
| Entender o fundamento do que já se faz por hábito — e conseguir apontar o problema em vez de opinar sobre ele | Processo: time alinhado e supervisão técnica constante |
| Classe mais fácil de testar, porque tem menos caminhos | Mais classes, mais arquivos, mais indireção |
| Trocar implementação sem tocar em quem usa | Contrato a mais para manter em cada troca prevista |
| Critério para decidir onde quebrar uma responsabilidade grande | Nenhum limite objetivo: a decisão continua sendo julgamento |

## Princípios relacionados

- **[Single Responsibility (SRP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/srp)** — o SRP é coesão vista de dentro de uma classe. "Um motivo para mudar" é a mesma pergunta de "o que faz esses métodos estarem juntos", feita pelo lado de quem vai alterar o código.
- **[Dependency Inversion (DIP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/dip)** — é a técnica que baixa o acoplamento: a dependência vira contrato. Aqui está o *porquê*; lá, a regra.
- **[Composição vs. Herança](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/composition-vs-inheritance)** — herança é o acoplamento mais forte que existe entre duas classes: a subclasse enxerga o que é `protected` e passa a depender de como o pai funciona por dentro. É acoplamento de conteúdo com bênção da linguagem.
- **[Interface Segregation (ISP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/isp)** — interface grande acopla o cliente a métodos que ele não usa. Segregar é baixar acoplamento pelo lado do contrato.

## Referências

- Larry Constantine e Edward Yourdon, *Structured Design* (1979) — a origem das duas escalas.
- Thomas McCabe, *A Complexity Measure* (1976) — complexidade ciclomática: o número de caminhos independentes de um método é o número mínimo de testes para cobri-lo.
