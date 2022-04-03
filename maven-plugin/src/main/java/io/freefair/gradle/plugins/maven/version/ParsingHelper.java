package io.freefair.gradle.plugins.maven.version;

import io.freefair.gradle.plugins.maven.version.matchers.Matcher;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class ParsingHelper {
    public static Matcher<Version> parseVersionSyntax(String version) {
        VersionSyntaxParser parser = new VersionSyntaxParser(new CommonTokenStream(new VersionSyntaxLexer(CharStreams.fromString(version))));
        VersionSyntaxParser.Version_syntaxContext version_syntaxContext = parser.version_syntax();
        return version_syntaxContext.accept(new VersionSyntaxVisitor());
    }

    public static Version parseVersion(String version) {
        return new Version(version);
    }
}
