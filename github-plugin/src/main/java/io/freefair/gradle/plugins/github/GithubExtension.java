package io.freefair.gradle.plugins.github;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lars Grefer
 */
@Getter
@Setter
public class GithubExtension {

    private final static Pattern slugPattern = Pattern.compile("(.*)/(.*)");

    /**
     * The Identifier of the GitHub Repository as {@code owner/repo}
     */
    private final Property<String> slug;

    /**
     * The username used for auhentication.
     */
    private final Property<String> username;

    /**
     * The token used for authentication.
     */
    private final Property<String> token;

    private final Property<String> tag;

    private final Property<Boolean> travis;

    @Inject
    public GithubExtension(ObjectFactory objectFactory) {
        slug = objectFactory.property(String.class);

        username = objectFactory.property(String.class);
        token = objectFactory.property(String.class);

        tag = objectFactory.property(String.class).convention("HEAD");

        travis = objectFactory.property(Boolean.class);
    }

    public Provider<String> getOwner() {
        return slug.map(sl -> {
            Matcher matcher = slugPattern.matcher(sl);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return "";
        });
    }

    public void setOwner(String owner) {
        slug.set(owner + "/" + getRepo().getOrElse(""));
    }

    public Provider<String> getRepo() {
        return slug.map(sl -> {
            Matcher matcher = slugPattern.matcher(sl);
            if (matcher.matches()) {
                return matcher.group(2);
            }
            return "";
        });
    }

    public void setRepo(String repo) {
        slug.set(getOwner().getOrElse("") + "/" + repo);
    }
}
