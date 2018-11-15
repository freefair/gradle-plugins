package io.freefair.gradle.plugins.maven.plugin.wrappers;

import lombok.RequiredArgsConstructor;
import org.codehaus.plexus.logging.Logger;

/**
 * Maven {@link Logger} implementation which delegates to a {@link org.gradle.api.logging.Logger gradle logger}.
 *
 * @author Lars Grefer
 */
@RequiredArgsConstructor
public class PlexusLoggerWrapper implements Logger {

    private final org.gradle.api.logging.Logger gradleLogger;
    private int threshold = Logger.LEVEL_DEBUG;

    @Override
    public void debug(String message) {
        gradleLogger.debug(message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        gradleLogger.debug(message, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return gradleLogger.isDebugEnabled();
    }

    @Override
    public void info(String message) {
        gradleLogger.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        gradleLogger.info(message, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return gradleLogger.isInfoEnabled();
    }

    @Override
    public void warn(String message) {
        gradleLogger.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        gradleLogger.warn(message, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return gradleLogger.isWarnEnabled();
    }

    @Override
    public void error(String message) {
        gradleLogger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        gradleLogger.error(message, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return gradleLogger.isErrorEnabled();
    }

    @Override
    public void fatalError(String message) {
        gradleLogger.error(message);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        gradleLogger.error(message, throwable);
    }

    @Override
    public boolean isFatalErrorEnabled() {
        return gradleLogger.isErrorEnabled();
    }

    @Override
    public int getThreshold() {
        return threshold;
    }

    @Override
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public Logger getChildLogger(String name) {
        return this;
    }

    @Override
    public String getName() {
        return gradleLogger.getName();
    }
}
