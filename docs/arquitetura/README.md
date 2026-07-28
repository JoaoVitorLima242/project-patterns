# Arquitetura

Princípios e patterns operam no nível de classes e objetos. Arquitetura opera um nível acima: como o sistema se divide em partes, quem pode depender de quem, e onde ficam as fronteiras que separam regra de negócio de detalhe de infraestrutura.

A pergunta central muda. Nos patterns é "como organizo essas classes?"; aqui é "o que acontece com esse sistema quando trocarmos o banco, quando o time dobrar, quando a regra de negócio mudar pela vigésima vez?".

Vale notar que a fronteira entre as seções é porosa: Repository é um pattern tático de DDD e ao mesmo tempo uma decisão arquitetural; Ports & Adapters é o Adapter do GoF aplicado ao contorno do sistema. Os tópicos aqui assumem os das outras duas seções.

## O que esta seção cobre

- **Arquitetura em camadas** — o ponto de partida, e por onde ela falha.
- **Clean Architecture** e **Hexagonal (Ports & Adapters)** — as duas respostas mais conhecidas à mesma pergunta sobre direção de dependências.
- **DDD tático** — Entity, Value Object, Aggregate, Repository, Domain Service.
- **CQRS** e **Event-Driven** — separar leitura de escrita, comunicar por eventos, e o preço de cada um.
- **Patterns de frontend** — Container/Presenter, Custom Hooks, State Machine na UI.

Esta é a seção mais aberta do repositório: não existe um catálogo fechado como os 23 do GoF, e a lista cresce conforme os estudos avançarem.

---

**Status de cada tópico → [mapa no README principal](https://github.com/JoaoVitorLima242/project-patterns/blob/main/README.md).** O mapa é a única fonte de verdade do que já foi escrito; esta página não repete essa informação para não sair de sincronia.
