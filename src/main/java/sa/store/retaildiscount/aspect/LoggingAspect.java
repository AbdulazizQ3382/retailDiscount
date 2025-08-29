package sa.store.retaildiscount.aspect;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* sa.store.retaildiscount.service..*(..))")
    public void serviceLayer() {}

    @Pointcut("execution(* sa.store.retaildiscount.controller..*(..))")
    public void controllerLayer() {}

    @Pointcut("execution(* sa.store.retaildiscount.config..*(..))")
    public void configLayer() {}

    @Around("serviceLayer() || controllerLayer()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.info("==> Entering method: {}.{}() with arguments: {}", 
                className, methodName, Arrays.toString(args));
        
        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("<== Exiting method: {}.{}() with result: {} | Execution time: {} ms", 
                    className, methodName, result, executionTime);
            
            return result;
            
        } catch (Exception ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("<== Exception in method: {}.{}() | Execution time: {} ms | Exception: {}", 
                    className, methodName, executionTime, ex.getMessage());
            throw ex;
        }
    }


    @AfterThrowing(pointcut = "serviceLayer() || controllerLayer()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.error("EXCEPTION: Error in {}.{}() - {}: {}", 
                className, methodName, exception.getClass().getSimpleName(), exception.getMessage());
    }
}