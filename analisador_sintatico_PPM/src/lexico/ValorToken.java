package lexico;

public class ValorToken {

    private String texto;
    private Integer numero;
    
    public ValorToken(String texto) {
        this.texto = texto;
    }

    public ValorToken(Integer numero) {
        this.numero = numero;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    @Override
    public String toString() {
        return ((texto != null) ? "texto=" + texto : "") + 
               ((numero != null) ? "numero=" + numero : "");
    }

}
