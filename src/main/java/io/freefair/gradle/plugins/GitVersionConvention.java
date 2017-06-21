package io.freefair.gradle.plugins;

import lombok.*;
import org.codehaus.groovy.runtime.NumberAwareComparator;
import org.gradle.api.Incubating;

import java.util.Comparator;

/**
 * @author Lars Grefer
 */
@Data
@Incubating
@SuppressWarnings("WeakerAccess")
public class GitVersionConvention {

    private String gitTagPrefix = "";

    private Comparator<String> gitVersionComparator = new NumberAwareComparator<>();
}
