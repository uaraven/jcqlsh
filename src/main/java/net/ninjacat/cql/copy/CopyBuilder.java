package net.ninjacat.cql.copy;

import net.ninjacat.cql.CqlCopyBaseListener;
import net.ninjacat.cql.CqlCopyParser;
import net.ninjacat.cql.shell.ShellException;
import org.antlr.v4.runtime.RuleContext;

import java.util.AbstractMap;
import java.util.stream.Collectors;

public class CopyBuilder extends CqlCopyBaseListener {

    private final CqlCopyContext.CqlCopyContextBuilder builder;

    public CopyBuilder() {
        this.builder = new CqlCopyContext.CqlCopyContextBuilder();
    }

    public CqlCopyContext getCopyData() {
        return this.builder.build();
    }

    @Override
    public void exitColumn_list(final CqlCopyParser.Column_listContext ctx) {
        this.builder.columnNames(ctx.column_name()
                .stream()
                .map(RuleContext::getText)
                .collect(Collectors.toList()));
        super.exitColumn_list(ctx);
    }

    @Override
    public void exitTable_name(final CqlCopyParser.Table_nameContext ctx) {
        this.builder.tableName(ctx.getText());
        super.exitTable_name(ctx);
    }

    @Override
    public void exitDirection(final CqlCopyParser.DirectionContext ctx) {
        switch (ctx.getText().toLowerCase()) {
            case "from":
                this.builder.direction(CopyDirection.FROM);
                break;
            case "to":
                this.builder.direction(CopyDirection.TO);
                break;
            default:
                throw new ShellException("Invalid COPY syntax, expected FROM/TO");
        }
        super.exitDirection(ctx);
    }

    @Override
    public void exitConsole_io(final CqlCopyParser.Console_ioContext ctx) {
        this.builder.useConsoleForIo(ctx.K_STDIN() != null || ctx.K_STDOUT() != null);
        super.exitConsole_io(ctx);
    }

    @Override
    public void exitFiles(final CqlCopyParser.FilesContext ctx) {
        this.builder.fileName(ctx.file().stream()
                .map(RuleContext::getText)
                .map(CopyBuilder::removeQuotes)
                .collect(Collectors.toList()));
        super.exitFiles(ctx);
    }

    @Override
    public void exitOptions_section(final CqlCopyParser.Options_sectionContext ctx) {
        this.builder.options(ctx.option().stream()
                .map(CopyBuilder::optionToMapEntry)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        super.exitOptions_section(ctx);
    }

    private static AbstractMap.SimpleEntry<CopyOption, String> optionToMapEntry(final CqlCopyParser.OptionContext octx) {
        final CopyOption name = CopyOption.valueOf(octx.option_name().getText().toUpperCase());
        final String value = removeQuotes(octx.option_value().getText());
        return new AbstractMap.SimpleEntry<>(name, value);
    }

    private static String removeQuotes(final String str) {
        if (str.startsWith("'") && str.endsWith("'")) {
            return str.substring(1, str.length() - 1);
        } else {
            return str;
        }
    }
}
