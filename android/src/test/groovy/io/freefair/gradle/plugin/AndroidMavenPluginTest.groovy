package io.freefair.gradle.plugin

import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test

/**
 * Created by larsgrefer on 30.12.15.
 */
public class AndroidMavenPluginTest {

    @Test
    public void testApply() throws Exception {

        Project project = ProjectBuilder.builder()
                .withProjectDir(new File("/Users/larsgrefer/git/advanced-bugsnag-android"))
                .build()
        project.pluginManager.apply(LibraryPlugin.class)
        project.pluginManager.apply(AndroidMavenPlugin.class)

        project.tasks.all {task ->
            println task
        }

    }
}