package io.freefair.gradle.plugins;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.groovy.runtime.NumberAwareComparator;

import java.util.Comparator;

/**
 * @author Lars Grefer
 */
@SuppressWarnings("WeakerAccess")
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class GitVersionConvention {

    private String gitTagPrefix = "";

    private Comparator<String> gitVersionComparator = new NumberAwareComparator<>();
}
