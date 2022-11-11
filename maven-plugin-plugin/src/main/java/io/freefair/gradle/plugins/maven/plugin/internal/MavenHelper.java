package io.freefair.gradle.plugins.maven.plugin.internal;

import lombok.experimental.UtilityClass;
import org.apache.maven.tools.plugin.extractor.annotations.converter.tag.block.JavadocBlockTagToHtmlConverter;
import org.apache.maven.tools.plugin.extractor.annotations.converter.tag.block.SeeTagConverter;
import org.apache.maven.tools.plugin.extractor.annotations.converter.tag.inline.*;
import org.codehaus.plexus.archiver.jar.JarUnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.DefaultArchiverManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class MavenHelper {

    public static Map<String, JavadocInlineTagToHtmlConverter> getJavadocInlineTagToHtmlConverters() {
        HashMap<String, JavadocInlineTagToHtmlConverter> map = new HashMap<>();

        map.put("code", new CodeTagConverter());
        map.put("linkPlain", new LinkPlainTagToHtmlConverter());
        map.put("link", new LinkTagToHtmlConverter());
        map.put("value", new ValueTagConverter());
        map.put("literal", new LiteralTagToHtmlConverter());
        map.put("docRoot", new DocRootTagConverter());

        return map;
    }

    public static Map<String, JavadocBlockTagToHtmlConverter> getJavadocBlockTagToHtmlConverters() {
        HashMap<String, JavadocBlockTagToHtmlConverter> map = new HashMap<>();

        map.put("see", new SeeTagConverter());

        return map;
    }

    public static ArchiverManager getArchiverManager() {
        return new DefaultArchiverManager(
                Collections.emptyMap(),
                Collections.singletonMap("jar", JarUnArchiver::new),
                Collections.emptyMap()
        );
    }
}
