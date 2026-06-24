package com.github.huangguasky.awesometools.core;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ExpressionResolver {

    private final ExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public String resolve(String expression, ProceedingJoinPoint joinPoint) {
        if (expression == null || expression.isBlank()) {
            return "";
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("methodName", method.getName());
        context.setVariable("className", method.getDeclaringClass().getName());
        context.setVariable("args", args);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        Object value = parser.parseExpression(expression).getValue(context);
        return value == null ? "" : String.valueOf(value);
    }
}
