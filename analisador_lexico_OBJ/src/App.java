import lexico.ClasseTokenOBJ;
import lexico.LexicoOBJ;
import lexico.Token;

public class App {
    public static void main(String[] args) throws Exception {

        LexicoOBJ l = new LexicoOBJ("cube-tex.obj");
        Token t;

        do {
            t = l.getNextToken();
            System.out.println(t);
        } while (t.getClasse() != ClasseTokenOBJ.EOF);

    }
}
