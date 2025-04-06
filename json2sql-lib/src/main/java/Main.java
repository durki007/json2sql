import pl.pwr.parser.QueryTranslator;

public class Main {
    public static void main(String[] args) {
        QueryTranslator translator = new QueryTranslator();
        var query = """
{
  "queryType": "SELECT",
  "table": "user_entity",
  "columns": ["id", "first_name"],
  "orderBy": null
}
""";
        var translated = translator.translate(query);
        System.out.println(translated);
    }
}
