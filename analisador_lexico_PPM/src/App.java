import lexico.ClasseTokenPPM;
import lexico.LexicoPPM;
import lexico.Token;

public class App {
    public static void main(String[] args) throws Exception {
        
        LexicoPPM l = new LexicoPPM("java-logo.ppm");
        Token t;

        int cont = 0;
        do {
            t = l.getNextToken();
            System.out.println(t);
            cont++;
            if (cont == 10) break;
        } while (t.getClasse() != ClasseTokenPPM.EOF);

    }
}
