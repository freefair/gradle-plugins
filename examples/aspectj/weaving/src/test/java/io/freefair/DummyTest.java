package io.freefair;

import org.junit.Before;
import org.junit.Test;

public class DummyTest {

    Dummy dummy;

    @Before
    public void setUp() throws Exception {

        dummy = new Dummy();
    }

    @Test
    public void test() {
        dummy.equals(new Dummy());
        dummy.test();
    }
}
