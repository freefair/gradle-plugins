package io.freefair.aspectj.test;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class DummyAspect {

    @Before("execution(* *(..)) && !within(io.freefair.aspectj.test.DummyAspect) && !adviceexecution()")
    public void logEnter(JoinPoint joinPoint) {
        System.out.print(joinPoint.getStaticPart());
        System.out.print(" -> ");
        System.out.println(joinPoint.getSignature());
    }
}
