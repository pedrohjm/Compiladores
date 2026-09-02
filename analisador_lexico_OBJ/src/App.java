import lexico.ClasseTokenOBJ;
import lexico.LexicoOBJ;
import lexico.Token;

import java.nio.file.Path;

public class App {
    public static void main(String[] args) throws Exception {

        LexicoOBJ l = new LexicoOBJ(caminhoAmostra("cube-tex.obj"));
        Token t;

        do {
            t = l.getNextToken();
            System.out.println(t);
        } while (t.getClasse() != ClasseTokenOBJ.EOF);

    }

    private static String caminhoAmostra(String nomeArquivo) throws Exception {
        Path binDir = Path.of(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return binDir.getParent().resolve(nomeArquivo).toString();
    }
}
