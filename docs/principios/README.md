# Princípios

Antes dos patterns vêm os princípios. Um pattern é uma solução *específica* para um problema *específico*; um princípio é o critério que te diz se aquela solução faz sentido ali.

A diferença importa na prática: quem decora patterns aplica o Strategy porque reconhece um `if/else` grande. Quem entende os princípios aplica o Strategy porque percebe que a classe está mudando por vários motivos diferentes — e sabe também quando *não* aplicar, porque as variações nunca vão crescer.

Quase todo pattern do catálogo GoF é a aplicação concreta de um destes princípios. O Strategy existe para atender OCP e SRP. O Adapter, para atender DIP. Ler os princípios primeiro faz os 23 patterns pararem de parecer 23 truques desconexos.

## O que esta seção cobre

- **Encapsulamento, Herança e Polimorfismo** — três dos pilares da orientação a objetos, um por página: o que cada um é, como funciona por dentro e o que cobra.
- **SOLID** — os cinco princípios de projeto orientado a objetos, um por página.
- **Composição vs. Herança** — a decisão de estrutura mais consequente do dia a dia.
- **Acoplamento e Coesão** — o vocabulário para dizer *por que* um design está ruim.
- **DRY, KISS, YAGNI** — os três que mais se aplicam errado, cada um com sua armadilha.
- **Lei de Demeter** — quanto um objeto deve saber sobre os vizinhos dos vizinhos.

---

**Status de cada tópico → [mapa no README principal](https://github.com/JoaoVitorLima242/project-patterns/blob/main/README.md).** O mapa é a única fonte de verdade do que já foi escrito; esta página não repete essa informação para não sair de sincronia.
