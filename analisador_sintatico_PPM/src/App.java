import lexico.LexicoPPM;
import sintatico.SintaticoPPM;

import java.nio.file.Path;

public class App {
    public static void main(String[] args) throws Exception {

        LexicoPPM lexico = new LexicoPPM(caminhoAmostra("java-logo.ppm"));
        SintaticoPPM sintatico = new SintaticoPPM(lexico);
        sintatico.analisar();

    }

    // Resolve o arquivo de amostra a partir da pasta onde a classe compilada esta
    // (bin/ ou src/, dependendo de como foi executado), em vez do diretorio de
    // trabalho do processo (que o VS Code/Code Runner as vezes ajustam para "src").
    private static String caminhoAmostra(String nomeArquivo) throws Exception {
        Path classDir = Path.of(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return classDir.getParent().resolve(nomeArquivo).toString();
    }
}
    