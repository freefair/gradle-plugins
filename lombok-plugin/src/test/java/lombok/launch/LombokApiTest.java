package lombok.launch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LombokApiTest {

    private LombokApi lombokApi;

    @BeforeEach
    void setUp() {
        lombokApi = new LombokApi();
    }

    @Test
    void config() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        lombokApi.config(outputStream, new File("."));

        String config = outputStream.toString();
        assertThat(config).contains("=");
    }
}
