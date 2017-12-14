package io.freefair.gradle.plugins.maven;

import org.gradle.api.Plugin;
import org.gradle.api.plugins.WarPlugin;

public class MavenPublishWarPlugin extends MavenPublishBasePlugin {

    @Override
    protected Class<? extends Plugin> getPluginClass() {
        return WarPlugin.class;
    }

    @Override
    String getComponentName() {
        return "web";
    }
}
