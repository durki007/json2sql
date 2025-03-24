package pl.pwr.parser;

import org.antlr.v4.runtime.RuleContext;
import pl.pwr.antlr.QueryLangParser;
import pl.pwr.antlr.QueryLangBaseVisitor;

import java.util.stream.Collectors;

public class SqlVisitor extends QueryLangBaseVisitor<String> {
    private String selectPart;
    private String fromPart;
    private String wherePart;


    @Override
    public String visitQuery(QueryLangParser.QueryContext ctx) {
        visitSelectClause(ctx.selectClause());
        visitFromClause(ctx.fromClause());

        if (ctx.whereClause() != null) {
            visitWhereClause(ctx.whereClause());
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(selectPart).append(" ");
        sql.append("FROM ").append(fromPart).append(" ");

        if (wherePart != null) {
            sql.append("WHERE ").append(wherePart);
        }

        return sql.toString().trim();
    }

    @Override
    public String visitSelectClause(QueryLangParser.SelectClauseContext ctx) {
        selectPart = ctx.columnList().columnName().stream()
                .map(RuleContext::getText)
                .collect(Collectors.joining(", "));
        return null;
    }

    @Override
    public String visitFromClause(QueryLangParser.FromClauseContext ctx) {
        fromPart = ctx.tableName().getText();
        return null;
    }

    @Override
    public String visitWhereClause(QueryLangParser.WhereClauseContext ctx) {
        var cond = ctx.condition();
        wherePart = cond.columnName().getText() + " " +
                cond.operator().getText() + " " +
                cond.value().getText();
        return null;
    }
}
