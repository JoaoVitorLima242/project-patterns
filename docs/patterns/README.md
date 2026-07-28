# Design Patterns

Soluções recorrentes para problemas recorrentes de projeto orientado a objetos. Um pattern não é código pronto para copiar — é a descrição de um arranjo entre classes que já se provou útil, mais o vocabulário para falar sobre ele. Dizer "isso aqui é um Observer" comunica em três palavras o que levaria um parágrafo.

Os 23 patterns catalogados pelo *Gang of Four* em 1994 se dividem em três famílias, pela pergunta que cada uma responde:

| Família | Pergunta que responde |
| --- | --- |
| **Criacionais** (5) | Como criar objetos sem amarrar o código à classe concreta? |
| **Estruturais** (7) | Como compor objetos em estruturas maiores mantendo tudo flexível? |
| **Comportamentais** (11) | Como distribuir responsabilidade e coordenar a conversa entre objetos? |

## Antes de aplicar qualquer um deles

Pattern é ferramenta, não meta. Código não fica melhor por conter mais patterns — fica melhor por resolver o problema com a menor complexidade que dá conta dele. O uso mais comum e mais caro é o prematuro: aplicar um pattern à variação que ainda não existe.

Por isso toda página aqui tem uma seção **Quando NÃO usar**. Ela costuma ser mais útil que a implementação.

Vale ler [Princípios](https://github.com/JoaoVitorLima242/project-patterns/tree/main/docs/principios) antes: quase todo pattern é a aplicação concreta de um princípio mais geral.

---

**Status de cada pattern → [mapa no README principal](https://github.com/JoaoVitorLima242/project-patterns/blob/main/README.md).** O mapa é a única fonte de verdade do que já foi escrito; esta página não repete essa informação para não sair de sincronia.
