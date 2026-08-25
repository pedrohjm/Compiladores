package lexico;

public class Token {

    private int linha;
    private int coluna;
    private ClasseTokenOBJ classe;
    private ValorToken valor;

    public Token(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public int getColuna() {
        return coluna;
    }

    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    public ClasseTokenOBJ getClasse() {
        return classe;
    }

    public void setClasse(ClasseTokenOBJ classe) {
        this.classe = classe;
    }

    public ValorToken getValor() {
        return valor;
    }

    public void setValor(ValorToken valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Token [" + linha + ", " + coluna + ", classe=" + classe + ((valor != null) ? ", valor=" + valor : "") + "]";
    }

}
