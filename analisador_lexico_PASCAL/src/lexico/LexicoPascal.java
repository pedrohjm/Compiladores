package lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LexicoPascal {

    private String nomeArquivo;
    private BufferedReader br;
    private char caractere;
    private int linha;
    private int coluna;
    private Map<String, ClasseTokenPascal> palavrasReservadas;

    public LexicoPascal(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
        try {
            br = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            caractere = (char) br.read();
        } catch (IOException ex) {
            System.out.println("Erro abrindo o arquivo " + nomeArquivo);
            System.out.println("Caminho do arquivo: " + caminhoArquivo);
        }
        linha = 1;
        coluna = 1;

        // Palavras-chave do Pascal sao insensiveis a maiusculas/minusculas.
        palavrasReservadas = new HashMap<>();
        palavrasReservadas.put("program", ClasseTokenPascal.KW_PROGRAM);
        palavrasReservadas.put("var", ClasseTokenPascal.KW_VAR);
        palavrasReservadas.put("integer", ClasseTokenPascal.KW_INTEGER);
        palavrasReservadas.put("procedure", ClasseTokenPascal.KW_PROCEDURE);
        palavrasReservadas.put("function", ClasseTokenPascal.KW_FUNCTION);
        palavrasReservadas.put("begin", ClasseTokenPascal.KW_BEGIN);
        palavrasReservadas.put("end", ClasseTokenPascal.KW_END);
        palavrasReservadas.put("read", ClasseTokenPascal.KW_READ);
        palavrasReservadas.put("write", ClasseTokenPascal.KW_WRITE);
        palavrasReservadas.put("writeln", ClasseTokenPascal.KW_WRITELN);
        palavrasReservadas.put("for", ClasseTokenPascal.KW_FOR);
        palavrasReservadas.put("to", ClasseTokenPascal.KW_TO);
        palavrasReservadas.put("do", ClasseTokenPascal.KW_DO);
        palavrasReservadas.put("repeat", ClasseTokenPascal.KW_REPEAT);
        palavrasReservadas.put("until", ClasseTokenPascal.KW_UNTIL);
        palavrasReservadas.put("while", ClasseTokenPascal.KW_WHILE);
        palavrasReservadas.put("if", ClasseTokenPascal.KW_IF);
        palavrasReservadas.put("then", ClasseTokenPascal.KW_THEN);
        palavrasReservadas.put("else", ClasseTokenPascal.KW_ELSE);
        palavrasReservadas.put("or", ClasseTokenPascal.KW_OR);
        palavrasReservadas.put("and", ClasseTokenPascal.KW_AND);
        palavrasReservadas.put("not", ClasseTokenPascal.KW_NOT);
        palavrasReservadas.put("true", ClasseTokenPascal.KW_TRUE);
        palavrasReservadas.put("false", ClasseTokenPascal.KW_FALSE);
    }

    public Token getNextToken() {
        try {
            while (caractere != 65535) { // EOF
                int linhaToken = linha;
                int colunaToken = coluna;

                if (caractere == ' ' || caractere == '\t' || caractere == '\r' || caractere == '\n') {
                    avancar();
                } else if (caractere == '{') {
                    pularComentarioChaves(linhaToken, colunaToken);
                } else if (caractere == '(') {
                    avancar();
                    if (caractere == '*') {
                        pularComentarioParenteses(linhaToken, colunaToken);
                    } else {
                        Token token = new Token(linhaToken, colunaToken);
                        token.setClasse(ClasseTokenPascal.ABRE_PARENTESES);
                        token.setValor(new ValorToken("("));
                        return token;
                    }
                } else if (Character.isLetter(caractere)) {
                    return lerPalavra(linhaToken, colunaToken);
                } else if (Character.isDigit(caractere)) {
                    return lerNumero(linhaToken, colunaToken);
                } else if (caractere == '\'') {
                    return lerCadeia(linhaToken, colunaToken);
                } else {
                    return lerSimbolo(linhaToken, colunaToken);
                }
            }
            Token token = new Token(linha, coluna);
            token.setClasse(ClasseTokenPascal.EOF);
            return token;
        } catch (IOException e) {
            System.out.println("Não foi possível ler do arquivo: " + nomeArquivo);
        }
        return null;
    }

    // Le o proximo caractere atualizando linha/coluna, considerando que a
    // posicao avanca a partir do caractere que esta sendo deixado para tras
    // (necessario porque comentarios em Pascal podem ocupar varias linhas).
    private void avancar() throws IOException {
        if (caractere == '\n') {
            linha++;
            coluna = 1;
        } else {
            coluna++;
        }
        caractere = (char) br.read();
    }

    private Token lerPalavra(int linha, int coluna) throws IOException {
        StringBuilder lexema = new StringBuilder();
        while (Character.isLetterOrDigit(caractere)) {
            lexema.append(caractere);
            avancar();
        }
        String texto = lexema.toString();
        ClasseTokenPascal classe = palavrasReservadas.getOrDefault(texto.toLowerCase(), ClasseTokenPascal.IDENTIFICADOR);
        Token token = new Token(linha, coluna);
        token.setClasse(classe);
        token.setValor(new ValorToken(texto));
        return token;
    }

    private Token lerNumero(int linha, int coluna) throws IOException {
        StringBuilder lexema = new StringBuilder();
        while (Character.isDigit(caractere)) {
            lexema.append(caractere);
            avancar();
        }
        Token token = new Token(linha, coluna);
        try {
            token.setClasse(ClasseTokenPascal.INTEIRO);
            token.setValor(new ValorToken(Integer.parseInt(lexema.toString())));
        } catch (NumberFormatException e) {
            erroLexico("Numero mal formado '" + lexema + "'", linha, coluna);
        }
        return token;
    }

    private Token lerCadeia(int linha, int coluna) throws IOException {
        StringBuilder texto = new StringBuilder();
        avancar(); // consome a aspa de abertura
        while (true) {
            if (caractere == 65535 || caractere == '\n') {
                erroLexico("Cadeia de caracteres nao terminada", linha, coluna);
            }
            if (caractere == '\'') {
                avancar();
                if (caractere == '\'') { // '' dentro da cadeia = aspa literal
                    texto.append('\'');
                    avancar();
                    continue;
                }
                Token token = new Token(linha, coluna);
                token.setClasse(ClasseTokenPascal.CADEIA);
                token.setValor(new ValorToken(texto.toString()));
                return token;
            }
            texto.append(caractere);
            avancar();
        }
    }

    private void pularComentarioChaves(int linhaInicio, int colunaInicio) throws IOException {
        avancar(); // consome '{'
        while (caractere != '}' && caractere != 65535) {
            avancar();
        }
        if (caractere == 65535) {
            erroLexico("Comentario nao terminado", linhaInicio, colunaInicio);
        }
        avancar(); // consome '}'
    }

    private void pularComentarioParenteses(int linhaInicio, int colunaInicio) throws IOException {
        avancar(); // consome o '*' que abriu o comentario
        while (true) {
            if (caractere == 65535) {
                erroLexico("Comentario nao terminado", linhaInicio, colunaInicio);
            }
            if (caractere == '*') {
                avancar();
                if (caractere == ')') {
                    avancar();
                    return;
                }
            } else {
                avancar();
            }
        }
    }

    private Token lerSimbolo(int linha, int coluna) throws IOException {
        char atual = caractere;
        avancar();
        Token token = new Token(linha, coluna);

        if (atual == ':' && caractere == '=') {
            avancar();
            token.setClasse(ClasseTokenPascal.ATRIBUICAO);
            token.setValor(new ValorToken(":="));
            return token;
        }
        if (atual == '<' && caractere == '=') {
            avancar();
            token.setClasse(ClasseTokenPascal.MENOR_IGUAL);
            token.setValor(new ValorToken("<="));
            return token;
        }
        if (atual == '<' && caractere == '>') {
            avancar();
            token.setClasse(ClasseTokenPascal.DIFERENTE);
            token.setValor(new ValorToken("<>"));
            return token;
        }
        if (atual == '>' && caractere == '=') {
            avancar();
            token.setClasse(ClasseTokenPascal.MAIOR_IGUAL);
            token.setValor(new ValorToken(">="));
            return token;
        }

        ClasseTokenPascal classe = classificarSimboloSimples(atual);
        if (classe == null) {
            erroLexico("Caractere invalido '" + atual + "'", linha, coluna);
        }
        token.setClasse(classe);
        token.setValor(new ValorToken(String.valueOf(atual)));
        return token;
    }

    private ClasseTokenPascal classificarSimboloSimples(char c) {
        switch (c) {
            case '=':
                return ClasseTokenPascal.IGUAL;
            case '<':
                return ClasseTokenPascal.MENOR;
            case '>':
                return ClasseTokenPascal.MAIOR;
            case '+':
                return ClasseTokenPascal.MAIS;
            case '-':
                return ClasseTokenPascal.MENOS;
            case '*':
                return ClasseTokenPascal.VEZES;
            case '/':
                return ClasseTokenPascal.DIVIDIDO;
            case ';':
                return ClasseTokenPascal.PONTO_VIRGULA;
            case ',':
                return ClasseTokenPascal.VIRGULA;
            case ':':
                return ClasseTokenPascal.DOIS_PONTOS;
            case '.':
                return ClasseTokenPascal.PONTO;
            case ')':
                return ClasseTokenPascal.FECHA_PARENTESES;
            default:
                return null;
        }
    }

    private void erroLexico(String mensagem, int linha, int coluna) {
        System.err.println("Erro Lexico. " + mensagem + " na linha " + linha + ", coluna " + coluna + ".");
        System.exit(1);
    }

}
