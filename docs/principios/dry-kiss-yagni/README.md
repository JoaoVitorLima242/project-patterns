# DRY, KISS, YAGNI

<!-- A frase de abertura, no seu jeito de dizer. Uma linha: o que os três têm
     em comum, ou por que estão na mesma página. -->

---

Três siglas que quase todo mundo repete e quase ninguém leu na fonte. Elas não vieram do mesmo lugar nem no mesmo ano: uma nasceu num livro sobre ofício, outra numa fábrica de aviões militares, a terceira dentro do Extreme Programming. E cada uma é mais estreita do que a fama sugere — é dessa diferença entre a formulação original e o uso popular que vem a maior parte dos problemas.

## DRY — Don't Repeat Yourself

Vem de Andy Hunt e Dave Thomas, no *The Pragmatic Programmer* (1999):

> Every piece of knowledge must have a single, unambiguous, authoritative representation within a system.
>
> — Toda porção de **conhecimento** deve ter uma representação única, sem ambiguidade e autoritativa dentro de um sistema.

Repare no que a frase **não** diz: ela não fala em código, em linha repetida nem em função duplicada. Fala em *conhecimento* — uma regra de negócio, um cálculo, um formato, uma decisão.

A diferença é exatamente o que separa o DRY bem aplicado do mal aplicado:

- Dois trechos idênticos que existem **pelo mesmo motivo** e vão mudar juntos são duplicação de verdade. Um dia alguém corrige um e esquece o outro — e o sistema passa a responder duas coisas diferentes para a mesma pergunta.
- Dois trechos idênticos **por coincidência**, que mudam por motivos diferentes, não são violação nenhuma. E juntá-los cria um acoplamento entre coisas que não tinham relação: na primeira vez que um dos dois lados precisar mudar, o que era uma função compartilhada ganha um parâmetro de configuração, depois um `if`, depois um segundo `if`.

Por isso a pergunta do DRY não é *"esse código está repetido?"*. É **"essa decisão está escrita em mais de um lugar?"**.

E vale o contrapeso: a duplicação tem um custo visível — dá para ver os dois arquivos —, enquanto a abstração errada tem um custo que só aparece meses depois, quando já não é mais fácil desfazer.

## KISS — Keep It Simple, Stupid

Não nasceu na computação. É atribuído a Kelly Johnson, engenheiro-chefe da Skunk Works da Lockheed — a divisão que projetou o U-2 e o SR-71 —, e o critério original era de manutenção em campo: o avião tinha que poder ser consertado por um **mecânico de habilidade média, sob pressão, com as ferramentas comuns** que ele teria à mão.

Isso muda o que "simples" significa. Não é *pouco código*, não é *código curto*, não é *sem abstração*. É **consertável por quem não escreveu**, com o que a pessoa tem em mãos, no momento em que está quebrado.

Duas consequências que a leitura popular perde:

- **Simples não é sinônimo de menor.** Um one-liner denso que ninguém entende às três da manhã falha no critério original, mesmo sendo a menor versão possível.
- **Simples não é sinônimo de sem estrutura.** Se a estrutura é o que permite o próximo consertar sem entender o sistema inteiro, ela é o KISS sendo aplicado, não violado.

O ponto de referência é sempre outra pessoa, no futuro, com pressa. Não o autor, hoje, com o problema fresco na cabeça.

## YAGNI — You Aren't Gonna Need It

Vem do Extreme Programming, com Kent Beck e Ron Jeffries, e a formulação é direta:

> Always implement things when you actually need them, never when you just foresee that you need them.
>
> — Implemente as coisas quando você realmente precisar delas, nunca quando você apenas prevê que vai precisar.

O argumento não é "não pense no futuro". É que o código escrito por previsão cobra em quatro frentes, como Martin Fowler organizou: o **custo de construir** o que não se usa; o **custo de atrasar** o que era para ser feito agora; o **custo de carregar** aquilo em toda leitura, refactor e build daí em diante; e o **custo de reparar**, quando a previsão erra e é preciso desfazer.

O detalhe que costuma passar batido: **previsão errada é pior que previsão ausente.** Se você não construiu nada, constrói na hora em que a necessidade aparecer, já sabendo o formato dela. Se construiu para o cenário errado, primeiro tem que remover — e normalmente com outras coisas já dependendo daquilo.

Vale a fronteira que o próprio XP marca: YAGNI vale para **funcionalidade especulativa**, não para qualidade interna. Não escrever teste, não nomear direito e não separar responsabilidade não são YAGNI — são só dívida.

## Na prática

Os três são simples e práticos, e é isso que os torna bons: não exigem cerimônia nenhuma para serem aplicados. **Eles trabalham juntos**, mirando o mesmo lugar — código mais simples e mais fácil de manter.

Mas cada um chega lá por um caminho próprio, e é aí que a distinção importa.

**O KISS cuida da forma.** O foco é deixar a lógica, a implementação e a infraestrutura o mais simples possível. Repare que não é só o código: a infraestrutura entra na conta, porque ela é tão capaz de complicar a manutenção quanto uma classe mal escrita. Quanto mais simples o conjunto, mais barato mexer nele depois.

**O DRY cuida da lógica repetida.** O propósito é evitar a mesma lógica em lugares diferentes. Quando ela se repete, o caminho é extrair um método e passar a chamá-lo — para a lógica existir em um lugar só, e a mudança acontecer em um lugar só.

**O YAGNI cuida do julgamento — e é o mais valioso dos três.** É também o que mostra quem é o desenvolvedor mais sênior, porque exige conhecimento para dizer se algo é realmente necessário agora ou não. Esse julgamento vale muito, e o motivo é o preço do erro: **uma arquitetura errada cobra duas vezes** — no custo de mudar e no custo de manter até que se mude.

Por isso os três não competem entre si. O KISS mantém a forma simples, o DRY mantém a lógica em um lugar, o YAGNI decide o que sequer deveria existir. Aplicados juntos, empurram o código para o mesmo destino.

## Trade-offs

| Ganha | Paga |
| --- | --- |
| Código mais simples e mais barato de manter, sem cerimônia para adotar | Nenhum dos três é regra automática: os três dependem de julgamento na hora |
| Uma decisão de negócio vive em um lugar só, e muda em um lugar só | Extrair cedo demais junta o que só era parecido, e o custo aparece meses depois |
| Não se paga por arquitetura que talvez nunca sirva | Decidir o que é necessário agora exige senioridade — e errar cobra em mudança e em manutenção |
| A infraestrutura entra na conta da simplicidade, não só o código | Simplicidade mal entendida vira ausência de estrutura, que também custa manutenção |

## Princípios relacionados

- **[Acoplamento e Coesão](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/coupling-and-cohesion)** — o custo do DRY mal aplicado tem nome: juntar dois trechos que só eram parecidos cria acoplamento entre coisas que mudavam por motivos diferentes.
- **[Open/Closed (OCP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/ocp)** — o OCP pede ponto de extensão; o YAGNI pergunta se aquela extensão vai existir. É a tensão mais concreta entre o SOLID e os três daqui.
- **[Single Responsibility (SRP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/srp)** — "um motivo para mudar" é o mesmo critério que decide se dois trechos iguais são duplicação de verdade.

## Referências

- Andy Hunt e Dave Thomas, *The Pragmatic Programmer* (1999) — origem do DRY, na formulação sobre conhecimento.
- Ron Jeffries, *You're NOT gonna need it!* (1998) — o YAGNI dentro do Extreme Programming.
- Martin Fowler, *Yagni* (2015) — os quatro custos do código especulativo.
