package io.freefair;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GroovyTest {

    @Test
    public void testGroovy() {
        try {
            GroovyDummy.groovyTest();
            fail();
        } catch (UnsupportedOperationException e) {
            assertEquals("dont call groovyTest", e.getMessage());
        }
    }
}
