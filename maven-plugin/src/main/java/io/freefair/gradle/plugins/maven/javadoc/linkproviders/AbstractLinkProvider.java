package io.freefair.gradle.plugins.maven.javadoc.linkproviders;

import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider;
import io.freefair.gradle.plugins.maven.version.ParsingHelper;
import io.freefair.gradle.plugins.maven.version.Version;
import io.freefair.gradle.plugins.maven.version.matchers.Matcher;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public abstract class AbstractLinkProvider implements JavadocLinkProvider {
    private final Map<ArtifactKey, String> artifactLinkMap = new HashMap<>();
    private static final Pattern REPLACEMENT_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9]+)(?::([0-9]+),([0-9]+))?}");

    protected void addArtifactLink(String group, String artifactId, String versionSyntax, String link) {
        Matcher<Version> matcher = null;
        if(versionSyntax != null && !versionSyntax.isEmpty()) {
            matcher = ParsingHelper.parseVersionSyntax(versionSyntax);
        }
        artifactLinkMap.put(new ArtifactKey(group, artifactId, matcher), link);
    }

    protected boolean additionalStartChecks(String group, String artifact, Version version) {
        return true;
    }

    public String getJavadocLink(String group, String artifact, Version version) {
        if (!additionalStartChecks(group, artifact, version)) {
            return null;
        }
        Optional<ArtifactKey> first = artifactLinkMap.keySet().stream().filter(key ->
                (stringMatches(key.getGroupId(), group))
                        && (stringMatches(key.getArtifactId(), artifact))
                        && (key.getVersionMatcher() == null || key.getVersionMatcher().matches(version)))
                .findFirst();
        if(first.isPresent()) {
            String s = artifactLinkMap.get(first.get());
            return replacePlaceholders(s, group, artifact, version);
        }
        return null;
    }

    private String replacePlaceholders(String s, String group, String artifact, Version version) {
        s = replacePlaceholder(s, "group", group);
        s = replacePlaceholder(s, "artifact", artifact);
        s = replacePlaceholder(s, "version", version.toString());
        return s;
    }

    private String replacePlaceholder(String s, String placeholderName, String replacementValue) {
        java.util.regex.Matcher matcher = REPLACEMENT_PATTERN.matcher(s);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String from = matcher.group(2);
            String to = matcher.group(3);
            if(from != null && to != null && !from.isEmpty() && !to.isEmpty()) {
                int fromInt = Integer.parseInt(from);
                int toInt = Integer.parseInt(to);
                replacementValue = replacementValue.substring(fromInt, toInt);
            }
            if(placeholder.equals(placeholderName)) {
                s = s.replace(matcher.group(0), replacementValue);
            }
        }
        return s;
    }

    private boolean stringMatches(String key, String value) {
        if(key == null) {
            return true;
        }
        if(key.startsWith("*") && key.endsWith("*")) {
            return value.contains(key.substring(1, key.length() - 1));
        } else if (key.startsWith("*")) {
            return value.endsWith(key.substring(1));
        } else if (key.endsWith("*")) {
            return value.startsWith(key.substring(0, key.length() - 1));
        }
        return Objects.equals(key, value);
    }

    @Getter
    private static class ArtifactKey {
        private final String groupId;
        private final String artifactId;
        private final Matcher<Version> versionMatcher;

        private ArtifactKey(String groupId, String artifactId, Matcher<Version> versionMatcher) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.versionMatcher = versionMatcher;
        }
    }
}
