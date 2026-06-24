package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.Fallback;
import com.github.huangguasky.awesometools.core.ThrowableMatcher;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 60)
public class FallbackAspect {

    @Around("@annotation(fallback)")
    public Object around(ProceedingJoinPoint joinPoint, Fallback fallback) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            if (!ThrowableMatcher.matches(ex, fallback.include(), fallback.exclude())) {
                throw ex;
            }
            return invokeFallback(joinPoint, fallback.method(), ex);
        }
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, String methodName, Throwable throwable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method method = findFallbackMethod(targetClass, methodName, signature.getMethod().getParameterTypes(), false);
        Object[] args = joinPoint.getArgs();
        if (method == null) {
            Class<?>[] withThrowable = Arrays.copyOf(signature.getMethod().getParameterTypes(),
                    signature.getMethod().getParameterCount() + 1);
            withThrowable[withThrowable.length - 1] = throwable.getClass();
            method = findFallbackMethod(targetClass, methodName, withThrowable, true);
            args = Arrays.copyOf(args, args.length + 1);
            args[args.length - 1] = throwable;
        }
        if (method == null) {
            throw new NoSuchMethodException("No fallback method found: " + methodName);
        }
        ReflectionUtils.makeAccessible(method);
        try {
            return method.invoke(joinPoint.getTarget(), args);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    private Method findFallbackMethod(Class<?> targetClass, String methodName, Class<?>[] parameterTypes, boolean throwableAware) {
        for (Method method : targetClass.getDeclaredMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != parameterTypes.length) {
                continue;
            }
            if (parametersMatch(method.getParameterTypes(), parameterTypes, throwableAware)) {
                return method;
            }
        }
        Class<?> superclass = targetClass.getSuperclass();
        return superclass == null ? null : findFallbackMethod(superclass, methodName, parameterTypes, throwableAware);
    }

    private boolean parametersMatch(Class<?>[] actual, Class<?>[] expected, boolean throwableAware) {
        for (int i = 0; i < actual.length; i++) {
            if (throwableAware && i == actual.length - 1) {
                if (!Throwable.class.isAssignableFrom(actual[i])) {
                    return false;
                }
                continue;
            }
            if (!actual[i].isAssignableFrom(expected[i])) {
                return false;
            }
        }
        return true;
    }
}
