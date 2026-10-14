package lexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class LexicoOBJ {

    private String nomeArquivo;
    private BufferedReader br;
    private char caractere;
    private int linha;
    private int coluna;
    private List<String> palavrasReservadas;

    public LexicoOBJ(String nomeArquivo) {
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
        palavrasReservadas = Arrays.asList("mtllib", "usemtl", "v", "vt", "vn", "f", "g", "o");
    }

    public Token getNextToken() {
        StringBuilder lexema;
        Token token;

        try {
            while (caractere != 65535) { // EOF
                lexema = new StringBuilder();
                token = new Token(linha, coluna);

                if (Character.isLetter(caractere) || caractere == '_') {
                    // Maximal munch: le o lexema inteiro (ex: "vt", "vn") antes de classificar,
                    // para nao confundir com o prefixo "v".
                    while (Character.isLetterOrDigit(caractere) || caractere == '_' || caractere == '-' || caractere == '.') {
                        lexema.append(caractere);
                        caractere = (char) br.read();
                        coluna++;
                    }
                    String texto = lexema.toString();
                    if (palavrasReservadas.contains(texto)) {
                        token.setClasse(classificarPalavraReservada(texto));
                    } else {
                        token.setClasse(ClasseTokenOBJ.IDENTIFICADOR);
                    }
                    token.setValor(new ValorToken(texto));
                    return token;
                } else if (Character.isDigit(caractere) || caractere == '.' || caractere == '-') {
                    return lerNumero(lexema, token);
                } else if (caractere == '/') {
                    token.setClasse(ClasseTokenOBJ.BARRA);
                    token.setValor(new ValorToken("/"));
                    caractere = (char) br.read();
                    coluna++;
                    return token;
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
                        token.setClasse(ClasseTokenOBJ.EOF);
                        return token;
                    }
                } else {
                    System.err.println("Erro Lexico. Caractere invalido '" + caractere + "' na linha " + linha + ", coluna " + coluna + ".");
                    System.exit(1);
                }
            }
            token = new Token(linha, coluna);
            token.setClasse(ClasseTokenOBJ.EOF);
            return token;
        } catch (IOException e) {
            System.out.println("Não foi possível ler do arquivo: " + nomeArquivo);
        }
        return null;
    }

    private ClasseTokenOBJ classificarPalavraReservada(String texto) {
        switch (texto) {
            case "mtllib":
                return ClasseTokenOBJ.KW_MTLLIB;
            case "usemtl":
                return ClasseTokenOBJ.KW_USEMTL;
            case "v":
                return ClasseTokenOBJ.KW_V;
            case "vt":
                return ClasseTokenOBJ.KW_VT;
            case "vn":
                return ClasseTokenOBJ.KW_VN;
            case "f":
                return ClasseTokenOBJ.KW_F;
            case "g":
                return ClasseTokenOBJ.KW_G;
            case "o":
                return ClasseTokenOBJ.KW_O;
            default:
                return ClasseTokenOBJ.IDENTIFICADOR;
        }
    }

    private Token lerNumero(StringBuilder lexema, Token token) throws IOException {
        boolean isFloat = false;

        // Sinal negativo so aparece em FLOAT no OBJ (ex: vn -1.0 0.0 0.0).
        if (caractere == '-') {
            lexema.append(caractere);
            caractere = (char) br.read();
            coluna++;
        }

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
                token.setClasse(ClasseTokenOBJ.FLOAT);
                token.setValor(new ValorToken(Double.parseDouble(lexema.toString())));
            } else {
                token.setClasse(ClasseTokenOBJ.INTEIRO);
                token.setValor(new ValorToken(Integer.parseInt(lexema.toString())));
            }
        } catch (NumberFormatException e) {
            System.err.println("Erro Lexico. Numero mal formado '" + lexema + "' na linha " + linha + ", coluna " + coluna + ".");
            System.exit(1);
        }

        return token;
    }

}
