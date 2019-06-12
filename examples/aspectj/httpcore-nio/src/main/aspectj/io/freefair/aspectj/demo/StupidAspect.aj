package io.freefair.aspectj.demo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class StupidAspect {

    @Around("execution(* org.apache.http.util.Args.*(..))")
    public Object stupidAdvice(ProceedingJoinPoint joinPoint) {
        throw new RuntimeException("Doing stupid things");
    }
}
