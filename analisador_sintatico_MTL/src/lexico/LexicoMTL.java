package lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class LexicoMTL {

    private String nomeArquivo;
    private BufferedReader br;
    private char caractere;
    private int linha;
    private int coluna;
    private List<String> palavrasReservadas;

    public LexicoMTL(String nomeArquivo) {
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
        // Palavras-chave sensiveis a maiusculas/minusculas: comparar exatamente.
        palavrasReservadas = Arrays.asList("newmtl", "Ka", "Kd", "Ks", "Ns", "illum", "map_Kd");
    }

    public Token getNextToken() {
        StringBuilder lexema;
        Token token;

        try {
            while (caractere != 65535) { // EOF
                lexema = new StringBuilder();
                token = new Token(linha, coluna);

                if (Character.isLetter(caractere) || caractere == '_') {
                    while (Character.isLetterOrDigit(caractere) || caractere == '_' || caractere == '-' || caractere == '.') {
                        lexema.append(caractere);
                        caractere = (char) br.read();
                        coluna++;
                    }
                    String texto = lexema.toString();
                    if (palavrasReservadas.contains(texto)) {
                        token.setClasse(classificarPalavraReservada(texto));
                    } else {
                        token.setClasse(ClasseTokenMTL.IDENTIFICADOR);
                    }
                    token.setValor(new ValorToken(texto));
                    return token;
                } else if (Character.isDigit(caractere) || caractere == '.') {
                    return lerNumero(lexema, token);
                } else if (caractere == ' ' || caractere == '\t' || caractere == '\r') {
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
                        token.setClasse(ClasseTokenMTL.EOF);
                        return token;
                    }
                } else {
                    System.err.println("Erro Lexico. Caractere invalido '" + caractere + "' na linha " + linha + ", coluna " + coluna + ".");
                    System.exit(1);
                }
            }
            token = new Token(linha, coluna);
            token.setClasse(ClasseTokenMTL.EOF);
            return token;
        } catch (IOException e) {
            System.out.println("Não foi possível ler do arquivo: " + nomeArquivo);
        }
        return null;
    }

    private ClasseTokenMTL classificarPalavraReservada(String texto) {
        switch (texto) {
            case "newmtl":
                return ClasseTokenMTL.KW_NEWMTL;
            case "Ka":
                return ClasseTokenMTL.KW_KA;
            case "Kd":
                return ClasseTokenMTL.KW_KD;
            case "Ks":
                return ClasseTokenMTL.KW_KS;
            case "Ns":
                return ClasseTokenMTL.KW_NS;
            case "illum":
                return ClasseTokenMTL.KW_ILLUM;
            case "map_Kd":
                return ClasseTokenMTL.KW_MAP_KD;
            default:
                return ClasseTokenMTL.IDENTIFICADOR;
        }
    }

    private Token lerNumero(StringBuilder lexema, Token token) throws IOException {
        boolean isFloat = false;

        while (Character.isDigit(caractere)) {
            lexema.append(caractere);
            caractere = (char) br.read();
            coluna++;
        }

        if (caractere == '.') {
            isFloat = true;
            lexema.append(caractere);
            caractere = (char) br.read();
            coluna++;
            while (Character.isDigit(caractere)) {
                lexema.append(caractere);
                caractere = (char) br.read();
                coluna++;
            }
        }

        try {
            if (isFloat) {
                token.setClasse(ClasseTokenMTL.FLOAT);
                token.setValor(new ValorToken(Double.parseDouble(lexema.toString())));
            } else {
                token.setClasse(ClasseTokenMTL.INTEIRO);
                token.setValor(new ValorToken(Integer.parseInt(lexema.toString())));
            }
        } catch (NumberFormatException e) {
            System.err.println("Erro Lexico. Numero mal formado '" + lexema + "' na linha " + linha + ", coluna " + coluna + ".");
            System.exit(1);
        }

        return token;
    }

}
