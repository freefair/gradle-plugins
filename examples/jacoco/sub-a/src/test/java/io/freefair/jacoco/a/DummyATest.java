package io.freefair.jacoco.a;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DummyATest {

    private DummyA dummyA;

    @BeforeEach
    void setUp() {
        dummyA = new DummyA();
    }

    @Test
    void foo() {
        assertThat(dummyA.foo()).isEqualTo(42);
    }
}
