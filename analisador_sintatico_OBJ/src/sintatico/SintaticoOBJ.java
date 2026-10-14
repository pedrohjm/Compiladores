package sintatico;

import lexico.ClasseTokenOBJ;
import lexico.LexicoOBJ;
import lexico.Token;

public class SintaticoOBJ {

    private final LexicoOBJ lexico;
    private Token token;

    public SintaticoOBJ(LexicoOBJ lexico) {
        this.lexico = lexico;
    }

    public void analisar() {
        try {
            token = lexico.getNextToken();
            arquivoObj();
            System.out.println("Analise sintatica concluida sem erros.");
        } catch (ErroSintaticoException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    // <arquivo_obj> ::= <linhas_obj> EOF
    private void arquivoObj() {
        linhasObj();
        expect(ClasseTokenOBJ.EOF);
    }

    // <linhas_obj> ::= <comando> <linhas_obj> | ε
    private void linhasObj() {
        while (ehInicioDeComando()) {
            comando();
        }
    }

    private boolean ehInicioDeComando() {
        switch (token.getClasse()) {
            case KW_MTLLIB:
            case KW_USEMTL:
            case KW_O:
            case KW_G:
            case KW_V:
            case KW_VT:
            case KW_VN:
            case KW_F:
                return true;
            default:
                return false;
        }
    }

    // <comando> ::= <importa_mtl> | <usa_mtl> | <def_objeto> | <def_grupo>
    //             | <def_vertice> | <def_uv> | <def_normal> | <def_face>
    private void comando() {
        switch (token.getClasse()) {
            case KW_MTLLIB:
                importaMtl();
                break;
            case KW_USEMTL:
                usaMtl();
                break;
            case KW_O:
                defObjeto();
                break;
            case KW_G:
                defGrupo();
                break;
            case KW_V:
                defVertice();
                break;
            case KW_VT:
                defUv();
                break;
            case KW_VN:
                defNormal();
                break;
            case KW_F:
                defFace();
                break;
            default:
                erro(ClasseTokenOBJ.KW_F); // inalcancavel: ehInicioDeComando ja filtrou
        }
    }

    // <importa_mtl> ::= KW_MTLLIB IDENTIFICADOR
    private void importaMtl() {
        expect(ClasseTokenOBJ.KW_MTLLIB);
        expect(ClasseTokenOBJ.IDENTIFICADOR);
    }

    // <usa_mtl> ::= KW_USEMTL IDENTIFICADOR
    private void usaMtl() {
        expect(ClasseTokenOBJ.KW_USEMTL);
        expect(ClasseTokenOBJ.IDENTIFICADOR);
    }

    // <def_objeto> ::= KW_O IDENTIFICADOR
    private void defObjeto() {
        expect(ClasseTokenOBJ.KW_O);
        expect(ClasseTokenOBJ.IDENTIFICADOR);
    }

    // <def_grupo> ::= KW_G IDENTIFICADOR
    private void defGrupo() {
        expect(ClasseTokenOBJ.KW_G);
        expect(ClasseTokenOBJ.IDENTIFICADOR);
    }

    // Decisao FLOAT vs INTEIRO (CLAUDE-sintatico.md): onde a gramatica pede
    // FLOAT, um inteiro como "vt 0 0.75" e erro sintatico -- mesma escolha do
    // projeto de referencia EC-compiladores. Por isso o cube-tex.obj deste
    // projeto usa "0.0" nas coordenadas de textura que eram "0".
    // <def_vertice> ::= KW_V FLOAT FLOAT FLOAT
    private void defVertice() {
        expect(ClasseTokenOBJ.KW_V);
        expect(ClasseTokenOBJ.FLOAT);
        expect(ClasseTokenOBJ.FLOAT);
        expect(ClasseTokenOBJ.FLOAT);
    }

    // <def_uv> ::= KW_VT FLOAT FLOAT
    private void defUv() {
        expect(ClasseTokenOBJ.KW_VT);
        expect(ClasseTokenOBJ.FLOAT);
        expect(ClasseTokenOBJ.FLOAT);
    }

    // <def_normal> ::= KW_VN FLOAT FLOAT FLOAT
    private void defNormal() {
        expect(ClasseTokenOBJ.KW_VN);
        expect(ClasseTokenOBJ.FLOAT);
        expect(ClasseTokenOBJ.FLOAT);
        expect(ClasseTokenOBJ.FLOAT);
    }

    // <def_face> ::= KW_F <conjunto> <conjunto> <conjunto>
    // A gramatica aceita exatamente 3 conjuntos (so triangulos); uma face com
    // 4 indices sobra como token nao consumido e vira erro sintatico mais
    // adiante, ao nao casar com nenhum <comando>.
    private void defFace() {
        expect(ClasseTokenOBJ.KW_F);
        conjunto();
        conjunto();
        conjunto();
    }

    // <conjunto> ::= INTEIRO <complemento_face>
    private void conjunto() {
        expect(ClasseTokenOBJ.INTEIRO);
        complementoFace();
    }

    // <complemento_face> ::= BARRA <dado_textura_ou_normal> | ε
    private void complementoFace() {
        if (token.getClasse() == ClasseTokenOBJ.BARRA) {
            token = lexico.getNextToken();
            dadoTexturaOuNormal();
        }
    }

    // <dado_textura_ou_normal> ::= BARRA INTEIRO | INTEIRO <complemento_normal>
    // A primeira alternativa cobre o caso "v//vn" (barra dupla, sem indice de
    // textura); a segunda cobre "v/vt" e "v/vt/vn".
    private void dadoTexturaOuNormal() {
        if (token.getClasse() == ClasseTokenOBJ.BARRA) {
            token = lexico.getNextToken();
            expect(ClasseTokenOBJ.INTEIRO);
        } else if (token.getClasse() == ClasseTokenOBJ.INTEIRO) {
            token = lexico.getNextToken();
            complementoNormal();
        } else {
            erro(ClasseTokenOBJ.INTEIRO);
        }
    }

    // <complemento_normal> ::= BARRA INTEIRO | ε
    private void complementoNormal() {
        if (token.getClasse() == ClasseTokenOBJ.BARRA) {
            token = lexico.getNextToken();
            expect(ClasseTokenOBJ.INTEIRO);
        }
    }

    private void expect(ClasseTokenOBJ esperado) {
        if (token.getClasse() != esperado) {
            erro(esperado);
        }
        token = lexico.getNextToken();
    }

    private void erro(ClasseTokenOBJ esperado) {
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
