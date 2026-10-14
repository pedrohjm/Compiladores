# Compiladores

Trabalho acadêmico do IFTM — Campus Avançado Uberaba Parque Tecnológico (professor
Rafael Godoi Orbolato). Analisadores **léxicos** e **sintáticos** escritos à mão em
Java, sem geradores (JFlex, ANTLR, yacc) e sem expressões regulares, para quatro
formatos: PPM, MTL, OBJ e Pascal Simplificado.

| Formato | Fase 1 — léxico | Fase 2 — sintático | Especificação |
|---|---|---|---|
| PPM (Portable Pixmap, P3/ASCII) | `analisador_lexico_PPM` | `analisador_sintatico_PPM` | `Context.md` / `CLAUDE-sintatico.md` |
| MTL (Material Template Library) | `analisador_lexico_MTL` | `analisador_sintatico_MTL` | `Context.md` / `CLAUDE-sintatico.md` |
| OBJ (Wavefront, versão texto) | `analisador_lexico_OBJ` | `analisador_sintatico_OBJ` | `Context.md` / `CLAUDE-sintatico.md` |
| Pascal Simplificado | `analisador_lexico_PASCAL` | `analisador_sintatico_PASCAL` | `BNF-Pascal_Simplificado_para_nosso_Compilador.md` |

Os três primeiros formatos se conectam: um `.obj` referencia um `.mtl` via `mtllib`,
e o `.mtl` referencia a textura — um `.ppm` — via `map_Kd`.

## Estrutura

```
.
├── Context.md                                       # spec dos lexers PPM/MTL/OBJ (fase 1)
├── CLAUDE-sintatico.md                              # spec dos parsers PPM/MTL/OBJ (fase 2)
├── CLAUDE-semantico.md                              # spec da fase 3 (semântica + geração de código)
├── BNF-Pascal_Simplificado_para_nosso_Compilador.md # gramática do Pascal Simplificado
├── Compiladores.code-workspace                      # workspace multi-root do VS Code
├── cube-tex.obj, cube.mtl, java-logo-pequeno.ppm    # arquivos de exemplo soltos
│
├── analisador_lexico_PPM/          # fase 1
├── analisador_lexico_MTL/
├── analisador_lexico_OBJ/
├── analisador_lexico_PASCAL/
│
├── analisador_sintatico_PPM/       # fase 2 (lexer da fase 1 + parser)
├── analisador_sintatico_MTL/
├── analisador_sintatico_OBJ/
├── analisador_sintatico_PASCAL/
│
├── analisador_sintatico_Professor/ # esqueleto de referência do professor
└── analisador_lexico/              # exercício antigo, independente da spec acima
```

`analisador_lexico_PPM` foi o projeto-modelo da fase 1 e `analisador_sintatico_Professor`
o da fase 2 — os demais replicam a mesma arquitetura e convenções. Cada projeto é
autocontido (a duplicação do pacote `lexico/` entre as fases é proposital: cada um é
entregável isoladamente). `analisador_lexico` é um exercício anterior, independente —
reconhece só identificador/número/`+`, não é um lexer de Pascal completo (esse é o papel
do `analisador_lexico_PASCAL`).

## Arquitetura comum

Projetos da **fase 1** (`analisador_lexico_<FORMATO>/`):

```
├── .vscode/
│   ├── launch.json       # run/debug do VS Code (compila em bin/, saída no Terminal)
│   └── settings.json     # sourcePaths=["src"], outputPath="bin"
├── src/
│   ├── App.java                      # lê a amostra e imprime os tokens
│   └── lexico/
│       ├── ClasseToken<Formato>.java # enum com as classes de token (terminais)
│       ├── Lexico<Formato>.java      # o autômato léxico, caractere a caractere
│       ├── Token.java                # linha + coluna + classe + valor
│       └── ValorToken.java           # texto / inteiro / decimal do token
├── README.md                         # boilerplate do VS Code Java
└── <amostra>                         # java-logo.ppm, cube.mtl, cube-tex.obj, fibonacci.pas
```

Projetos da **fase 2** (`analisador_sintatico_<FORMATO>/`) têm o mesmo `lexico/`,
reaproveitado sem alterações, mais:

```
└── src/sintatico/
    ├── Sintatico<Formato>.java       # parser recursivo-descendente, 1 método por não-terminal
    └── ErroSintaticoException.java   # aborta a descida no primeiro erro
```

### Análise léxica

- **Autômato manual**: `Lexico<Formato>.java` lê um caractere por vez com `BufferedReader`,
  rastreando linha e coluna desde o início.
- **Maximal munch**: o lexema é lido por inteiro antes de ser classificado — essencial no
  OBJ, onde `v`, `vt` e `vn` compartilham prefixo.
- **Erro léxico**: imprime `Erro Lexico. ... na linha X, coluna Y.` e encerra com
  `System.exit(1)`.
- **Saída**: uma linha por token — `Token [linha, coluna, classe=..., valor=...]`.

### Análise sintática

- **Descida recursiva**, um método por não-terminal, com o nome da regra da gramática
  (`arquivoObj`, `defFace`, `complementoNormal`, ...) e a produção BNF em comentário.
- **Consumo de tokens** via `expect(CLASSE)`, que dispara erro quando o token atual não é
  o esperado. Regras recursivas à direita (`<mais_pixels>`, `<linhas_obj>`,
  `<mais_expressao>`, ...) viram laços `while`, não recursão literal — um PPM de 400×400
  tem 160 mil pixels e estouraria a pilha.
- **Erro sintático**: `Erro sintatico na linha X, coluna Y: esperado A, encontrado B ('lexema')`,
  em `stderr`, encerrando com código 1. O parser para no **primeiro** erro, via exceção
  interna capturada em `analisar()` — sem erros em cascata.
- **Sucesso**: imprime `Analise sintatica concluida sem erros.`

### Decisões de implementação

- **`FLOAT` vs `INTEIRO`**: onde a gramática pede `FLOAT`, um inteiro é erro sintático
  (`Kd 1 0 0`, `Ns 10`, `vt 0 0.75`). Por isso o `cube-tex.obj` do projeto sintático usa
  `0.0` nas coordenadas `vt` que o arquivo solto da raiz escreve como `0`.
- **Pascal, `id := ...` vs chamada de procedimento**: ambos começam com `id`; o prefixo
  comum é consumido e a decisão sai do token seguinte (`:=` ou não), mantendo 1 token de
  lookahead por decisão.
- **Pascal, `(` em `<fator_logico>`**: tanto `<relacional>` quanto `( <expressao_logica> )`
  podem começar com `(` — ambiguidade LL(1) em aberto na gramática. Um `(` nesse ponto é
  sempre tratado como sub-expressão lógica entre parênteses (ex.: `not (x <> y)`).
- **Pascal, `;` obrigatório**: `<mais_sentencas> ::= ; <cont_sentencas>` não tem
  alternativa vazia, então todo comando — inclusive o último antes de `end` — exige `;`.
  Pelo mesmo motivo (`<sentencas>` sem produção vazia), um corpo vazio (`begin end.`) é
  rejeitado.

## Como compilar e rodar

Fase 1:

```bash
cd analisador_lexico_<FORMATO>
javac -d bin src/App.java src/lexico/*.java
java -cp bin App
```

Fase 2:

```bash
cd analisador_sintatico_<FORMATO>
javac -d bin src/App.java src/lexico/*.java src/sintatico/*.java
java -cp bin App
```

Ou pelo VS Code: abra o `Compiladores.code-workspace`, selecione a pasta do projeto e
aperte **F5** (compila e roda, saída no Terminal integrado). O `App.java` resolve o
caminho da amostra a partir da pasta da classe compilada, então funciona igual pelo
terminal, pelo F5 ou pelo Code Runner.

`bin/` é gerado pelo `javac` e não é versionado (ver `.gitignore`) — se sumir, é só
compilar de novo.
