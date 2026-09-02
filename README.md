# Compiladores

Trabalho acadêmico do IFTM — Campus Avançado Uberaba Parque Tecnológico (professor
Rafael Godoi Orbolato). Implementação de **analisadores léxicos** (scanners manuais,
sem geradores como JFlex/ANTLR) para quatro linguagens/formatos.

## Estrutura

```
.
├── Context.md                                        # especificação dos lexers PPM/MTL/OBJ
├── BNF-Pascal_Simplificado_para_nosso_Compilador.md   # gramática do Pascal simplificado
├── Compiladores.code-workspace                        # workspace multi-root do VS Code
├── cube-tex.obj, cube.mtl, java-logo-pequeno.ppm       # arquivos de exemplo soltos
│
├── analisador_lexico_PPM/       # lexer do formato PPM (P3/ASCII)
├── analisador_lexico_MTL/       # lexer do formato MTL (Wavefront Material)
├── analisador_lexico_OBJ/       # lexer do formato OBJ (Wavefront geometria)
├── analisador_lexico_PASCAL/    # lexer do Pascal simplificado (ver BNF)
└── analisador_lexico/           # exercício antigo/separado (não segue a spec acima)
```

`analisador_lexico_PPM` foi o projeto-modelo: os outros três (`MTL`, `OBJ`, `PASCAL`)
replicam a mesma arquitetura e convenções. `analisador_lexico` é um exercício anterior,
independente — reconhece só identificador/número/`+`, não é um lexer de Pascal completo
(esse é o papel do `analisador_lexico_PASCAL`).

## Arquitetura comum aos quatro lexers

Cada `analisador_lexico_<FORMATO>/` segue a mesma organização:

```
analisador_lexico_<FORMATO>/
├── .vscode/
│   ├── launch.json     # config de run/debug do VS Code (compila em bin/, roda no Terminal)
│   └── settings.json   # java.project.sourcePaths=["src"], outputPath="bin"
├── src/
│   ├── App.java                     # ponto de entrada: le o sample e imprime os tokens
│   └── lexico/
│       ├── ClasseToken<Formato>.java  # enum com os tipos de token (terminais)
│       ├── Lexico<Formato>.java       # o scanner: le o arquivo caractere a caractere
│       ├── Token.java                 # linha + coluna + classe + valor
│       └── ValorToken.java            # texto/numero/decimal do token
├── README.md            # boilerplate padrão do VS Code Java
└── <arquivo>.<ext>      # arquivo de amostra usado pelo App.java (cube.mtl, fibonacci.pas...)
```

- **Scanner manual**: cada `Lexico<Formato>.java` implementa um autômato à mão, lendo
  um caractere por vez com `BufferedReader`, rastreando linha/coluna desde o início.
- **Maximal munch**: o lexema é lido por inteiro antes de ser classificado (essencial no
  OBJ, onde `v`, `vt` e `vn` compartilham o mesmo prefixo).
- **Erros léxicos**: caractere/número inválido imprime `Erro Lexico. ... na linha X,
  coluna Y.` e encerra o programa (`System.exit(1)`).
- **Saída**: uma linha por token, no formato `Token [linha, coluna, classe=..., valor=...]`.

## Como compilar e rodar

Em qualquer um dos quatro projetos:

```bash
cd analisador_lexico_<FORMATO>
javac -d bin src/App.java src/lexico/*.java
java -cp bin App
```

Ou pelo VS Code: abra o `Compiladores.code-workspace`, selecione a pasta do projeto
desejado e aperte **F5** (compila e roda automaticamente, saída no Terminal integrado).

`bin/` é gerado pelo `javac` e não é versionado (ver `.gitignore`) — se sumir, é só
compilar de novo.
