package lexico;

public class ValorToken {

    private String texto;
    private Integer numero;
    private Double decimal;

    public ValorToken(String texto) {
        this.texto = texto;
    }

    public ValorToken(Integer numero) {
        this.numero = numero;
    }

    public ValorToken(Double decimal) {
        this.decimal = decimal;
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

    public Double getDecimal() {
        return decimal;
    }

    public void setDecimal(Double decimal) {
        this.decimal = decimal;
    }

    @Override
    public String toString() {
        return ((texto != null) ? "texto=" + texto : "") +
               ((numero != null) ? "numero=" + numero : "") +
               ((decimal != null) ? "decimal=" + decimal : "");
    }

}
