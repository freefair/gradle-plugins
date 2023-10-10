package sass.embedded_protocol.EmbeddedSass;

import lombok.experimental.UtilityClass;

/**
 * Delegate for the old OutputStyle FQN.
 * <p>
 * This should help builds to migrate to the new package.
 *
 * @author Lars Grefer
 * @deprecated Migrate to {@link com.sass_lang.embedded_protocol.OutputStyle}
 */
@Deprecated
@UtilityClass
public final class OutputStyle {

    /**
     * @see com.sass_lang.embedded_protocol.OutputStyle#EXPANDED
     * @deprecated Migrate to {@link com.sass_lang.embedded_protocol.OutputStyle#EXPANDED}
     */
    @Deprecated
    public static final com.sass_lang.embedded_protocol.OutputStyle EXPANDED = com.sass_lang.embedded_protocol.OutputStyle.EXPANDED;

    /**
     * @see com.sass_lang.embedded_protocol.OutputStyle#COMPRESSED
     * @deprecated Migrate to {@link com.sass_lang.embedded_protocol.OutputStyle#COMPRESSED}
     */
    @Deprecated
    public static final com.sass_lang.embedded_protocol.OutputStyle COMPRESSED = com.sass_lang.embedded_protocol.OutputStyle.COMPRESSED;
}
