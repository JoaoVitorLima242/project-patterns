# Como contribuir

Este documento é o passo a passo para adicionar um tópico ao repositório. Serve tanto para quem quiser contribuir quanto para mim mesmo daqui a três meses, quando eu não lembrar mais das convenções.

## Adicionando um tópico

1. **Crie a pasta** dentro da seção correspondente:

   ```
   docs/patterns/comportamentais/observer/
   docs/principios/ocp/
   docs/arquitetura/clean-architecture/
   ```

   Pastas nascem sob demanda — o repositório não guarda diretórios vazios para os tópicos ainda não escritos. Quem lista o que existe é o mapa.

2. **Copie o template** para dentro dela:

   ```bash
   cp templates/pattern.md docs/patterns/comportamentais/observer/README.md
   ```

3. **Escreva.** As seções estão comentadas dentro do template. Comece pelo problema, sempre — a solução só faz sentido depois que a dor está clara. Apague os comentários `<!-- ... -->` conforme preencher.

4. **Adicione os exemplos** nas três linguagens, em `typescript/main.ts`, `python/main.py` e `java/Main.java`.

5. **Rode os três** e confira que a saída faz sentido. Este passo é o motivo de os exemplos existirem como arquivos: é o que impede o repositório de publicar código que não compila.

6. **Atualize o mapa** no [README.md](https://github.com/JoaoVitorLima242/project-patterns/blob/main/README.md): troque o `🔜` por `✅` e transforme o nome do tópico em link.

7. **Procure menções ao tópico** nas páginas já escritas — na seção "Patterns relacionados" elas ficam como texto puro enquanto o alvo não existe. Agora viram link.

## Convenções

### Nomes e idioma

- **Pastas em inglês e `kebab-case`**: `strategy`, `abstract-factory`, `clean-architecture`. Casa com a literatura e com o que as pessoas pesquisam.
- **Prosa em português.** Termos técnicos consagrados ficam no original — não traduza "Strategy", "Observer", "Ports & Adapters".
- **Página sempre em `README.md`**, para o GitHub renderizar ao abrir a pasta.

### Links: URL completa, nunca caminho relativo

Todo link entre páginas e para arquivos de código usa a URL completa do GitHub:

```markdown
[Exemplo completo](https://github.com/JoaoVitorLima242/project-patterns/blob/main/docs/principios/srp/typescript/main.ts)
```

Não `./typescript/main.ts` nem `../../README.md`. O motivo é que o Markdown daqui circula fora do GitHub — copiado para outro lugar, lido num agregador, colado numa conversa — e caminho relativo quebra em todos esses casos.

Use `/blob/main/` para arquivos e `/tree/main/` para pastas. Como os links apontam para `main`, um arquivo criado numa branch só resolve depois do merge — o que é esperado.

### Estrutura da página

Siga as seções do template, na ordem. A previsibilidade é o que permite comparar dois patterns rapidamente.

Duas seções carregam o valor real do repositório e não devem ser puladas:

- **Quando NÃO usar** — onde o pattern é overkill, com qual outro ele é confundido.
- **Trade-offs** — o que se paga. Todo pattern cobra alguma coisa.

Se alguma delas ficou vazia, o tópico ainda não está entendido o bastante para ser publicado. Deixe como `🚧 Em progresso` no mapa.

### Diagramas

Use blocos ` ```mermaid `. O GitHub renderiza nativamente, então o diagrama vive no próprio Markdown — sem imagem para manter, sem ferramenta externa. Mantenha enxuto: só os participantes e as relações entre eles.

### Código

- **Um arquivo por linguagem, autocontido**, com uma demonstração que imprime resultado ao rodar.
- **Zero dependências externas.** Só a biblioteca padrão. Quem clona não instala nada.
- **O mesmo cenário nas três linguagens** — facilita comparar as abordagens.
- **Código idiomático da linguagem**, não uma tradução literal do Java. Se o jeito natural em Python for outro, use o natural e comente a diferença na página — por exemplo, quando uma função de primeira classe substitui uma hierarquia de classes.
- Comentários no código explicam o **papel no pattern** ("O Context não sabe COMO o frete é calculado"), não a sintaxe.

#### Restrição do TypeScript

Os exemplos rodam via *type stripping* do Node, que apaga os tipos sem transformar o código. Construções que exigem transformação **não funcionam**:

| Não use | Use no lugar |
| --- | --- |
| `enum` | objeto `as const` + union type |
| `constructor(private x: T)` | declarar o campo e atribuir no corpo |
| `namespace` | módulos |
| decorators | composição explícita |

Na prática é uma limitação leve, e mantém o código mais próximo de JS — o que facilita traduzir para Python e Java.

### Trecho inline vs. arquivo completo

A página mostra o **núcleo do pattern inline** (~20–30 linhas) e linka o arquivo executável no fim de cada bloco. O leitor entende o pattern sem sair do GitHub; quem quiser rodar clica no link.

Deixe fora do trecho inline: imports, setup, `main`, prints e as Concrete Strategies repetitivas (uma basta para mostrar o padrão). Quanto menor o trecho, menor a chance de ele divergir do arquivo com o tempo — essa duplicação é o custo aceito do formato.

### Status no mapa

O mapa no README principal é a **única fonte de verdade** do que está escrito. Os `README.md` de seção descrevem a categoria e apontam de volta para o mapa — não repita status neles, ou os dois saem de sincronia.

| | |
| --- | --- |
| ✅ | Página completa, incluindo "Quando NÃO usar" e "Trade-offs" |
| 🚧 | Existe e é legível, mas tem seção faltando |
| 🔜 | Previsto, ainda sem pasta |
