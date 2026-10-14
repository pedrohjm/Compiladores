package lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Lexico {

    private String nomeArquivo;
    private BufferedReader br;
    private char caractere;
    private int linha;
    private int coluna;
    private List<String> palavrasReservadas;

    public Lexico(String nomeArquivo) {
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
        palavrasReservadas = Arrays.asList("program", "begin", "end", "var", "integer", "read",
                "write", "writeln", "for", "to", "do", "repeat", "until", "while", "if", "then",
                "or", "and", "not", "true", "false");
    }

    public Token getNextToken() {
        StringBuilder lexema;
        Token token;

        try {
            while (caractere != 65535) { // EOF
                lexema = new StringBuilder();
                token = new Token(linha, coluna);

                if (Character.isLetter(caractere)) {
                    while (Character.isLetter(caractere) || Character.isDigit(caractere)) {
                        lexema.append(caractere);
                        caractere = (char) br.read();
                        coluna++;
                    }
                    if (palavrasReservadas.contains(lexema.toString().toLowerCase())) {
                        token.setClasse(ClasseToken.PalavraReservada);
                    } else {
                        token.setClasse(ClasseToken.Identificador);
                    }
                    token.setValor(new ValorToken(lexema.toString()));
                    return token;
                } else if (Character.isDigit(caractere)) {
                    while (Character.isDigit(caractere)) {
                        lexema.append(caractere);
                        caractere = (char) br.read();
                        coluna++;
                    }
                    token.setClasse(ClasseToken.NumeroInteiro);
                    token.setValor(new ValorToken(Integer.parseInt(lexema.toString())));
                    return token;
                } else if (caractere == ' ' || caractere == '\t') {
                    caractere = (char) br.read();
                    coluna++;
                } else if (caractere == '\n') {
                    linha++;
                    coluna = 1;
                    caractere = (char) br.read();
                } else if (caractere == '+') {
                    token.setClasse(ClasseToken.Adicao);
                    caractere = (char) br.read();
                    coluna++;
                    return token;
                } else if (caractere == '-') {
                    token.setClasse(ClasseToken.Subtracao);
                    caractere = (char) br.read();
                    coluna++;
                    return token;
                } else if (caractere == '*') {
                    token.setClasse(ClasseToken.Multiplicacao);
                    caractere = (char) br.read();
                    coluna++;
                    return token;
                } else if (caractere == '/') {
                    caractere = (char) br.read();
                    coluna++;
                    if (caractere == '/') { // COMENTARIO
                        caractere = (char) br.read();
                        coluna++;
                        while (caractere != '\n' && caractere != 65535) {
                            caractere = (char) br.read();
                            coluna++;
                        }
                        if (caractere == '\n') {
                            linha++;
                            coluna = 1;
                            caractere = (char) br.read();
                        } else if (caractere == 65535) {
                            token = new Token(linha, coluna);
                            token.setClasse(ClasseToken.EOF);
                            return token;
                        }
                    } else {
                        token.setClasse(ClasseToken.Divisao);
                        return token;
                    }
                } else if (caractere == '(') {
                    token.setClasse(ClasseToken.ParEsq);
                    caractere = (char) br.read();
                    coluna++;
                    return token;
                } else if (caractere == ')') {
                    token.setClasse(ClasseToken.ParDir);
                    caractere = (char) br.read();
                    coluna++;
                    return token;
                } else if (caractere == ';') {
                    token.setClasse(ClasseToken.PontoVirg);
                    caractere = (char) br.read();
                    coluna++;
                    return token;
                } else if (caractere == ',') {
                    token.setClasse(ClasseToken.Virg);
                    caractere = (char) br.read();
                    coluna++;
                    return token;
                } else if (caractere == '.') {
                    token.setClasse(ClasseToken.Ponto);
                    caractere = (char) br.read();
                    coluna++;
                    return token;
                } else if (caractere == ':') {
                    caractere = (char) br.read();
                    coluna++;
                    if (caractere == '=') {
                        caractere = (char) br.read();
                        coluna++;
                        token.setClasse(ClasseToken.Atribuicao);
                    } else {
                        token.setClasse(ClasseToken.DoisPontos);
                    }
                    return token;
                } else if (caractere == '>') {
                    caractere = (char) br.read();
                    coluna++;
                    if (caractere == '=') {
                        caractere = (char) br.read();
                        coluna++;
                        token.setClasse(ClasseToken.MaiorIgual);
                    } else {
                        token.setClasse(ClasseToken.Maior);
                    }
                    return token;
                } else if (caractere == '<') {
                    caractere = (char) br.read();
                    coluna++;
                    if (caractere == '=') {
                        caractere = (char) br.read();
                        coluna++;
                        token.setClasse(ClasseToken.MenorIgual);
                    } else if (caractere == '>') {
                        caractere = (char) br.read();
                        coluna++;
                        token.setClasse(ClasseToken.Diferente);
                    } else {
                        token.setClasse(ClasseToken.Menor);
                    }
                    return token;
                } else if (caractere == '=') {
                    token.setClasse(ClasseToken.Igual);
                    caractere = (char) br.read();
                    coluna++;
                    return token;
                } else if (caractere == '{') {
                    while (caractere != '}') {
                        caractere = (char) br.read();
                        coluna++;
                        if (caractere == '\n') {
                            linha++;
                            coluna = 1;
                            caractere = (char) br.read();
                        } else if (caractere == 65535) {
                            System.err.println("Erro Lexico. " + linha + ", " + coluna
                                    + " Faltou fechar um comentário de bloco { }.");
                            System.exit(1);
                        }
                    }
                    caractere = (char) br.read();
                    coluna++;
                } else if (caractere == '\'') {
                    caractere = (char) br.read();
                    coluna++;
                    while (caractere != '\'') {
                        if (caractere == '\n') {
                            System.err.println("Erro Lexico. " + linha + ", " + coluna
                                    + " Strings devem terminar na mesma linha.");
                            System.exit(1);
                        } else {
                            if (caractere == 65535) {
                                System.err.println("Erro Lexico. " + linha + ", " + coluna
                                        + " Faltou terminar uma String.");
                                System.exit(1);
                            }
                        }
                        lexema.append(caractere);
                        caractere = (char) br.read();
                        coluna++;
                    }
                    caractere = (char) br.read();
                    coluna++;
                    token.setClasse(ClasseToken.CadeiaDeCaracteres);
                    token.setValor(new ValorToken(lexema.toString()));
                    return token;
                } else {
                    System.err.println("Erro Lexico. Caractere Invalido.");
                    System.exit(1);
                }
            }
            token = new Token(linha, coluna);
            token.setClasse(ClasseToken.EOF);
            return token;
        } catch (IOException e) {
            System.err.println("Não foi possível ler do arquivo: " + nomeArquivo);
        }
        return null;
    }

}
