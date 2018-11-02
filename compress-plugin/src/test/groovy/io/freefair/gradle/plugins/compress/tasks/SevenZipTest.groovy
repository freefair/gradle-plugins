package io.freefair.gradle.plugins.compress.tasks

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SevenZipTest extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'base'
                id 'io.freefair.compress.7z'
            }
        """
    }

    def testSevenZip() {
        buildFile << """
            task foo(type: SevenZip) {
                from rootDir
                exclude '**/*.7z'
            }
        """

        testProjectDir.newFolder("src")
        testProjectDir.newFile("src/test.txt") << "Hallo Welt"

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('foo')
                .withPluginClasspath()
                .withDebug(true)
                .build()

        then:
        result.task(":foo").outcome == SUCCESS
    }
}
