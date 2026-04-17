package lombok.launch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

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
