# CLAUDE.md

Instruções de projeto para o Claude Code.

## Contexto

Trabalho acadêmico do IFTM — Campus Avançado Uberaba Parque Tecnológico.
Professor: Rafael Godoi Orbolato.

O objetivo é implementar **analisadores léxicos** para três formatos de arquivo
usados em computação gráfica:

| Formato | Descrição | Entregável |
|---|---|---|
| `.ppm` | Portable Pixmap (modo ASCII / P3) | Lexer que reconhece os tokens de imagens PPM |
| `.mtl` | Material Template Library | Lexer que reconhece os tokens de arquivos MTL |
| `.obj` | Wavefront OBJ (versão texto/ASCII) | Lexer que reconhece os tokens de arquivos OBJ |

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
│   ├── ppm/          # lexer PPM
│   ├── mtl/          # lexer MTL
│   └── obj/          # lexer OBJ
├── samples/
│   ├── cube.obj
│   ├── cube.mtl
│   └── texture.ppm
└── tests/
```

---

## 1. PPM — Portable Pixmap

No formato PPM em modo ASCII, a imagem é literalmente um arquivo de texto
estruturado.

### Exemplo

```
P3              <- "Número mágico" que identifica o formato (P3 = PPM em ASCII)
3 2             <- Largura e Altura em pixels
255             <- Valor máximo de cor (geralmente 255 para RGB padrão)
255 0 0   0 255 0   0 0 255      <- Linha 1: Pixel Vermelho, Verde e Azul
255 255 0 255 255 255 0 0 0      <- Linha 2: Pixel Amarelo, Branco e Preto
```

### Tokens léxicos (terminais)

| Token | Descrição |
|---|---|
| `MAGIC` | A string exata `P3` |
| `NUMERO` | Inteiros positivos (ex: 255, 0, 128) |
| `EOF` | Fim do arquivo |

### Regras

- `#` indica comentário: o restante da linha deve ser ignorado.
- Espaços, tabs e quebras de linha são separadores e não geram token.

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

### Tokens léxicos (terminais)

| Token | Descrição |
|---|---|
| `KW_NEWMTL` | A string exata `newmtl` |
| `KW_KD` | A string exata `Kd` (cor difusa) |
| `KW_MAP_KD` | A string exata `map_Kd` (arquivo de textura) |
| `KW_KA` | A string exata `Ka` (cor ambiente) |
| `KW_KS` | A string exata `Ks` (cor especular) |
| `KW_NS` | A string exata `Ns` (expoente especular / brilho) |
| `KW_ILLUM` | A string exata `illum` (modelo de iluminação) |
| `INTEIRO` | Número inteiro (necessário para o parâmetro `illum`) |
| `FLOAT` | Número decimal (ex: 1.0, 0.5) |
| `IDENTIFICADOR` | Nome do material ou do arquivo (ex: `MatMadeira`, `textura.ppm`) |
| `EOF` | Fim do arquivo |

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

### Tokens léxicos (terminais)

| Token | Descrição |
|---|---|
| `KW_MTLLIB` | A string exata `mtllib` |
| `KW_USEMTL` | A string exata `usemtl` |
| `KW_V` | A string exata `v` (vértice espacial) |
| `KW_VT` | A string exata `vt` (vértice de textura / UV) |
| `KW_VN` | A string exata `vn` (vetor normal) |
| `KW_F` | A string exata `f` (face / polígono) |
| `KW_G` | A string exata `g` (declaração de grupo) |
| `KW_O` | A string exata `o` (declaração de objeto) |
| `BARRA` | O caractere exato `/` |
| `FLOAT` | Número decimal, podendo ser negativo (ex: -0.5, 1.0) |
| `INTEIRO` | Número inteiro positivo, índice (ex: 1, 2, 3) |
| `IDENTIFICADOR` | Nome de arquivos ou materiais |
| `EOF` | Fim do arquivo |

---

## Pontos de atenção do léxico

Detalhes que costumam quebrar a implementação — verificar em todos os lexers:

- **Maximal munch:** `vt` e `vn` devem casar antes de `v`. Ler o lexema inteiro
  até o delimitador e só então classificar, em vez de decidir pelo primeiro
  caractere.
- **`FLOAT` vs `INTEIRO`:** `1.0` é FLOAT, `1` é INTEIRO. O ponto decide.
  Atenção a formas como `.5`, `1.`, `0` e notação científica se for aceitá-las.
- **Sinal negativo:** só aparece em FLOAT no OBJ (`vn -1.0 0.0 0.0`). Os índices
  de face e os valores do PPM são positivos.
- **`IDENTIFICADOR` com ponto:** `texture.ppm` e `cube.mtl` são um único token,
  não IDENTIFICADOR + `.` + IDENTIFICADOR. O padrão precisa aceitar ponto,
  underscore e hífen no meio do nome, sem colidir com FLOAT.
- **Palavras-chave sensíveis a maiúsculas:** `Kd`, `Ka`, `Ks`, `Ns`, `map_Kd`,
  `illum`, `newmtl` — comparar exatamente como especificado.
- **Comentários:** `#` até o fim da linha, inclusive quando aparece no fim de uma
  linha com conteúdo (`v 0.0 0.0 0.0  # 1 a`). Nunca gera token.
- **Faces:** `f 3/10/1` produz `KW_F INTEIRO BARRA INTEIRO BARRA INTEIRO`.
  A validação de que os índices existem é papel do parser, não do lexer.
- **`MAGIC` do PPM:** `P3` é token próprio e aparece obrigatoriamente no início.
- **Espaços em branco:** espaço, tab, `\r` e `\n` são apenas separadores.
- **Caractere inválido:** emitir erro léxico com **linha e coluna**, não abortar
  silenciosamente.

## Convenções de implementação

- Cada token carrega: tipo, lexema, linha e coluna.
- Rastrear linha/coluna desde o início — retrofitar isso depois dá trabalho.
- Saída padrão sugerida, uma linha por token:
  `<TIPO> lexema (linha, coluna)`
- Sem dependências externas de parsing: a análise léxica é implementada à mão
  (autômato / scanner manual), salvo indicação contrária do professor.
- Comentários e nomes de tokens em português, seguindo os nomes da especificação.
