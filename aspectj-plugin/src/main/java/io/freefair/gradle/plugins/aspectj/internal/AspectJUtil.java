package io.freefair.gradle.plugins.aspectj.internal;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class AspectJUtil {

    public static @Nonnull Map<String, String> getAspectJToolsExclude() {
        Map<String, String> excludeMap = new HashMap<>();
        excludeMap.put("group", "org.aspectj");
        excludeMap.put("module", "aspectjtools");
        return excludeMap;
    }
}
