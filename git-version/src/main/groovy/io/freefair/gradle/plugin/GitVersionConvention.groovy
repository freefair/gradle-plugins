package io.freefair.gradle.plugin

import org.codehaus.groovy.runtime.NumberAwareComparator

/**
 * Created by larsgrefer on 15.03.16.
 */
class GitVersionConvention {

    String gitTagPrefix = "";

    Comparator<String> gitVersionComparator = new NumberAwareComparator<>()
}
