package io.freefair.gradle.plugin.android.quality
import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Incubating
import org.gradle.api.JavaVersion
import org.gradle.api.Task
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.plugins.quality.TargetJdk
import org.gradle.util.VersionNumber
/**
 * Copy of {@link org.gradle.api.plugins.quality.PmdPlugin} which
 * <ul>
 * <li>extends {@link AbstractAndroidCodeQualityPlugin} instead of {@link org.gradle.api.plugins.quality.internal.AbstractCodeQualityPlugin}</li>
 * <li>uses {@link AndroidSourceSet AndroidSourceSets} instead of {@link org.gradle.api.tasks.SourceSet JavaSourceSets}</li>
 * </ul>
 *
 * @see org.gradle.api.plugins.quality.PmdPlugin
 * @see AbstractAndroidCodeQualityPlugin
 */
@Incubating
class AndroidPmdPlugin extends AbstractAndroidCodeQualityPlugin<Pmd> {

    public static final String DEFAULT_PMD_VERSION = "5.2.3"
    private PmdExtension extension

    @Override
    protected String getToolName() {
        return "PMD"
    }

    @Override
    protected Class<Pmd> getTaskType() {
        return Pmd
    }

    @Override
    protected CodeQualityExtension createExtension() {
        extension = project.extensions.create("pmd", PmdExtension, project)
        extension.with {
            toolVersion = DEFAULT_PMD_VERSION
            // NOTE: should change default rule set to java-basic once we bump default version to 5.0+
            // this will also require a change to Pmd.run() (convert java-basic to basic for old versions,
            // instead of basic to java-basic for new versions)
            ruleSets = ["basic"]
            ruleSetFiles = project.files()
        }
        extension.getConventionMapping().with{
            targetJdk = { getDefaultTargetJdk(androidExtension.compileOptions.sourceCompatibility) }
        }
        return extension
    }

    TargetJdk getDefaultTargetJdk(JavaVersion javaVersion) {
        try {
            return TargetJdk.toVersion(javaVersion.toString())
        } catch(IllegalArgumentException ignored) {
            // TargetJDK does not include 1.1, 1.2 and 1.8;
            // Use same fallback as PMD
            return TargetJdk.VERSION_1_4
        }
    }

    @Override
    protected void configureTaskDefaults(Pmd task, String baseName) {
        def config = project.configurations['pmd']
        config.defaultDependencies { dependencies ->
            VersionNumber version = VersionNumber.parse(this.extension.toolVersion)
            String dependency = calculateDefaultDependencyNotation(version)
            dependencies.add(this.project.dependencies.create(dependency))
        }
        task.conventionMapping.with {
            pmdClasspath = { config }
            ruleSets = { extension.ruleSets }
            ruleSetConfig = { extension.ruleSetConfig }
            ruleSetFiles = { extension.ruleSetFiles }
            ignoreFailures = { extension.ignoreFailures }
            rulePriority = { extension.rulePriority }
            consoleOutput = { extension.consoleOutput }
            targetJdk = { extension.targetJdk }
            task.reports.all { report ->
                report.conventionMapping.with {
                    enabled = { true }
                    destination = { new File(extension.reportsDir, "${baseName}.${report.name}") }
                }
            }
        }
    }

    private String calculateDefaultDependencyNotation(VersionNumber toolVersion) {
        if (toolVersion < VersionNumber.version(5)) {
            return "pmd:pmd:$extension.toolVersion"
        } else if (toolVersion < VersionNumber.parse("5.2.0")) {
            return "net.sourceforge.pmd:pmd:$extension.toolVersion"
        }
        return "net.sourceforge.pmd:pmd-java:$extension.toolVersion"
    }

    @Override
    protected void configureForSourceSet(AndroidSourceSet sourceSet, Pmd task) {
        task.with {
            description = "Run PMD analysis for ${sourceSet.name} classes"
        }
        task.setSource(sourceSet.java.sourceFiles)
        task.conventionMapping.with {
            classpath = { sourceSet.java.sourceFiles }
        }
    }

    @Override
    protected void configureBaseTask(Task task) {
        task.description = "Run PMD analysis for all classes"
    }
}
