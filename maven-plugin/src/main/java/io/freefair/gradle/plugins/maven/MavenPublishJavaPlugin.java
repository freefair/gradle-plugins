package io.freefair.gradle.plugins.maven;

import org.gradle.api.Plugin;
import org.gradle.api.plugins.JavaPlugin;

public class MavenPublishJavaPlugin extends MavenPublishBasePlugin {

    @Override
    protected Class<? extends Plugin> getPluginClass() {
        return JavaPlugin.class;
    }

    @Override
    String getComponentName() {
        return "java";
    }
}
