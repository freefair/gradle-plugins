package io.freefair.gradle.plugins.maven.plugin.wrappers;

import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.logging.Log;
import org.gradle.api.logging.Logger;

/**
 * Maven {@link Log} implementation which delegates to a {@link Logger gradle logger}.
 *
 * @author Lars Grefer
 */
@RequiredArgsConstructor
public class MavenLogWrapper implements Log {

    private final Logger gradleLogger;

    @Override
    public boolean isDebugEnabled() {
        return gradleLogger.isDebugEnabled();
    }

    @Override
    public void debug(CharSequence content) {
        gradleLogger.debug(content.toString());
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        gradleLogger.debug(content.toString(), error);
    }

    @Override
    public void debug(Throwable error) {
        gradleLogger.debug(error.getLocalizedMessage(), error);
    }

    @Override
    public boolean isInfoEnabled() {
        return gradleLogger.isInfoEnabled();
    }

    @Override
    public void info(CharSequence content) {
        gradleLogger.info(content.toString());
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        gradleLogger.info(content.toString(), error);
    }

    @Override
    public void info(Throwable error) {
        gradleLogger.info(error.getLocalizedMessage(), error);
    }

    @Override
    public boolean isWarnEnabled() {
        return gradleLogger.isWarnEnabled();
    }

    @Override
    public void warn(CharSequence content) {
        gradleLogger.warn(content.toString());
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        gradleLogger.warn(content.toString(), error);
    }

    @Override
    public void warn(Throwable error) {
        gradleLogger.warn(error.getLocalizedMessage(), error);
    }

    @Override
    public boolean isErrorEnabled() {
        return gradleLogger.isErrorEnabled();
    }

    @Override
    public void error(CharSequence content) {
        gradleLogger.error(content.toString());
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        gradleLogger.error(content.toString(), error);
    }

    @Override
    public void error(Throwable error) {
        gradleLogger.error(error.getLocalizedMessage(), error);
    }
}
