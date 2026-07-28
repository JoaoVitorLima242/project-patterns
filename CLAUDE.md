# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## O que este repositório é

Documentação de estudos sobre design patterns, princípios e arquitetura, em português. **Não é um projeto de software** — não há build, lint, suíte de testes, gerenciador de pacotes nem dependências. O produto é o Markdown, lido diretamente no GitHub.

O código existe apenas como exemplo executável ao lado de cada página, para provar que o que está publicado realmente roda.

## Regra mais importante

**O dono do repositório escreve as teses.** Não gere conteúdo teórico — a prosa explicativa das páginas é trabalho dele, deliberadamente.

O que é apropriado fazer sem pedido explícito: estrutura, código dos exemplos, correções, links, consistência do mapa. O que não é: escrever as seções "O problema", "A ideia", "Quando NÃO usar" e "Trade-offs" de um tópico. Se pedirem uma página nova, confirme se querem a prosa escrita ou apenas o esqueleto e o código.

## Comandos

Não existem `npm install`, `npm test`, `make` ou equivalentes. Os exemplos rodam direto, sem instalar nada:

```bash
node    docs/patterns/<família>/<pattern>/typescript/main.ts   # Node 22.18+ / 23.6+
python3 docs/patterns/<família>/<pattern>/python/main.py       # Python 3.10+
java    docs/patterns/<família>/<pattern>/java/Main.java       # JDK 17+
```

A verificação de um tópico é: rodar os três e conferir que a saída bate entre eles (o Java difere só na vírgula decimal, por locale pt-BR).

Como os links são URLs completas do GitHub (ver adiante), o checador resolve cada uma contra o filesystem local e denuncia qualquer link relativo remanescente, que viola a convenção. Os placeholders `<seção>/<tópico>` do `templates/pattern.md` são esperados:

```bash
python3 - <<'EOF'
import re, pathlib
BASE = "https://github.com/JoaoVitorLima242/project-patterns"
for md in sorted(pathlib.Path('.').rglob('*.md')):
    if '.git/' in str(md): continue
    for _, link in re.findall(r'\[([^\]]+)\]\(([^)]+)\)', md.read_text()):
        if link.startswith(BASE):
            caminho = re.sub(rf'^{re.escape(BASE)}/(blob|tree)/main/', '', link)
            if '<' in caminho: continue          # placeholder do template
            if not pathlib.Path(caminho).exists():
                print(f"NAO EXISTE   {md}  ->  {caminho}")
        elif not link.startswith(('http', '#', 'mailto:')):
            print(f"RELATIVO     {md}  ->  {link}")
EOF
```

## Estrutura

```
README.md          # porta de entrada + O MAPA de todos os tópicos previstos
CONTRIBUTING.md    # checklist para adicionar um tópico + convenções completas
templates/         # pattern.md — modelo a copiar para cada tópico novo
docs/
  principios/      # SOLID, composição vs herança, acoplamento...
  patterns/        # os 23 do GoF, em criacionais/estruturais/comportamentais
  arquitetura/     # Clean, Hexagonal, DDD tático, CQRS, frontend
```

Cada tópico é uma pasta com `README.md` + `typescript/main.ts` + `python/main.py` + `java/Main.java`.

`CONTRIBUTING.md` tem o passo a passo completo e as convenções. Leia antes de adicionar conteúdo.

## Armadilhas específicas deste repositório

Cada uma destas já causou um erro real:

### O mapa no README é a única fonte de verdade do status

Os `README.md` de seção descrevem a categoria e apontam de volta para o mapa — de propósito, não repetem status. Ao escrever um tópico, atualize `🔜` → `✅` **apenas** no README raiz. Não adicione listas de status nos índices de seção.

### Links são URLs completas do GitHub, nunca relativos

`https://github.com/JoaoVitorLima242/project-patterns/blob/main/<caminho>` para arquivos, `/tree/main/` para pastas. Nada de `./typescript/main.ts` ou `../../README.md` — o Markdown daqui circula fora do GitHub e caminho relativo quebra.

Consequência: um arquivo criado numa branch só resolve depois do merge em `main`. Isso é esperado, não é link quebrado.

### Links para tópicos `🔜` ficam como texto puro

Um tópico ainda não escrito não tem pasta, então linkar para ele dá 404 no GitHub. Nas seções "Patterns relacionados", tópicos pendentes ficam em **negrito** sem link. Quando um tópico é escrito, procure menções a ele nas páginas existentes e converta em link.

### Pastas nascem sob demanda

Não crie diretórios vazios para os tópicos `🔜`. O Git não versiona pasta vazia e o mapa já registra o que está previsto.

### Java: a classe `Main` deve ser a primeira do arquivo

O launcher de arquivo único (`java Main.java`) executa a **primeira** classe declarada. Com `record Pedido` no topo, ele falha com `can't find main(String[]) method in class: Pedido`. Records e demais tipos vão **abaixo** de `public class Main`.

### TypeScript: sem construções que exigem transformação

Os `.ts` rodam pelo *type stripping* do Node, que apaga tipos mas não transforma código. **Não funcionam:** `enum`, `namespace`, decorators e parameter properties (`constructor(private x: T)`).

Use objeto `as const` + union type no lugar de `enum`, e declare o campo atribuindo no corpo do construtor.

### Trecho inline vs. arquivo completo

A página mostra o núcleo do pattern inline (~20–30 linhas) e linka o arquivo executável. Imports, setup, `main`, prints e Concrete Strategies repetitivas ficam **só** no arquivo. Essa duplicação parcial é o custo aceito do formato — quanto menor o trecho inline, menor a chance de divergir.

## Convenções de escrita

- **Prosa em português**, pastas em **inglês** e `kebab-case` (`abstract-factory`, `clean-architecture`).
- Termos técnicos consagrados não se traduzem: "Strategy", "Observer", "Ports & Adapters".
- Página sempre em `README.md`, para o GitHub renderizar ao abrir a pasta.
- Diagramas em blocos ` ```mermaid ` — o GitHub renderiza nativamente, sem imagem para manter.
- Comentários no código explicam o **papel no pattern** ("O Context não sabe COMO o frete é calculado"), não a sintaxe.
- Código idiomático de cada linguagem, não tradução literal do Java. Quando o jeito natural difere (função de primeira classe em Python no lugar de uma hierarquia), use o natural e comente a diferença na página.
- As seções "Quando NÃO usar" e "Trade-offs" carregam o valor do repositório. Tópico sem elas fica `🚧`, não `✅`.
