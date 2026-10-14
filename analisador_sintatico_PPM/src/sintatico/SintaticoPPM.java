package sintatico;

import lexico.ClasseTokenPPM;
import lexico.LexicoPPM;
import lexico.Token;

public class SintaticoPPM {

    private final LexicoPPM lexico;
    private Token token;

    public SintaticoPPM(LexicoPPM lexico) {
        this.lexico = lexico;
    }

    public void analisar() {
        try {
            token = lexico.getNextToken();
            imagemPpm();
            System.out.println("Analise sintatica concluida sem erros.");
        } catch (ErroSintaticoException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    // <imagem_ppm> ::= <cabecalho> <lista_pixels> EOF
    private void imagemPpm() {
        cabecalho();
        listaPixels();
        expect(ClasseTokenPPM.EOF);
    }

    // <cabecalho> ::= MAGIC NUMERO NUMERO NUMERO
    // MAGIC e NUMERO, no lexer desta fase, sao ClasseTokenPPM.PalavraReservada
    // (o texto "P3") e ClasseTokenPPM.NumeroInteiro, respectivamente.
    private void cabecalho() {
        expect(ClasseTokenPPM.PalavraReservada);
        expect(ClasseTokenPPM.NumeroInteiro);
        expect(ClasseTokenPPM.NumeroInteiro);
        expect(ClasseTokenPPM.NumeroInteiro);
    }

    // <lista_pixels> ::= <pixel> <mais_pixels>
    private void listaPixels() {
        pixel();
        maisPixels();
    }

    // <mais_pixels> ::= <pixel> <mais_pixels> | ε
    // Escrito como laco (nao recursao de verdade) porque uma imagem PPM real
    // pode ter centenas de milhares de pixels, o que estouraria a pilha numa
    // descida recursiva literal.
    private void maisPixels() {
        while (token.getClasse() == ClasseTokenPPM.NumeroInteiro) {
            pixel();
        }
    }

    // <pixel> ::= NUMERO NUMERO NUMERO
    private void pixel() {
        expect(ClasseTokenPPM.NumeroInteiro);
        expect(ClasseTokenPPM.NumeroInteiro);
        expect(ClasseTokenPPM.NumeroInteiro);
    }

    private void expect(ClasseTokenPPM esperado) {
        if (token.getClasse() != esperado) {
            erro(esperado);
        }
        token = lexico.getNextToken();
    }

    private void erro(ClasseTokenPPM esperado) {
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
