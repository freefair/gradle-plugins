package io.freefair.gradle.plugins.github;

import lombok.Data;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class GithubExtension {

    private final static Pattern slugPattern = Pattern.compile("(.*)/(.*)");

    private final Property<String> slug;

    private final Property<String> username;

    private final Property<String> token;

    @Inject
    public GithubExtension(ObjectFactory objectFactory) {
        slug = objectFactory.property(String.class);

        username = objectFactory.property(String.class);
        token = objectFactory.property(String.class);
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
