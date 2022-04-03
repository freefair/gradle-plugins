package io.freefair.gradle.plugins.maven.version;

import lombok.Getter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Version {
    @Getter
    private List<VersionPart> parts;

    public Version(String value) {
        parse(value);
    }

    public Version(List<VersionPart> parts) {
        this.parts = parts;
    }

    private void parse(String value) {
        List<VersionPart> result = new ArrayList<>();
        SemVerSyntaxParser parser = new SemVerSyntaxParser(new CommonTokenStream(new SemVerSyntaxLexer(CharStreams.fromString(value))));
        SemVerSyntaxParser.Sem_ver_syntaxContext sem_ver_syntaxContext = parser.sem_ver_syntax();
        VersionPart firstPart = new VersionPart("", sem_ver_syntaxContext.parts().part(0).getText());
        result.add(firstPart);
        List<VersionPart> collect = sem_ver_syntaxContext.parts().part().stream().skip(1).map(part -> new VersionPart(".", part.getText())).collect(Collectors.toList());
        result.addAll(collect);
        SemVerSyntaxParser.QualifierContext qualifier = sem_ver_syntaxContext.parts().qualifier();
        if (qualifier != null) {
            VersionPart firstPartOfQualifier = new VersionPart("-", qualifier.part(0).getText());
            result.add(firstPartOfQualifier);
            List<VersionPart> collect1 = qualifier.part().stream().skip(1).map(part -> new VersionPart(".", part.getText())).collect(Collectors.toList());
            result.addAll(collect1);
        }
        this.parts = result;
    }

    @Override
    public String toString() {
        return parts.stream().map(VersionPart::toString).collect(Collectors.joining());
    }
}