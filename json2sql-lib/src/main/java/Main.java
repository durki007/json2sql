import pl.pwr.parser.QueryTranslator;

public class Main {
    public static void main(String[] args) {
        QueryTranslator translator = new QueryTranslator();
        var query = "test";
        var translated = translator.translate(query);
        System.out.println(translated);
    }
}
