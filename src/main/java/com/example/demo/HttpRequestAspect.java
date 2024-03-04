package com.example.demo;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
public class HttpRequestAspect {

    private final Tracer tracer;

    @Autowired
    public HttpRequestAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    @Pointcut("execution(@org.springframework.web.bind.annotation.GetMapping * *(..)) && within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object traceHttpRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the current HTTP request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // Access request information
        String servletPath = request.getServletPath();
        String method = request.getMethod();
        Map<String, String[]> parameterMap = request.getParameterMap();

        // Create a new span
        SpanBuilder spanBuilder = tracer.spanBuilder(servletPath)
                .setAttribute("http.method", method);
        parameterMap.forEach((key, value) -> spanBuilder.setAttribute("http.param." + key, Arrays.toString(value)));

        Span span = spanBuilder.startSpan();
        try (Scope scope = span.makeCurrent()) {
            // Proceed with the original method execution
            return joinPoint.proceed();
        } finally {
            span.end(); // End the span when the method execution completes
        }
    }
}
