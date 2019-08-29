package io.freefair;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class KotlinTest {

    @Test
    public void testKotlin() {
        try {
            new KotlinDummy().main();
            fail();
        } catch (UnsupportedOperationException e) {
            assertEquals("dont call main", e.getMessage());
        }
    }
}
