package io.freefair.gradle.plugins.git

import org.codehaus.groovy.runtime.NumberAwareComparator

/**
 * Created by larsgrefer on 15.03.16.
 */
class GitVersionConvention {

    String gitTagPrefix = "";

    Comparator<String> gitVersionComparator = new NumberAwareComparator<>()
}
