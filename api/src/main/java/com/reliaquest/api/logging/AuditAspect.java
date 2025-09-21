package com.reliaquest.api.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Around("@within(com.reliaquest.api.logging.Audited)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        log.debug("{}.{}() - Started execution with args: {}", className, methodName, joinPoint.getArgs());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();

            log.debug("{}.{}() - Completed in {} ms | Result: {}", className, methodName,
                    stopWatch.getTotalTimeMillis(),
                    formatResult(result));

            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.debug("{}.{}() - Failed after {} ms | Error: {}", className, methodName, stopWatch.getTotalTimeMillis(),
                    e.getMessage(), e);
            throw e;
        }
    }

    private String formatResult(Object result) {
        if (result == null) {
            return "void";
        }

        return result.toString();
    }
}
