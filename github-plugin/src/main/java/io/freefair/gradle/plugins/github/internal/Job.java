package io.freefair.gradle.plugins.github.internal;

import lombok.Data;

@Data
public class Job {

    /**
     * The external ID of the job.
     */
    private String id;

    /**
     * Correlator provides a key that is used to group snapshots submitted over time.
     * Only the "latest" submitted snapshot for a given combination of job.correlator and detector.name will be considered when calculating a repository's current dependencies.
     * Correlator should be as unique as it takes to distinguish all detection runs for a given "wave" of CI workflow you run.
     * If you're using GitHub Actions, a good default value for this could be the environment variables GITHUB_WORKFLOW and GITHUB_JOB concatenated together.
     * If you're using a build matrix, then you'll also need to add additional key(s) to distinguish between each submission inside a matrix variation.
     */
    private String correlator;

    /**
     * The url for the job.
     */
    private String html_url;

}
