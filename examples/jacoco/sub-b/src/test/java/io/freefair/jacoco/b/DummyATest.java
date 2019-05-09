package io.freefair.jacoco.b;

import io.freefair.jacoco.a.DummyA;
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
    void bar() {
        assertThat(dummyA.bar()).isEqualTo(42);
    }
}
