package io.freefair.gradle.plugins.maven.version;

import io.freefair.gradle.plugins.maven.version.matchers.*;
import org.antlr.v4.runtime.tree.*;

import java.util.stream.Collectors;

public class VersionSyntaxVisitor implements ParseTreeVisitor<Matcher<Version>> {

    @Override
    public Matcher<Version> visit(ParseTree tree) {
        if (tree instanceof RuleNode) {
            if (tree instanceof VersionSyntaxParser.RangeContext) {
                return visit((VersionSyntaxParser.RangeContext) tree);
            }
        }
        return null;
    }

    public Matcher<Version> visit(VersionSyntaxParser.RangeContext range) {
        return new HyphenMatcher(visit(range.parts(0)), visit(range.parts(1)));
    }

    public ComparingMatcher<Version> visit(VersionSyntaxParser.PartsContext parts) {
        return new PartsMatcher(parts.xr().stream().map(this::visit).collect(Collectors.toList()), visit(parts.qualifier()));
    }

    public ComparingMatcher<Version> visit(VersionSyntaxParser.XrContext xr) {
        if(xr.part() != null) {
            return visit(xr.part());
        } else {
            return new AllMatcher();
        }
    }

    public ComparingMatcher<Version> visit(VersionSyntaxParser.QualifierContext qualifier) {
        if(qualifier != null && qualifier.part() != null) {
            return new QualifierMatcher(qualifier.part().stream().map(this::visit).collect(Collectors.toList()));
        }
        return null;
    }

    public ComparingMatcher<Version> visit(VersionSyntaxParser.PartContext part) {
        return new NumberMatcher(part.getText());
    }

    @Override
    public Matcher<Version> visitChildren(RuleNode node) {
        if (node instanceof VersionSyntaxParser.Version_syntaxContext) {
            VersionSyntaxParser.Version_syntaxContext n = (VersionSyntaxParser.Version_syntaxContext) node;
            if(n.range() != null) {
                return visit(n.range());
            } else {
                String operator = null;
                if(n.operator() != null) {
                    operator = n.operator().getText();
                }
                return new OperatorMatcher(operator, visit(n.parts()));
            }
        }
        return null;
    }

    @Override
    public Matcher<Version> visitTerminal(TerminalNode node) {
        return null;
    }

    @Override
    public Matcher<Version> visitErrorNode(ErrorNode node) {
        return null;
    }
}
