package sintatico;

import lexico.ClasseTokenPascal;
import lexico.LexicoPascal;
import lexico.Token;

// Implementa a gramatica do BNF-Pascal_Simplificado_para_nosso_Compilador.md
// (secao "## Gramatica", antes de "## Acoes Semanticas..."). Os marcadores
// {AXX} do documento sao pontos de acao semantica (geracao de assembly) da
// fase seguinte e nao fazem parte da sintaxe: sao ignorados aqui de proposito.
public class SintaticoPascal {

    private final LexicoPascal lexico;
    private Token token;

    public SintaticoPascal(LexicoPascal lexico) {
        this.lexico = lexico;
    }

    public void analisar() {
        try {
            token = lexico.getNextToken();
            programa();
            System.out.println("Analise sintatica concluida sem erros.");
        } catch (ErroSintaticoException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    // <programa> ::= program id ; <corpo> .
    private void programa() {
        expect(ClasseTokenPascal.KW_PROGRAM);
        expect(ClasseTokenPascal.IDENTIFICADOR);
        expect(ClasseTokenPascal.PONTO_VIRGULA);
        corpo();
        expect(ClasseTokenPascal.PONTO);
        expect(ClasseTokenPascal.EOF);
    }

    // <corpo> ::= <declara> <rotina> begin <sentencas> end
    private void corpo() {
        declara();
        rotina();
        expect(ClasseTokenPascal.KW_BEGIN);
        sentencas();
        expect(ClasseTokenPascal.KW_END);
    }

    // ---------------------------------------------------------------
    // Declaracoes de variaveis
    // ---------------------------------------------------------------

    // <declara> ::= var <dvar> <mais_dc> | ε
    private void declara() {
        if (token.getClasse() == ClasseTokenPascal.KW_VAR) {
            token = lexico.getNextToken();
            dvar();
            maisDc();
        }
    }

    // <mais_dc> ::= ; <cont_dc>
    private void maisDc() {
        expect(ClasseTokenPascal.PONTO_VIRGULA);
        contDc();
    }

    // <cont_dc> ::= <dvar> <mais_dc> | ε
    private void contDc() {
        if (token.getClasse() == ClasseTokenPascal.IDENTIFICADOR) {
            dvar();
            maisDc();
        }
    }

    // <dvar> ::= <variaveis> : <tipo_var>
    private void dvar() {
        variaveis();
        expect(ClasseTokenPascal.DOIS_PONTOS);
        tipoVar();
    }

    // <tipo_var> ::= integer
    private void tipoVar() {
        expect(ClasseTokenPascal.KW_INTEGER);
    }

    // <variaveis> ::= id <mais_var>
    private void variaveis() {
        expect(ClasseTokenPascal.IDENTIFICADOR);
        maisVar();
    }

    // <mais_var> ::= , <variaveis> | ε
    private void maisVar() {
        if (token.getClasse() == ClasseTokenPascal.VIRGULA) {
            token = lexico.getNextToken();
            variaveis();
        }
    }

    // ---------------------------------------------------------------
    // Procedimentos e funcoes
    // ---------------------------------------------------------------

    // <rotina> ::= <procedimento> | <funcao> | ε
    private void rotina() {
        if (token.getClasse() == ClasseTokenPascal.KW_PROCEDURE) {
            procedimento();
        } else if (token.getClasse() == ClasseTokenPascal.KW_FUNCTION) {
            funcao();
        }
    }

    // <procedimento> ::= procedure id <parametros> ; <corpo> ; <rotina>
    private void procedimento() {
        expect(ClasseTokenPascal.KW_PROCEDURE);
        expect(ClasseTokenPascal.IDENTIFICADOR);
        parametros();
        expect(ClasseTokenPascal.PONTO_VIRGULA);
        corpo();
        expect(ClasseTokenPascal.PONTO_VIRGULA);
        rotina();
    }

    // <funcao> ::= function id <parametros> : <tipo_funcao> ; <corpo> ; <rotina>
    private void funcao() {
        expect(ClasseTokenPascal.KW_FUNCTION);
        expect(ClasseTokenPascal.IDENTIFICADOR);
        parametros();
        expect(ClasseTokenPascal.DOIS_PONTOS);
        tipoFuncao();
        expect(ClasseTokenPascal.PONTO_VIRGULA);
        corpo();
        expect(ClasseTokenPascal.PONTO_VIRGULA);
        rotina();
    }

    // <parametros> ::= ( <lista_parametros> ) | ε
    private void parametros() {
        if (token.getClasse() == ClasseTokenPascal.ABRE_PARENTESES) {
            token = lexico.getNextToken();
            listaParametros();
            expect(ClasseTokenPascal.FECHA_PARENTESES);
        }
    }

    // <lista_parametros> ::= <lista_id> : <tipo_var> <cont_lista_par>
    private void listaParametros() {
        listaId();
        expect(ClasseTokenPascal.DOIS_PONTOS);
        tipoVar();
        contListaPar();
    }

    // <cont_lista_par> ::= ; <lista_parametros> | ε
    private void contListaPar() {
        if (token.getClasse() == ClasseTokenPascal.PONTO_VIRGULA) {
            token = lexico.getNextToken();
            listaParametros();
        }
    }

    // <lista_id> ::= id <cont_lista_id>
    private void listaId() {
        expect(ClasseTokenPascal.IDENTIFICADOR);
        contListaId();
    }

    // <cont_lista_id> ::= , <lista_id> | ε
    private void contListaId() {
        if (token.getClasse() == ClasseTokenPascal.VIRGULA) {
            token = lexico.getNextToken();
            listaId();
        }
    }

    // <tipo_funcao> ::= integer
    private void tipoFuncao() {
        expect(ClasseTokenPascal.KW_INTEGER);
    }

    // ---------------------------------------------------------------
    // Comandos
    // ---------------------------------------------------------------

    // <sentencas> ::= <comando> <mais_sentencas>
    private void sentencas() {
        comando();
        maisSentencas();
    }

    // <mais_sentencas> ::= ; <cont_sentencas>
    // Sem alternativa epsilon: todo comando (inclusive o ultimo de um bloco)
    // exige ";" depois dele. Confirmado nos programas de teste do BNF, que
    // sempre trazem ";" antes de qualquer "end"/"until".
    private void maisSentencas() {
        expect(ClasseTokenPascal.PONTO_VIRGULA);
        contSentencas();
    }

    // <cont_sentencas> ::= <sentencas> | ε
    private void contSentencas() {
        if (ehInicioDeComando()) {
            sentencas();
        }
    }

    private boolean ehInicioDeComando() {
        switch (token.getClasse()) {
            case KW_READ:
            case KW_WRITE:
            case KW_WRITELN:
            case KW_FOR:
            case KW_REPEAT:
            case KW_WHILE:
            case KW_IF:
            case IDENTIFICADOR:
                return true;
            default:
                return false;
        }
    }

    // <var_read> ::= id <mais_var_read>
    private void varRead() {
        expect(ClasseTokenPascal.IDENTIFICADOR);
        maisVarRead();
    }

    // <mais_var_read> ::= , <var_read> | ε
    private void maisVarRead() {
        if (token.getClasse() == ClasseTokenPascal.VIRGULA) {
            token = lexico.getNextToken();
            varRead();
        }
    }

    // <exp_write> ::= id <mais_exp_write> | string <mais_exp_write> | intnum <mais_exp_write>
    private void expWrite() {
        if (token.getClasse() == ClasseTokenPascal.IDENTIFICADOR
                || token.getClasse() == ClasseTokenPascal.CADEIA
                || token.getClasse() == ClasseTokenPascal.INTEIRO) {
            token = lexico.getNextToken();
            maisExpWrite();
        } else {
            erro("IDENTIFICADOR, CADEIA ou INTEIRO");
        }
    }

    // <mais_exp_write> ::= , <exp_write> | ε
    private void maisExpWrite() {
        if (token.getClasse() == ClasseTokenPascal.VIRGULA) {
            token = lexico.getNextToken();
            expWrite();
        }
    }

    // <comando> ::= read(<var_read>) | write(<exp_write>) | writeln(<exp_write>)
    //             | for id := <expressao> to <expressao> do begin <sentencas> end
    //             | repeat <sentencas> until (<expressao_logica>)
    //             | while (<expressao_logica>) do begin <sentencas> end
    //             | if (<expressao_logica>) then begin <sentencas> end <pfalsa>
    //             | id := <expressao>
    //             | <chamada_procedimento>
    private void comando() {
        switch (token.getClasse()) {
            case KW_READ:
                token = lexico.getNextToken();
                expect(ClasseTokenPascal.ABRE_PARENTESES);
                varRead();
                expect(ClasseTokenPascal.FECHA_PARENTESES);
                break;
            case KW_WRITE:
                token = lexico.getNextToken();
                expect(ClasseTokenPascal.ABRE_PARENTESES);
                expWrite();
                expect(ClasseTokenPascal.FECHA_PARENTESES);
                break;
            case KW_WRITELN:
                token = lexico.getNextToken();
                expect(ClasseTokenPascal.ABRE_PARENTESES);
                expWrite();
                expect(ClasseTokenPascal.FECHA_PARENTESES);
                break;
            case KW_FOR:
                token = lexico.getNextToken();
                expect(ClasseTokenPascal.IDENTIFICADOR);
                expect(ClasseTokenPascal.ATRIBUICAO);
                expressao();
                expect(ClasseTokenPascal.KW_TO);
                expressao();
                expect(ClasseTokenPascal.KW_DO);
                expect(ClasseTokenPascal.KW_BEGIN);
                sentencas();
                expect(ClasseTokenPascal.KW_END);
                break;
            case KW_REPEAT:
                token = lexico.getNextToken();
                sentencas();
                expect(ClasseTokenPascal.KW_UNTIL);
                expect(ClasseTokenPascal.ABRE_PARENTESES);
                expressaoLogica();
                expect(ClasseTokenPascal.FECHA_PARENTESES);
                break;
            case KW_WHILE:
                token = lexico.getNextToken();
                expect(ClasseTokenPascal.ABRE_PARENTESES);
                expressaoLogica();
                expect(ClasseTokenPascal.FECHA_PARENTESES);
                expect(ClasseTokenPascal.KW_DO);
                expect(ClasseTokenPascal.KW_BEGIN);
                sentencas();
                expect(ClasseTokenPascal.KW_END);
                break;
            case KW_IF:
                token = lexico.getNextToken();
                expect(ClasseTokenPascal.ABRE_PARENTESES);
                expressaoLogica();
                expect(ClasseTokenPascal.FECHA_PARENTESES);
                expect(ClasseTokenPascal.KW_THEN);
                expect(ClasseTokenPascal.KW_BEGIN);
                sentencas();
                expect(ClasseTokenPascal.KW_END);
                pfalsa();
                break;
            case IDENTIFICADOR:
                // <id := expressao> e <chamada_procedimento> comecam com o
                // mesmo "id"; consome-se o prefixo comum aqui e decide-se
                // pelo proximo token (":=" ou nao), sem precisar de mais de
                // 1 token de lookahead.
                token = lexico.getNextToken();
                if (token.getClasse() == ClasseTokenPascal.ATRIBUICAO) {
                    token = lexico.getNextToken();
                    expressao();
                } else {
                    chamadaProcedimento();
                }
                break;
            default:
                erro("READ, WRITE, WRITELN, FOR, REPEAT, WHILE, IF ou IDENTIFICADOR");
        }
    }

    // <pfalsa> ::= else begin <sentencas> end | ε
    private void pfalsa() {
        if (token.getClasse() == ClasseTokenPascal.KW_ELSE) {
            token = lexico.getNextToken();
            expect(ClasseTokenPascal.KW_BEGIN);
            sentencas();
            expect(ClasseTokenPascal.KW_END);
        }
    }

    // <chamada_procedimento> ::= id <argumentos>
    // O "id" ja foi consumido em comando() (prefixo compartilhado com a
    // atribuicao "id := ..."), entao este metodo so cuida de <argumentos>.
    private void chamadaProcedimento() {
        argumentos();
    }

    // <argumentos> ::= ( <lista_arg> ) | ε
    private void argumentos() {
        if (token.getClasse() == ClasseTokenPascal.ABRE_PARENTESES) {
            token = lexico.getNextToken();
            listaArg();
            expect(ClasseTokenPascal.FECHA_PARENTESES);
        }
    }

    // <lista_arg> ::= <expressao> <cont_lista_arg>
    private void listaArg() {
        expressao();
        contListaArg();
    }

    // <cont_lista_arg> ::= , <lista_arg> | ε
    private void contListaArg() {
        if (token.getClasse() == ClasseTokenPascal.VIRGULA) {
            token = lexico.getNextToken();
            listaArg();
        }
    }

    // ---------------------------------------------------------------
    // Expressoes logicas e relacionais
    // ---------------------------------------------------------------

    // <expressao_logica> ::= <termo_logico> <mais_expr_logica>
    private void expressaoLogica() {
        termoLogico();
        maisExprLogica();
    }

    // <mais_expr_logica> ::= or <termo_logico> <mais_expr_logica> | ε
    // Escrito como laco (nao recursao literal) para nao estourar a pilha em
    // expressoes com muitos "or" encadeados.
    private void maisExprLogica() {
        while (token.getClasse() == ClasseTokenPascal.KW_OR) {
            token = lexico.getNextToken();
            termoLogico();
        }
    }

    // <termo_logico> ::= <fator_logico> <mais_termo_logico>
    private void termoLogico() {
        fatorLogico();
        maisTermoLogico();
    }

    // <mais_termo_logico> ::= and <fator_logico> <mais_termo_logico> | ε
    private void maisTermoLogico() {
        while (token.getClasse() == ClasseTokenPascal.KW_AND) {
            token = lexico.getNextToken();
            fatorLogico();
        }
    }

    // <fator_logico> ::= <relacional> | ( <expressao_logica> ) | not <fator_logico> | true | false
    //
    // Decisao de implementacao: <relacional> (via <expressao>/<fator>) e a
    // alternativa "( <expressao_logica> )" podem comecar com "(" -- e um
    // ponto de ambiguidade LL(1) em aberto nesta gramatica. Aqui, um "(" na
    // entrada de <fator_logico> e sempre tratado como abertura de uma
    // sub-expressao logica entre parenteses (ex.: "not (x <> y)"), nunca
    // como o parenteses interno de <fator> ::= ( <expressao> ). Nenhum dos
    // programas de teste do enunciado depende do outro caminho.
    private void fatorLogico() {
        switch (token.getClasse()) {
            case ABRE_PARENTESES:
                token = lexico.getNextToken();
                expressaoLogica();
                expect(ClasseTokenPascal.FECHA_PARENTESES);
                break;
            case KW_NOT:
                token = lexico.getNextToken();
                fatorLogico();
                break;
            case KW_TRUE:
                token = lexico.getNextToken();
                break;
            case KW_FALSE:
                token = lexico.getNextToken();
                break;
            default:
                relacional();
        }
    }

    // <relacional> ::= <expressao> (= | > | >= | < | <= | <>) <expressao>
    private void relacional() {
        expressao();
        switch (token.getClasse()) {
            case IGUAL:
            case MAIOR:
            case MAIOR_IGUAL:
            case MENOR:
            case MENOR_IGUAL:
            case DIFERENTE:
                token = lexico.getNextToken();
                break;
            default:
                erro("=, >, >=, <, <= ou <>");
        }
        expressao();
    }

    // ---------------------------------------------------------------
    // Expressoes aritmeticas
    // ---------------------------------------------------------------

    // <expressao> ::= <termo> <mais_expressao>
    private void expressao() {
        termo();
        maisExpressao();
    }

    // <mais_expressao> ::= + <termo> <mais_expressao> | - <termo> <mais_expressao> | ε
    private void maisExpressao() {
        while (token.getClasse() == ClasseTokenPascal.MAIS || token.getClasse() == ClasseTokenPascal.MENOS) {
            token = lexico.getNextToken();
            termo();
        }
    }

    // <termo> ::= <fator> <mais_termo>
    private void termo() {
        fator();
        maisTermo();
    }

    // <mais_termo> ::= * <fator> <mais_termo> | / <fator> <mais_termo> | ε
    private void maisTermo() {
        while (token.getClasse() == ClasseTokenPascal.VEZES || token.getClasse() == ClasseTokenPascal.DIVIDIDO) {
            token = lexico.getNextToken();
            fator();
        }
    }

    // <fator> ::= id | intnum | ( <expressao> ) | id <argumentos>
    // As duas alternativas com "id" se resumem a "id <argumentos>", ja que
    // um "id" isolado e o caso <argumentos> ::= ε.
    private void fator() {
        switch (token.getClasse()) {
            case IDENTIFICADOR:
                token = lexico.getNextToken();
                argumentos();
                break;
            case INTEIRO:
                token = lexico.getNextToken();
                break;
            case ABRE_PARENTESES:
                token = lexico.getNextToken();
                expressao();
                expect(ClasseTokenPascal.FECHA_PARENTESES);
                break;
            default:
                erro("IDENTIFICADOR, INTEIRO ou '('");
        }
    }

    // ---------------------------------------------------------------
    // Infraestrutura do parser
    // ---------------------------------------------------------------

    private void expect(ClasseTokenPascal esperado) {
        if (token.getClasse() != esperado) {
            erro(esperado.toString());
        }
        token = lexico.getNextToken();
    }

    private void erro(String esperado) {
        throw new ErroSintaticoException(
                "Erro sintatico na linha " + token.getLinha() + ", coluna " + token.getColuna() +
                ": esperado " + esperado + ", encontrado " + token.getClasse() +
                " ('" + lexemaAtual() + "')");
    }

    private String lexemaAtual() {
        if (token.getValor() == null) {
            return "";
        }
        if (token.getValor().getTexto() != null) {
            return token.getValor().getTexto();
        }
        if (token.getValor().getNumero() != null) {
            return String.valueOf(token.getValor().getNumero());
        }
        return "";
    }

}
