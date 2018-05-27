# Enhanced `war` tasks

## Plugins

### `io.freefair.war-overlay`

Adds maven-like overlays to every task of type `War`:

```groovy
war {
    overlays {
        foo {
            from "com.example:foo:1.0@war"
            
            // enabled = true
            // provided = false
            // enableCompilation = true
            // exclude "*.html"
        }
        bar {
            from project(":bar")
        }
    }
}
```

Every `War` task is extended with a `overlays` property which is a `NamedDomainObjectContainer` of
`WarOverlay` objects. This can be compared to the `<overlays>` configuration parameter of the `maven-war-plugin`:
https://maven.apache.org/plugins/maven-war-plugin/war-mojo.html#overlays

The source of the war overlays can be

* an [`AbstractArchiveTask`](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/bundling/AbstractArchiveTask.html)
* another project (`project(":foo")`) which has the `war` plugin applied
* a dependency in any of the notations described [here](https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/dsl/DependencyHandler.html)
(The dependency has to resolve to a single war, jar or zip file)

### `io.freefair.war-attach-classes`

This plugin ports the [`attachClasses`](https://maven.apache.org/plugins/maven-war-plugin/war-mojo.html#attachClasses)
configuration parameter of the `maven-war-plugin` to gradle:

```groovy
war {
    attachClasses = true
    // classesClassifier = "classes"
}
```

### `io.freefair.war-archive-classes`

This plugin ports the [`archiveClasses`](https://maven.apache.org/plugins/maven-war-plugin/war-mojo.html#archiveClasses)
configuration parameter of the `maven-war-plugin` to gradle:

```groovy
war {
    archiveClasses = true
}
```