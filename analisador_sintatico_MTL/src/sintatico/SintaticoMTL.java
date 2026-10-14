package sintatico;

import lexico.ClasseTokenMTL;
import lexico.LexicoMTL;
import lexico.Token;

public class SintaticoMTL {

    private final LexicoMTL lexico;
    private Token token;

    public SintaticoMTL(LexicoMTL lexico) {
        this.lexico = lexico;
    }

    public void analisar() {
        try {
            token = lexico.getNextToken();
            arquivoMtl();
            System.out.println("Analise sintatica concluida sem erros.");
        } catch (ErroSintaticoException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    // <arquivo_mtl> ::= <materiais> EOF
    private void arquivoMtl() {
        materiais();
        expect(ClasseTokenMTL.EOF);
    }

    // <materiais> ::= <material> <materiais> | ε
    private void materiais() {
        while (token.getClasse() == ClasseTokenMTL.KW_NEWMTL) {
            material();
        }
    }

    // <material> ::= KW_NEWMTL IDENTIFICADOR <propriedades>
    private void material() {
        expect(ClasseTokenMTL.KW_NEWMTL);
        expect(ClasseTokenMTL.IDENTIFICADOR);
        propriedades();
    }

    // <propriedades> ::= <propriedade> <propriedades> | ε
    private void propriedades() {
        while (ehInicioDePropriedade()) {
            propriedade();
        }
    }

    private boolean ehInicioDePropriedade() {
        switch (token.getClasse()) {
            case KW_KA:
            case KW_KD:
            case KW_KS:
            case KW_NS:
            case KW_ILLUM:
            case KW_MAP_KD:
                return true;
            default:
                return false;
        }
    }

    // <propriedade> ::= <cor_amb> | <cor_difusa> | <cor_spec> | <exp_spec> | <mod_ilum> | <mapa_textura>
    private void propriedade() {
        switch (token.getClasse()) {
            case KW_KA:
                corAmb();
                break;
            case KW_KD:
                corDifusa();
                break;
            case KW_KS:
                corSpec();
                break;
            case KW_NS:
                expSpec();
                break;
            case KW_ILLUM:
                modIlum();
                break;
            case KW_MAP_KD:
                mapaTextura();
                break;
            default:
                erro(ClasseTokenMTL.KW_KA); // inalcancavel: ehInicioDePropriedade ja filtrou
        }
    }

    // <cor_amb> ::= KW_KA FLOAT FLOAT FLOAT
    // Decisao FLOAT vs INTEIRO (CLAUDE-sintatico.md): onde a gramatica pede
    // FLOAT, um inteiro como "Kd 1 0 0" ou "Ns 10" e erro sintatico -- mesma
    // escolha do projeto de referencia EC-compiladores.
    private void corAmb() {
        expect(ClasseTokenMTL.KW_KA);
        expect(ClasseTokenMTL.FLOAT);
        expect(ClasseTokenMTL.FLOAT);
        expect(ClasseTokenMTL.FLOAT);
    }

    // <cor_difusa> ::= KW_KD FLOAT FLOAT FLOAT
    private void corDifusa() {
        expect(ClasseTokenMTL.KW_KD);
        expect(ClasseTokenMTL.FLOAT);
        expect(ClasseTokenMTL.FLOAT);
        expect(ClasseTokenMTL.FLOAT);
    }

    // <cor_spec> ::= KW_KS FLOAT FLOAT FLOAT
    private void corSpec() {
        expect(ClasseTokenMTL.KW_KS);
        expect(ClasseTokenMTL.FLOAT);
        expect(ClasseTokenMTL.FLOAT);
        expect(ClasseTokenMTL.FLOAT);
    }

    // <exp_spec> ::= KW_NS FLOAT
    private void expSpec() {
        expect(ClasseTokenMTL.KW_NS);
        expect(ClasseTokenMTL.FLOAT);
    }

    // <mod_ilum> ::= KW_ILLUM INTEIRO
    private void modIlum() {
        expect(ClasseTokenMTL.KW_ILLUM);
        expect(ClasseTokenMTL.INTEIRO);
    }

    // <mapa_textura> ::= KW_MAP_KD IDENTIFICADOR
    private void mapaTextura() {
        expect(ClasseTokenMTL.KW_MAP_KD);
        expect(ClasseTokenMTL.IDENTIFICADOR);
    }

    private void expect(ClasseTokenMTL esperado) {
        if (token.getClasse() != esperado) {
            erro(esperado);
        }
        token = lexico.getNextToken();
    }

    private void erro(ClasseTokenMTL esperado) {
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
        if (token.getValor().getDecimal() != null) {
            return String.valueOf(token.getValor().getDecimal());
        }
        return "";
    }

}
