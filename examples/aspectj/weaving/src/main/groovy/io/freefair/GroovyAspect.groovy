package io.freefair

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

@Aspect
class GroovyAspect {

    @Around("execution(* GroovyDummy.groovyTest())")
    void aroundGT(ProceedingJoinPoint joinPoint) {
        joinPoint.proceed()
        throw new UnsupportedOperationException("dont call groovyTest")
    }
}
