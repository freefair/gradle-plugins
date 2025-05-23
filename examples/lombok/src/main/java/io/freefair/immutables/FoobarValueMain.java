package io.freefair.immutables;

import java.util.List;

public class FoobarValueMain {
    public static void main(String... args) {
        FoobarValue value = ImmutableFoobarValue.builder()
                .foo(2)
                .bar("Bar")
                .addBuz(1, 3, 4)
                .build(); // FoobarValue{foo=2, bar=Bar, buz=[1, 3, 4], crux={}}

        int foo = value.foo(); // 2

        List<Integer> buz = value.buz(); // ImmutableList.of(1, 3, 4)
    }
}
