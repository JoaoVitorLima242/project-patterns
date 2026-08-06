# Herança

O mecanismo que faz a orientação a objetos funcionar por dentro — e que cobra caro quando é usado para economizar digitação.

---

Herança é um dos fundamentos da orientação a objetos — está na lista canônica dos pilares do paradigma, ao lado de encapsulamento, abstração e polimorfismo, e é dela que vem boa parte do que a linguagem faz por baixo quando você chama um método.

Por isso vale entender o mecanismo antes de discutir estilo. Existe uma página nesta seção sobre [Composição vs. Herança](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/composition-vs-inheritance), e ela responde a pergunta da escolha: capacidade se resolve compondo, identidade se resolve com contrato. Esta aqui é sobre **o que a herança é, o que ela faz por dentro e o que acontece depois que ela foi escolhida.**

## De onde vem

Herança não nasceu como técnica de reúso de código. Nasceu como forma de **modelar**.

O conceito aparece no **Simula 67**, de Ole-Johan Dahl e Kristen Nygaard, a primeira linguagem orientada a objetos — criada para escrever simulações. Se o programa precisa representar veículos, e caminhão e carro compartilham grande parte do que um veículo é, a linguagem passa a ter como dizer isso: subclasse. A ideia era descrever o mundo em categorias, e categoria é uma coisa que naturalmente tem subcategoria.

O **Smalltalk**, nos anos 70, generalizou o modelo — tudo é objeto, toda classe herda de outra até `Object` —, e é dele que vem o formato que as linguagens de hoje copiaram. Vale registrar que Alan Kay, que cunhou o termo "orientação a objetos", dizia que o essencial do paradigma era a **troca de mensagens** entre objetos, não a hierarquia de classes: a herança entrou como consequência do modelo, não como o ponto dele.

Isso explica a tensão que a herança carrega até hoje: ela foi feita para **expressar categoria**, e desde cedo passou a ser usada para **economizar digitação**. São coisas diferentes, e a segunda é a que dá problema.

## O que ela faz por baixo

Mecanicamente, um `extends` entrega três coisas ao mesmo tempo:

1. **Estrutura** — a subclasse ganha os campos e métodos da base sem redeclarar.
2. **Subtipo** — onde se espera a base, a subclasse é aceita. É o que torna o polimorfismo possível em linguagens de tipagem nominal.
3. **Despacho dinâmico** — a chamada é resolvida em **tempo de execução**, pelo tipo real do objeto, não pelo tipo declarado da variável.

O terceiro item é o coração do assunto, e o que faz a herança ser mais do que um atalho de escrita. Quando um código chama `veiculo.mover()`, quem decide qual implementação roda não é o compilador olhando a variável: é o objeto, no momento da chamada. Em linguagens como Java isso é implementado por uma tabela de métodos que cada classe carrega; em **JavaScript e TypeScript**, é a *prototype chain* — a busca sobe de protótipo em protótipo até encontrar o método, e `class` é açúcar sintático sobre esse mecanismo.

É por causa do despacho dinâmico que o `if` sobre tipo desaparece e patterns como **Template Method** e **Strategy** existem. E é por causa dele, também, que a base pode acabar chamando código da subclasse sem saber — o que é a origem do problema mais conhecido da herança, logo adiante.

## Quando herdar é a escolha certa

Nem todo `extends` é dívida. Os casos em que ele se paga têm um padrão comum: a relação de tipo é real, e o que a base entrega é contrato, não implementação.

- **A relação "é um" é verdadeira e estável.** Não "parece um" nem "tem quase tudo de um": se em todo lugar que espera a base a subclasse serve sem ressalva, a identidade existe de fato. É a régua do [LSP](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/lsp).
- **A base é um contrato, não código.** Implementar uma `interface` dá identidade sem trazer tripa junto — nada de método protegido, nada de dependência de como o pai funciona por dentro.
- **A variação está em passos, não no todo.** Quando o algoritmo é o mesmo e só alguns pontos mudam, a base define o esqueleto e as subclasses preenchem os buracos. Esse é o **Template Method**, e é o caso em que a herança foi projetada para ser usada — a base decide o que pode ser sobrescrito, e documenta.
- **A hierarquia é fechada e conhecida.** Um conjunto finito de tipos do domínio que você controla — `PagamentoPix`, `PagamentoBoleto`, `PagamentoCartao` — não sofre com os problemas de extensão aberta, porque não há terceiro herdando de fora.
- **O framework exige.** Às vezes herdar é a API: a decisão foi tomada por quem escreveu a biblioteca, e brigar com ela custa mais do que aceitar.

## Onde a hierarquia cobra

Quase todo problema clássico de herança vem da mesma raiz, e ela é consequência direta do despacho dinâmico: a subclasse não depende só do que a base promete, depende de **como a base faz** — e isso não aparece em assinatura nenhuma.

### Fragile base class

O problema mais conhecido, e o que melhor explica todos os outros. Uma coleção que conta quantos itens já passaram por ela:

```ts
class Colecao<T> {
  protected itens: T[] = [];

  adicionar(item: T): void {
    this.itens.push(item);
  }

  // Detalhe interno: adicionarTodos() usa adicionar() por baixo.
  adicionarTodos(novos: T[]): void {
    for (const n of novos) this.adicionar(n);
  }
}

class ColecaoContada<T> extends Colecao<T> {
  contador = 0;

  adicionar(item: T): void {
    this.contador++;
    super.adicionar(item);
  }

  adicionarTodos(novos: T[]): void {
    this.contador += novos.length;
    super.adicionarTodos(novos); // ...que vai chamar adicionar() de novo.
  }
}
```

`adicionarTodos([a, b, c])` conta **seis**. E o mais importante: **nenhuma das duas classes está errada sozinha.** O bug mora na relação — a subclasse assumiu, sem poder saber, que `adicionarTodos` não reaproveitava `adicionar`.

A consequência é que o *pai não pode mais mudar de ideia*. Trocar a implementação interna de `adicionarTodos`, algo que deveria ser invisível de fora, quebra subclasses que ninguém tocou. A herança transformou um detalhe de implementação em parte do contrato, sem avisar ninguém.

### Profundidade

Cada nível a mais é contexto que alguém vai precisar carregar para entender uma linha. Numa hierarquia de quatro níveis, responder "o que este método faz" vira uma subida na árvore, procurando quem sobrescreveu o quê — e o comportamento final não está escrito em lugar nenhum: ele é o resultado da combinação.

O sintoma é fácil de reconhecer: quando ler o código exige o gráfico da hierarquia aberto do lado, a profundidade já passou do ponto.

### Herança múltipla

Herdar de dois lugares levanta a pergunta de qual implementação vale quando as duas definem a mesma coisa — o problema do diamante. As linguagens resolvem de formas diferentes (Java proíbe e oferece interface com método default; Python define uma ordem de resolução; outras têm mixin ou trait), mas o custo é sempre o mesmo: para saber o que roda, é preciso conhecer a regra da linguagem, não só o código à vista.

### Herança de estado

Herdar comportamento já cobra. Herdar **estado** cobra mais: campos `protected` viram API compartilhada com todas as subclasses, presentes e futuras, e qualquer uma delas pode deixar o objeto num estado que a base não previu. É o reúso caixa-branca no pior formato — invariante da base mantida por código que a base não escreveu.

## Projete para herança, ou proíba

A recomendação do *Effective Java* resume o que fazer com tudo isso: **ou a classe foi feita para ser herdada, e isso está documentado, ou ela deveria impedir a herança.**

Projetar para herança significa decidir e escrever quais métodos podem ser sobrescritos, o que a base garante e o que ela chama por dentro — porque, como o caso do `adicionarTodos` mostra, quem herda precisa dessa informação e ela não está na assinatura.

O caminho do meio — uma classe comum, herdável por acidente, com métodos que se chamam entre si — é onde o problema nasce. Por isso o padrão sensato é o inverso do costume: **fechar por padrão** (`final`, `sealed`, ou só não expor a classe) e abrir quando houver um motivo.

## Conclusão

Herança é fundamento, e é por isso que vale entendê-la mesmo em código que quase não a usa. O despacho dinâmico — a chamada resolvida pelo objeto, em tempo de execução — é o mecanismo que sustenta o polimorfismo, e é o que faz metade do catálogo de patterns funcionar. Quem entende isso não está aprendendo uma sintaxe, está aprendendo como a orientação a objetos funciona por dentro.

O detalhe que amarra a página é que **o mesmo mecanismo que dá o poder é o que produz a fragilidade**. Porque a chamada é resolvida no objeto, a base pode acabar executando código da subclasse sem saber — daí a fragile base class, daí o custo de ler uma hierarquia profunda, daí o cuidado com estado `protected`. Não são defeitos avulsos de uma ferramenta mal feita: são o outro lado da mesma propriedade.

Daí a diferença entre os dois usos que existem desde o Simula: herdar para **expressar categoria** é usar a ferramenta para o que ela foi feita; herdar para **economizar digitação** é pegar o acoplamento sem precisar dele. O primeiro caso se sustenta quando a relação "é um" é verdadeira, a base foi projetada para ser estendida e isso está documentado.

Na dúvida, o padrão sensato é o inverso do costume: **fechado por padrão, aberto por decisão** — e a decisão de compor, quando existe, quase sempre é a mais barata de desfazer.

## Princípios relacionados

- **[Composição vs. Herança](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/composition-vs-inheritance)** — lá está a decisão entre as duas; aqui, o que fazer quando a herança venceu.
- **[Liskov Substitution (LSP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/lsp)** — é a régua que diz se a relação "é um" é verdadeira. Herança que viola o LSP não é herança errada de estilo, é modelo errado.
- **[Acoplamento e Coesão](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/coupling-and-cohesion)** — a fragile base class é acoplamento de conteúdo com bênção da linguagem: a subclasse depende de como o pai funciona por dentro.
- **[Interface Segregation (ISP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/isp)** — herdar traz a interface inteira da base, inclusive o que a subclasse não queria expor.

## Referências

- Ole-Johan Dahl e Kristen Nygaard, *Simula 67* — a origem do conceito de classe e subclasse, criado para modelar simulações.
- Joshua Bloch, *Effective Java* (3ª ed.) — itens 18 e 19: "prefira composição a herança" e "projete e documente para herança, ou proíba".
- Erich Gamma et al., *Design Patterns* (1994) — a distinção entre reúso caixa-branca e caixa-preta, e o Template Method como uso legítimo da herança.
