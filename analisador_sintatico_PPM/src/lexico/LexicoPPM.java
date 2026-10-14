package lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class LexicoPPM {

    private String nomeArquivo;
    private BufferedReader br;
    private char caractere;
    private int linha;
    private int coluna;
    private List<String> palavrasReservadas;

    public LexicoPPM(String nomeArquivo) {
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
        palavrasReservadas = Arrays.asList("p3");
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
                        token.setClasse(ClasseTokenPPM.PalavraReservada);
                    } else {
                        System.err.println("Erro Lexico. Caractere Invalido.");
                        System.exit(1);
                    }
                    token.setValor(new ValorToken(lexema.toString()));
                    return token;
                } else if (Character.isDigit(caractere)) {
                    while (Character.isDigit(caractere)) {
                        lexema.append(caractere);
                        caractere = (char) br.read();
                        coluna++;
                    }
                    token.setClasse(ClasseTokenPPM.NumeroInteiro);
                    token.setValor(new ValorToken(Integer.parseInt(lexema.toString())));
                    return token;
                } else if (caractere == ' ' || caractere == '\t') {
                    caractere = (char) br.read();
                    coluna++;
                } else if (caractere == '\n') {
                    linha++;
                    coluna = 1;
                    caractere = (char) br.read();
                } else if (caractere == '#') {
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
                        token.setClasse(ClasseTokenPPM.EOF);
                        return token;
                    }
                } else {
                    System.err.println("Erro Lexico. Caractere Invalido.");
                    System.exit(1);
                }
            }
            token = new Token(linha, coluna);
            token.setClasse(ClasseTokenPPM.EOF);
            return token;
        } catch (

        IOException e) {
            System.err.println("Não foi possível ler do arquivo: " + nomeArquivo);
        }
        return null;
    }

}
