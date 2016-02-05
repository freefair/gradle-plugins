package io.freefair.gradle.plugin

import org.codehaus.groovy.runtime.NumberAwareComparator

class GitVersionExtension {

    String tagPrefix = '';

    Comparator<String> versionComparator = new NumberAwareComparator<>()
}
