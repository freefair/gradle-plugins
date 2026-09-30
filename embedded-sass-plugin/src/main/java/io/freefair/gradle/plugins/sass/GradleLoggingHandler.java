package io.freefair.gradle.plugins.sass;

import com.sass_lang.embedded_protocol.LogEventType;
import com.sass_lang.embedded_protocol.OutboundMessage;
import com.sass_lang.embedded_protocol.SourceSpan;
import de.larsgrefer.sass.embedded.logging.Slf4jLoggingHandler;
import lombok.Setter;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.problems.*;


@Setter
public class GradleLoggingHandler extends Slf4jLoggingHandler {

    static final ProblemGroup group = ProblemGroup.create("sass-deprecation-warning", "Sass Deprecated Warning");

    private final Logger logger;

    private ProblemReporter problemReporter;

    public GradleLoggingHandler(Task task, Problems problems) {
        this(task.getLogger());
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
            String deprecationType = "sass:" + logEvent.getDeprecationType();
            problemReporter.report(ProblemId.create(deprecationType, deprecationType, group), problemSpec -> fillProblemSpec(logEvent, problemSpec));
        } else {
            super.handle(logEvent);
        }
    }

    private static void fillProblemSpec(OutboundMessage.LogEventOrBuilder logEvent, ProblemSpec problemSpec) {

        if (logEvent.hasSpan()) {
            SourceSpan span = logEvent.getSpan();

            if (span.hasEnd()) {
                int size = span.getEnd().getOffset() - span.getStart().getOffset();
                problemSpec = problemSpec.lineInFileLocation(span.getUrl(), span.getStart().getLine(), span.getStart().getColumn(), size);
            } else {
                problemSpec = problemSpec.lineInFileLocation(span.getUrl(), span.getStart().getLine(), span.getStart().getColumn());
            }
        }

        problemSpec
                .contextualLabel(logEvent.getDeprecationType())
                .solution(logEvent.getMessage())
                .documentedAt("https://sass-lang.com/d/" + logEvent.getDeprecationType())
                .details(logEvent.getFormatted());
    }
}
