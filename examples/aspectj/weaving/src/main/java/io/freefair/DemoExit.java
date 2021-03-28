package io.freefair;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class DemoExit {

    @AfterReturning("execution(* *(..)) && !within(io.freefair.DemoExit) && !adviceexecution() && !@annotation(lombok.Generated)")
    public void logExit(JoinPoint joinPoint) {
        System.out.print(joinPoint.getStaticPart());
        System.out.print(" <- ");
        System.out.println(joinPoint.getSignature());
    }

    @AfterThrowing(value = "execution(* *(..)) && !within(io.freefair.DemoExit) && !adviceexecution() && !@annotation(lombok.Generated)", throwing = "e")
    public void logThrow(JoinPoint joinPoint, Exception e) {
        System.out.print(joinPoint.getStaticPart());
        System.out.print(e.getMessage());
        System.out.println(joinPoint.getSignature());
    }

    @Around("execution(* Dummy.test())")
    public Object logAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return proceedingJoinPoint.proceed();
    }

    private static final Object foo = new Object();
    static {
        System.out.println("bar");
    }
}
