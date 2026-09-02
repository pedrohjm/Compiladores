import lexico.ClasseTokenPPM;
import lexico.LexicoPPM;
import lexico.Token;

import java.nio.file.Path;

public class App {
    public static void main(String[] args) throws Exception {

        LexicoPPM l = new LexicoPPM(caminhoAmostra("java-logo.ppm"));
        Token t;

        int cont = 0;
        do {
            t = l.getNextToken();
            System.out.println(t);
            cont++;
            if (cont == 10) break;
        } while (t.getClasse() != ClasseTokenPPM.EOF);

    }

    private static String caminhoAmostra(String nomeArquivo) throws Exception {
        Path classDir = Path.of(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return classDir.getParent().resolve(nomeArquivo).toString();
    }
}
