package pl.pwr.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import pl.pwr.antlr.QueryLangLexer;
import pl.pwr.antlr.QueryLangParser;

public class QueryTranslator {
    public String translate(String input) {
        CharStream charStream = CharStreams.fromString(input);
        QueryLangLexer lexer = new QueryLangLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        QueryLangParser parser = new QueryLangParser(tokens);

        ParseTree tree = parser.query();
        SqlVisitor visitor = new SqlVisitor();
        return visitor.visit(tree);
    }
}
