package io.freefair.gradle.plugins.github;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lars Grefer
 */
@Getter
@Setter
public abstract class GithubExtension {

    private final static Pattern slugPattern = Pattern.compile("(.*)/(.*)");

    /**
     * The Identifier of the GitHub Repository as {@code owner/repo}
     */
    public abstract Property<String> getSlug();

    /**
     * The username used for auhentication.
     */
    public abstract Property<String> getUsername();

    /**
     * The token used for authentication.
     */
    public abstract Property<String> getToken();

    public abstract Property<String> getTag();

    public abstract Property<Boolean> getTravis();

    public GithubExtension() {
        getTag().convention("HEAD");
        getTravis().convention(false);
    }

    public Provider<String> getOwner() {
        return getSlug().map(sl -> {
            Matcher matcher = slugPattern.matcher(sl);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return null;
        });
    }

    public void setOwner(String owner) {
        getSlug().set(owner + "/" + getRepo().getOrElse(""));
    }

    public Provider<String> getRepo() {
        return getSlug().map(sl -> {
            Matcher matcher = slugPattern.matcher(sl);
            if (matcher.matches()) {
                return matcher.group(2);
            }
            return null;
        });
    }

    public void setRepo(String repo) {
        getSlug().set(getOwner().getOrElse("") + "/" + repo);
    }
}
