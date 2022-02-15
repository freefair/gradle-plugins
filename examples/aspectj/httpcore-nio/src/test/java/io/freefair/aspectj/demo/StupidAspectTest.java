package io.freefair.aspectj.demo;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StupidAspectTest {

    @Test
    public void stupidAdvice() {
        try {
            org.apache.http.util.Args.check(true, "foo");
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("stupid"));
        }
    }
}
