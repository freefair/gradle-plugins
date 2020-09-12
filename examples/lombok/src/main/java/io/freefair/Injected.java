package io.freefair;

import javax.inject.Named;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Injected {

    @Named("x")
    private String x;

}
