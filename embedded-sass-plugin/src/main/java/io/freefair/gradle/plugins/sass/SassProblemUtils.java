package io.freefair.gradle.plugins.sass;

import com.sass_lang.embedded_protocol.SourceSpan;
import lombok.experimental.UtilityClass;
import org.gradle.api.problems.ProblemSpec;

@UtilityClass
public class SassProblemUtils {

    public static void fillSpanInfo(ProblemSpec problemSpec, SourceSpan span) {

        if (span.hasEnd()) {
            int size = span.getEnd().getOffset() - span.getStart().getOffset();
            problemSpec = problemSpec.lineInFileLocation(span.getUrl(), span.getStart().getLine(), span.getStart().getColumn(), size);
        } else {
            problemSpec = problemSpec.lineInFileLocation(span.getUrl(), span.getStart().getLine(), span.getStart().getColumn());
        }

    }
}
