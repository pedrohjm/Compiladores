import lexico.ClasseToken;
import lexico.Lexico;
import lexico.Token;
import sintatico.Sintatico;

public class App {
    public static void main(String[] args) throws Exception {
        
        Lexico l = new Lexico("teste.pas");
        // Token t;

        // do {
        //     t = l.getNextToken();
        //     System.out.println(t);
        // } while (t.getClasse() != ClasseToken.EOF);

        Sintatico s = new Sintatico(l);
        s.analisar();

    }
}
