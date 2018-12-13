package io.freefair;

public class Dummy {

    public String test() {
        return "foo";
    }

    private static final Object foo = new Object();
    static {
        System.out.println("bar");
        Demo demo = new Demo();
    }
}
