package io.freefair

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

@Aspect
class KotlinAspect {

    @Around("execution(* KotlinDummy.main())")
    fun aroundMain(jp: ProceedingJoinPoint) {
        jp.proceed()
        throw UnsupportedOperationException("dont call main")
    }

}

class KotlinDummy {
    fun main() {
        println("Hello from Kotlin")
    }
}


