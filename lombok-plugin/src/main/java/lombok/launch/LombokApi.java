package lombok.launch;

import lombok.SneakyThrows;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lars Grefer
 * @implNote Must be in this package in order to access {@link Main#getShadowClassLoader()}
 */
public class LombokApi {

    private final Class<?> configurationApp;
    private final Method runApp;
    private final Method redirectOutput;

    @SneakyThrows
    public LombokApi() {
        ClassLoader shadowClassLoader = Main.getShadowClassLoader();

        configurationApp = shadowClassLoader.loadClass("lombok.core.configuration.ConfigurationApp");
        runApp = configurationApp.getMethod("runApp", List.class);
        redirectOutput = configurationApp.getMethod("redirectOutput", PrintStream.class, PrintStream.class);
    }

    public void config(OutputStream outputStream, File... paths) {
        List<String> args = Arrays.stream(paths).map(File::getAbsolutePath).collect(Collectors.toList());

        config(outputStream, args);
    }

    @SneakyThrows
    public void config(OutputStream outputStream, List<String> args) {
        Object configApp = configurationApp.getConstructor().newInstance();

        redirectOutput.invoke(configApp, new PrintStream(outputStream, true), System.err);
        runApp.invoke(configApp, args);
    }
}
