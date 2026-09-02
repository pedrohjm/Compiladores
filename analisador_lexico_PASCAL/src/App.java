import lexico.ClasseTokenPascal;
import lexico.LexicoPascal;
import lexico.Token;

import java.nio.file.Path;

public class App {
    public static void main(String[] args) throws Exception {

        LexicoPascal l = new LexicoPascal(caminhoAmostra("fibonacci.pas"));
        Token t;

        do {
            t = l.getNextToken();
            System.out.println(t);
        } while (t.getClasse() != ClasseTokenPascal.EOF);

    }

    // Resolve o arquivo de amostra a partir da pasta "bin", em vez do diretorio de
    // trabalho do processo (que o VS Code as vezes ajusta para "src" ao rodar via F5).
    private static String caminhoAmostra(String nomeArquivo) throws Exception {
        Path binDir = Path.of(App.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return binDir.getParent().resolve(nomeArquivo).toString();
    }
}
