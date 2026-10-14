# CLAUDE.md — Analisador Sintático

Instruções de projeto para o Claude Code.
Fase 2: análise sintática. Depende dos lexers descritos em `Context.md` (fase léxica).

## Contexto

Disciplina: **Compiladores** — IFTM, Campus Avançado Uberaba Parque Tecnológico.
Professor: Rafael Godoi Orbolato.

O objetivo é implementar **analisadores sintáticos** que consomem o fluxo de
tokens produzido pelos lexers e reconhecem a gramática de três formatos:

| Formato | Símbolo inicial | Entregável |
|---|---|---|
| `.ppm` | `<imagem_ppm>` | Parser que reconhece a gramática de imagens PPM |
| `.mtl` | `<arquivo_mtl>` | Parser que reconhece a gramática de arquivos MTL |
| `.obj` | `<arquivo_obj>` | Parser que reconhece a gramática de arquivos OBJ |

Os três formatos se conectam: um `.obj` referencia um `.mtl` via `mtllib`, e o
`.mtl` referencia a textura (por exemplo um `.ppm`) via `map_Kd`.

## Stack

> **TODO:** definir linguagem/ferramentas antes de gerar código.
> Preencher com os comandos reais assim que o projeto estiver montado.

- Linguagem: _(a definir)_
- Build: _(a definir)_
- Testes: _(a definir)_
- Execução: _(a definir)_

## Estrutura sugerida

```
.
├── src/
│   ├── ppm/
│   │   ├── lexer.*
│   │   └── parser.*
│   ├── mtl/
│   │   ├── lexer.*
│   │   └── parser.*
│   └── obj/
│       ├── lexer.*
│       └── parser.*
├── samples/
│   ├── cube.obj
│   ├── cube.mtl
│   └── texture.ppm
└── tests/
```

---

## 1. PPM — Portable Pixmap

No formato de imagem PPM, em modo ASCII, a imagem é literalmente um arquivo de
texto estruturado.

### Exemplo

```
P3              <- "Número mágico" que identifica o formato (P3 = PPM em ASCII)
3 2             <- Largura e Altura em pixels
255             <- Valor máximo de cor (geralmente 255 para RGB padrão)
255 0 0   0 255 0   0 0 255      <- Linha 1: Pixel Vermelho, Verde e Azul
255 255 0 255 255 255 0 0 0      <- Linha 2: Pixel Amarelo, Branco e Preto
```

### Regras de produção

```bnf
<imagem_ppm>   ::= <cabecalho> <lista_pixels> EOF

<cabecalho>    ::= MAGIC NUMERO NUMERO NUMERO

<lista_pixels> ::= <pixel> <mais_pixels>

<mais_pixels>  ::= <pixel> <mais_pixels>
                 | ε

<pixel>        ::= NUMERO NUMERO NUMERO
```

---

## 2. MTL — Material Template Library

O arquivo `.mtl` é o "arquivo de cabeçalho" de propriedades visuais do `.obj`.
O `.obj` não guarda a imagem da textura dentro dele — guarda o **mapeamento UV**
(coordenadas de textura) e diz à placa de vídeo qual parte da imagem deve ser
colada em qual triângulo. É o `.mtl` que informa o nome do arquivo de imagem.

### Exemplo (`cube.mtl`)

```mtl
newmtl texture
Ka 0.0 0.0 0.0
Kd 0.5 0.5 0.5
Ks 0.0 0.0 0.0
Ns 10.0
illum 2
map_Kd texture.ppm
```

### Regras de produção

```bnf
<arquivo_mtl>   ::= <materiais> EOF

<materiais>     ::= <material> <materiais>
                  | ε

<material>      ::= KW_NEWMTL IDENTIFICADOR <propriedades>

<propriedades>  ::= <propriedade> <propriedades>
                  | ε

<propriedade>   ::= <cor_amb>
                  | <cor_difusa>
                  | <cor_spec>
                  | <exp_spec>
                  | <mod_ilum>
                  | <mapa_textura>

<cor_amb>       ::= KW_KA FLOAT FLOAT FLOAT
<cor_difusa>    ::= KW_KD FLOAT FLOAT FLOAT
<cor_spec>      ::= KW_KS FLOAT FLOAT FLOAT
<exp_spec>      ::= KW_NS FLOAT
<mod_ilum>      ::= KW_ILLUM INTEIRO
<mapa_textura>  ::= KW_MAP_KD IDENTIFICADOR
```

---

## 3. OBJ — Wavefront

O formato de objeto 3D mais simples de se trabalhar é o Wavefront `.obj`
(em sua versão texto/ASCII).

### Exemplo (`cube.obj`)

```obj
# Import into Blender with Y-forward, Z-up
#
# Vertices:            Faces:
#     f-------g        +-------+
#    /.      /|       /.  5   /|  3 back
#   / .     / |      / .     / |
#  e-------h  |   2 +-------+ 1|
#  | b . . |. c     | . . . |. +
#  | .     | /y     | .  4  | /
#  |.      |/       |.      |/
#  a-------d        +---- x +-------+
#                            6
#                          bottom

# Material defined in separate file.
mtllib cube.mtl

g cube

# Vertices
v 0.0 0.0 0.0  # 1 a
v 0.0 1.0 0.0  # 2 b
v 1.0 1.0 0.0  # 3 c
v 1.0 0.0 0.0  # 4 d
v 0.0 0.0 1.0  # 5 e
v 0.0 1.0 1.0  # 6 f
v 1.0 1.0 1.0  # 7 g
v 1.0 0.0 1.0  # 8 h

# Normal vectors
# One for each face. Shared by all vertices in that face.
vn  1.0  0.0  0.0  # 1 cghd
vn -1.0  0.0  0.0  # 2 aefb
vn  0.0  1.0  0.0  # 3 gcbf
vn  0.0 -1.0  0.0  # 4 dhea
vn  0.0  0.0  1.0  # 5 hgfe
vn  0.0  0.0 -1.0  # 6 cdab

# Texture
# (u,v) coordinate into texture map image, ranging from 0.0 - 1.0.
vt 0.25 1.00  # 1  f(5) = f for face 5
vt 0.50 1.00  # 2  g(5)
vt 0    0.75  # 3  f(2)
vt 0.25 0.75  # 4  e(2,4,5)
vt 0.50 0.75  # 5  h(1,4,5)
vt 0.75 0.75  # 6  g(1)
vt 0    0.50  # 7  b(2)
vt 0.25 0.50  # 8  a(2,4,6)
vt 0.50 0.50  # 9  d(1,4,6)
vt 0.75 0.50  # 10 c(1)
vt 0.25 0.25  # 11 b(3,6)
vt 0.50 0.25  # 12 c(3,6)
vt 0.25 0     # 13 f(3)
vt 0.50 0     # 14 g(3)

# Define material for the following faces
usemtl texture

# Faces v/vt/vn
# Cada face = 2 triângulos (ccw) => 1-2-3 + 1-3-4

# Face 1: cghd = cgh + chd
f 3/10/1 7/6/1 8/5/1
f 3/10/1 8/5/1 4/9/1
# Face 2: aefb = aef + afb
f 1/8/2 5/4/2 6/3/2
f 1/8/2 6/3/2 2/7/2
# Face 3: gcbf = gcb + gbf
f 7/14/3 3/12/3 2/11/3
f 7/14/3 2/11/3 6/13/3
# Face 4: dhea = dhe + dea
f 4/9/4 8/5/4 5/4/4
f 4/9/4 5/4/4 1/8/4
# Face 5: hgfe = hgf + hfe
f 8/5/5 7/2/5 6/1/5
f 8/5/5 6/1/5 5/4/5
# Face 6: cdab = cda + cab
f 3/12/6 4/9/6 1/8/6
f 3/12/6 1/8/6 2/11/6
```

### Regras de produção

```bnf
<arquivo_obj>   ::= <linhas_obj> EOF

<linhas_obj>    ::= <comando> <linhas_obj>
                  | ε

<comando>       ::= <importa_mtl>
                  | <usa_mtl>
                  | <def_objeto>
                  | <def_grupo>
                  | <def_vertice>
                  | <def_uv>
                  | <def_normal>
                  | <def_face>

<importa_mtl>   ::= KW_MTLLIB IDENTIFICADOR
<usa_mtl>       ::= KW_USEMTL IDENTIFICADOR
<def_objeto>    ::= KW_O IDENTIFICADOR
<def_grupo>     ::= KW_G IDENTIFICADOR
<def_vertice>   ::= KW_V FLOAT FLOAT FLOAT
<def_uv>        ::= KW_VT FLOAT FLOAT
<def_normal>    ::= KW_VN FLOAT FLOAT FLOAT

<def_face>      ::= KW_F <conjunto> <conjunto> <conjunto>

<conjunto>               ::= INTEIRO <complemento_face>

<complemento_face>       ::= BARRA <dado_textura_ou_normal>
                           | ε

<dado_textura_ou_normal> ::= BARRA INTEIRO
                           | INTEIRO <complemento_normal>

<complemento_normal>     ::= BARRA INTEIRO
                           | ε
```

---

## Pontos de atenção da sintaxe

- **A gramática é LL(1):** dá para implementar por **descida recursiva** com
  1 token de lookahead. Toda alternativa é decidida pelo token atual.
- **`<def_face>` aceita exatamente 3 conjuntos** — só triângulos. Uma face com
  4 índices é erro sintático por esta gramática, mesmo sendo válida no formato
  Wavefront real.
- **As 4 formas de índice de face** são cobertas por `<conjunto>`:
  `v` (1), `v/vt` (1/2), `v//vn` (1//3) e `v/vt/vn` (1/2/3). Testar as quatro.
  A dupla barra é o caso que quebra implementações apressadas: depois da
  primeira `BARRA`, é preciso olhar se vem outra `BARRA` ou um `INTEIRO`.
- **`FLOAT` vs `INTEIRO` no `<def_uv>`:** o exemplo traz `vt 0 0.75` e
  `vt 0.25 0`. A gramática exige `FLOAT`, mas o lexer classifica `0` como
  `INTEIRO`. Decidir onde resolver — o lexer emitindo `FLOAT` nesse contexto,
  ou o parser aceitando `INTEIRO` onde espera `FLOAT` — e documentar a escolha.
  O mesmo vale para `Ns` no MTL.
- **`<propriedades>` do MTL é livre:** ordem e repetição não são restritas pela
  gramática. Um material sem nenhuma propriedade é sintaticamente válido.
- **`<materiais>` e `<linhas_obj>` aceitam ε:** um arquivo vazio (só `EOF`)
  é válido para MTL e OBJ. Já o PPM exige cabeçalho e **pelo menos um pixel**,
  porque `<lista_pixels>` começa com `<pixel>` obrigatório.
- **Contagem de pixels é semântica, não sintática:** a gramática do PPM não
  verifica se a quantidade de pixels bate com largura × altura, nem se os
  valores respeitam o máximo declarado. Não misturar essa validação no parser
  sintático — se for implementar, separar como análise semântica.
- **Índices de face existirem é semântica também:** `f 99/99/99` passa no
  parser mesmo sem 99 vértices declarados.
- **Comentários e espaços nunca chegam ao parser** — o lexer já os descartou.

## Convenções de implementação

- Parser por **descida recursiva**, um método por não-terminal, com nome igual
  ao da gramática (`arquivoObj`, `defFace`, `complementoNormal`, ...).
- Consumo de tokens via `match(TIPO)` / `expect(TIPO)`, que dispara erro quando
  o token atual não é o esperado.
- Mensagem de erro sintático deve dizer **o que era esperado, o que veio e onde**:
  `Erro sintático na linha 12, coluna 5: esperado FLOAT, encontrado IDENTIFICADOR ('abc')`
- Reaproveitar o lexer da fase anterior sem alterá-lo — o parser puxa tokens sob
  demanda (`proximoToken()`), não carrega a lista inteira, salvo se precisar de
  backtracking.
- Sem geradores de parser (yacc/bison/ANTLR): implementação manual, salvo
  indicação contrária do professor.
- Nomes de não-terminais e mensagens em português, seguindo a especificação.
