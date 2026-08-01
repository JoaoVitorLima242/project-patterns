# Design Patterns

Documentação de estudos sobre design patterns, princípios de projeto e arquitetura de software — escrita em português, com exemplos em TypeScript, Python e Java.

A ideia não é ser mais um catálogo com a definição formal de cada pattern (isso o livro do GoF já faz, e melhor). É registrar o que costuma faltar: **qual dor cada pattern resolve, quando ele é a escolha errada, e o que você paga por usá-lo.** Toda página tem uma seção "Quando NÃO usar", e ela costuma ser a mais útil.

É um trabalho em andamento, preenchido aos poucos. O mapa abaixo mostra tudo que está previsto e o que já existe.

## Como navegar

| Seção | O que é |
| --- | --- |
| **[Princípios](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios)** | Os critérios que dizem se um pattern faz sentido ali. Comece por aqui. |
| **[Design Patterns](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/patterns)** | Os 23 patterns do *Gang of Four*, em três famílias. |
| **[Arquitetura](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/arquitetura)** | Um nível acima: fronteiras do sistema e direção de dependências. |

---

## Mapa

**✅ Escrito**  ·  **🚧 Em progresso**  ·  **🔜 Em breve**

### Princípios

| Tópico | Status | Ideia central |
| --- | :---: | --- |
| [**Single Responsibility (SRP)**](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/srp) | ✅ | Uma classe deve ter uma única responsabilidade |
| [Open/Closed (OCP)](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios/ocp) | 🚧 | Aberto para extensão, fechado para modificação |
| Liskov Substitution (LSP) | 🚧 | Subtipo tem que poder substituir o tipo base sem surpresa |
| Interface Segregation (ISP) | 🚧 | Interface pequena e específica em vez de uma que serve a todos |
| Dependency Inversion (DIP) | 🚧 | Depender de abstrações, não de implementações |
| Composição vs. Herança | 🔜 | A decisão de estrutura mais consequente do dia a dia |
| Acoplamento e Coesão | 🔜 | O vocabulário para dizer *por que* um design está ruim |
| DRY, KISS, YAGNI | 🔜 | Os três que mais se aplicam errado |
| Lei de Demeter | 🔜 | Quanto um objeto deve saber sobre os vizinhos dos vizinhos |

### Design Patterns — Criacionais

> Como criar objetos sem amarrar o código à classe concreta.

| Pattern | Status | Ideia central |
| --- | :---: | --- |
| Factory Method | 🔜 | Subclasses decidem qual objeto instanciar |
| Abstract Factory | 🔜 | Cria famílias de objetos relacionados sem citar classes concretas |
| Builder | 🔜 | Monta objetos complexos passo a passo |
| Prototype | 🔜 | Cria novos objetos clonando um existente |
| Singleton | 🔜 | Uma única instância global — e por que costuma ser má ideia |

### Design Patterns — Estruturais

> Como compor objetos em estruturas maiores mantendo tudo flexível.

| Pattern | Status | Ideia central |
| --- | :---: | --- |
| Adapter | 🔜 | Faz interfaces incompatíveis conversarem |
| Bridge | 🔜 | Separa abstração de implementação para variarem sozinhas |
| Composite | 🔜 | Trata objeto individual e composição de objetos do mesmo jeito |
| Decorator | 🔜 | Adiciona comportamento envolvendo o objeto, sem herança |
| Facade | 🔜 | Uma porta de entrada simples para um subsistema complicado |
| Flyweight | 🔜 | Compartilha estado comum entre muitos objetos parecidos |
| Proxy | 🔜 | Um substituto que controla o acesso ao objeto real |

### Design Patterns — Comportamentais

> Como distribuir responsabilidade e coordenar a conversa entre objetos.

| Pattern | Status | Ideia central |
| --- | :---: | --- |
| Chain of Responsibility | 🔜 | Passa a requisição por uma corrente até alguém tratar |
| Command | 🔜 | Transforma uma ação em objeto — dá undo, fila e log |
| Interpreter | 🔜 | Representa a gramática de uma linguagem como objetos |
| Iterator | 🔜 | Percorre uma coleção sem expor como ela é feita por dentro |
| Mediator | 🔜 | Centraliza a comunicação para os objetos não se conhecerem |
| Memento | 🔜 | Salva e restaura estado sem quebrar o encapsulamento |
| Observer | 🔜 | Notifica os interessados quando algo muda |
| State | 🔜 | O objeto muda de comportamento junto com o estado interno |
| Strategy | 🔜 | Algoritmos intercambiáveis, trocados em tempo de execução |
| Template Method | 🔜 | O esqueleto do algoritmo na base, os passos nas subclasses |
| Visitor | 🔜 | Adiciona operações a uma estrutura sem alterar suas classes |

### Arquitetura

| Tópico | Status | Ideia central |
| --- | :---: | --- |
| Arquitetura em camadas | 🔜 | O ponto de partida — e por onde ele falha |
| Clean Architecture | 🔜 | Dependências apontam para dentro, rumo ao domínio |
| Hexagonal (Ports & Adapters) | 🔜 | O domínio no centro, infraestrutura plugada na borda |
| DDD tático | 🔜 | Entity, Value Object, Aggregate, Repository |
| CQRS | 🔜 | Separar o caminho de leitura do de escrita |
| Event-Driven | 🔜 | Componentes conversam por eventos, não por chamadas |
| Container / Presenter | 🔜 | Separar busca de dados de apresentação na UI |
| Custom Hooks | 🔜 | Extrair lógica com estado para fora do componente |
| State Machine na UI | 🔜 | Estados explícitos em vez de um punhado de booleanos |

---

## Contribuindo

Sugestões, correções e novos tópicos são bem-vindos — o passo a passo está em **[CONTRIBUTING.md](https://github.com/JoaoVitorLima242/project-patterns/blob/main/CONTRIBUTING.md)**.

## Rodando os exemplos (opcional)

Você não precisa de nada disso para ler a documentação: as páginas são Markdown e trazem o código inline, legível direto aqui no GitHub.

Mas cada pattern também tem os exemplos como arquivos executáveis ao lado da página, caso você queira mexer neles. Não há nada a instalar — sem `npm install`, sem dependências, sem build:

```bash
node    docs/patterns/<família>/<pattern>/typescript/main.ts
python3 docs/patterns/<família>/<pattern>/python/main.py
java    docs/patterns/<família>/<pattern>/java/Main.java
```

Requisitos: **Node 22.18+ ou 23.6+** (executa `.ts` direto, apagando os tipos), **Python 3.10+**, **JDK 17+**. O Node imprime um aviso de "Type Stripping is an experimental feature" — é esperado e inofensivo.
