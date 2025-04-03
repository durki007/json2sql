package pl.pwr.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import src.main.antlr.JSONLexer;
import src.main.antlr.JSONParser;


public class QueryTranslator {
    public String translate(String input) {
        CharStream charStream = CharStreams.fromString(input);
        JSONLexer lexer = new JSONLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JSONParser parser = new JSONParser(tokens);

        ParseTree tree = parser.json();
        Json2SqlVisitor visitor = new Json2SqlVisitor();
        return visitor.visit(tree);
    }
}
