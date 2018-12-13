package io.freefair;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class Demo {

    @Before("execution(* *(..)) && !within(io.freefair.Demo) && !adviceexecution()")
    public void logEnter(JoinPoint joinPoint) {
        System.out.print(joinPoint.getStaticPart());
        System.out.print(" -> ");
        System.out.println(joinPoint.getSignature());
    }
}
