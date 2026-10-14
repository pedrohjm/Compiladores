package sintatico;

import lexico.ClasseToken;
import lexico.Lexico;
import lexico.Token;

public class Sintatico {

    private final Lexico lexico;
    private Token token;

    public Sintatico(Lexico lexico) {
        this.lexico = lexico;
    }

    public void analisar() {
        token = lexico.getNextToken();
        programa();
    }

    // <programa> ::= program id {A01} ; <corpo> . {A45}
    private void programa() {
        if (ehPalavraReservada("program")) {
            token = lexico.getNextToken();
            if (token.getClasse() == ClasseToken.Identificador) {
                token = lexico.getNextToken();
                // {A01}
                if (token.getClasse() == ClasseToken.PontoVirg) {
                    token = lexico.getNextToken();
                    corpo();
                    if (token.getClasse() == ClasseToken.Ponto) {
                        token = lexico.getNextToken();
                        // A{45}
                    } else {
                        erroSintatico("Faltou o ponto final do programa (.)");
                    }
                } else {
                    erroSintatico("Faltou o ponto e virgula (;) depois do identificador do programa");
                }
            } else {
                erroSintatico("Faltou o nome do programa");
            }
        } else {
            erroSintatico("Faltou começar o programa com PROGRAM");
        }
    }

    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    private void corpo() {

    }

    private void erroSintatico(String mensagem) {
        System.err.println("[" + token.getLinha() + ", " +
                           token.getColuna() + 
                           "] - Erro Sintático: " + mensagem);
    }
    
    private boolean ehPalavraReservada(String palavra) {
        return token.getClasse() == ClasseToken.PalavraReservada &&
               token.getValor().getTexto().equalsIgnoreCase(palavra);
    }

}
