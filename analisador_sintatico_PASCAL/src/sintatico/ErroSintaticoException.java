package sintatico;

// Excecao interna do parser: interrompe a descida recursiva assim que o
// primeiro erro sintatico e encontrado, em vez de propagar erros em cascata.
// Capturada uma unica vez em SintaticoPascal.analisar().
class ErroSintaticoException extends RuntimeException {

    ErroSintaticoException(String mensagem) {
        super(mensagem);
    }

}
