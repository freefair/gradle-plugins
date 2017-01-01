package io.freefair.gradle.plugins

import org.codehaus.groovy.runtime.NumberAwareComparator

/**
 * @author Lars Grefer
 */
class GitVersionConvention {

    String gitTagPrefix = "";

    Comparator<String> gitVersionComparator = new NumberAwareComparator<>()
}
