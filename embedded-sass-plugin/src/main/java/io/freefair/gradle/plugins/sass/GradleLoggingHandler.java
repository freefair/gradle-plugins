package io.freefair.gradle.plugins.sass;

import com.sass_lang.embedded_protocol.LogEventType;
import com.sass_lang.embedded_protocol.OutboundMessage;
import com.sass_lang.embedded_protocol.SourceSpan;
import de.larsgrefer.sass.embedded.logging.Slf4jLoggingHandler;
import lombok.Setter;
import org.gradle.api.Incubating;
import org.gradle.api.logging.Logger;
import org.gradle.api.problems.ProblemGroup;
import org.gradle.api.problems.ProblemId;
import org.gradle.api.problems.ProblemReporter;
import org.gradle.api.problems.Problems;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Setter
public class GradleLoggingHandler extends Slf4jLoggingHandler {

    private static final ProblemGroup problemGroup = ProblemGroup.create("sass-deprecations", "Sass Deprecation Warnings");

    @NonNull
    private final Logger logger;

    @Nullable
    private ProblemReporter problemReporter;

    @Incubating
    public GradleLoggingHandler(Logger logger, Problems problems) {
        this(logger);
        this.problemReporter = problems.getReporter();
    }

    public GradleLoggingHandler(Logger logger) {
        super(logger);
        this.logger = logger;
    }

    @Override
    public void handle(OutboundMessage.LogEventOrBuilder logEvent) {

        if (logEvent.getType() == LogEventType.DEPRECATION_WARNING) {
            logger.lifecycle(logEvent.getFormatted());
            reportProblem(logEvent);
        } else {
            super.handle(logEvent);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Incubating
    private void reportProblem(OutboundMessage.LogEventOrBuilder logEvent) {
        if (problemReporter == null) {
            return;
        }

        String deprecationType = logEvent.getDeprecationType();
        ProblemId problemId = ProblemId.create(deprecationType, "sass:" + deprecationType, problemGroup);
        problemReporter.report(problemId, problemSpec -> {

            if (logEvent.hasSpan()) {
                SassProblemUtils.fillSpanInfo(problemSpec, logEvent.getSpan());
            }

            problemSpec
                    .solution(logEvent.getMessage())
                    .documentedAt("https://sass-lang.com/d/" + logEvent.getDeprecationType())
                    .details(logEvent.getFormatted());
        });
    }

}
