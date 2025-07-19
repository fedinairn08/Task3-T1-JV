package com.task3.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
@Component
public class AuditAspect {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final boolean useConsole;
    private final String topic;

    public AuditAspect(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${weyland.audit.mode:console}") String auditMode,
            @Value("${weyland.audit.kafka.topic:android-audit}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.useConsole = "console".equals(auditMode);
        this.topic = topic;
    }

    @Around("@annotation(WeylandWatchingYou)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toString();
        Object[] args = joinPoint.getArgs();
        Object result;

        try {
            result = joinPoint.proceed();
            logAudit(String.format(
                    "Method: %s | Args: %s | Result: %s",
                    methodName, argsToString(args), result
            ));
        } catch (Throwable e) {
            logAudit(String.format(
                    "Method: %s | Args: %s | Error: %s",
                    methodName, argsToString(args), e.getMessage()
            ));
            throw e;
        }
        return result;
    }

    private void logAudit(String message) {
        if (useConsole) {
            System.out.println("[AUDIT] " + message);
        } else {
            kafkaTemplate.send(topic, message);
        }
    }

    private String argsToString(Object[] args) {
        if (args == null) return "null";
        return Arrays.stream(args)
                .map(arg -> argToString(arg, 0))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String argToString(Object arg, int depth) {
        if (depth > 2) return "...";
        if (arg == null) return "null";

        if (arg.getClass().isArray()) {
            return arrayToString(arg, depth);
        } else if (arg instanceof Collection) {
            return collectionToString((Collection<?>) arg, depth);
        }
        return arg.toString();
    }

    private String arrayToString(Object array, int depth) {
        int length = Array.getLength(array);
        if (length == 0) return "[]";

        return IntStream.range(0, Math.min(5, length))
                .mapToObj(i -> argToString(Array.get(array, i), depth + 1))
                .collect(Collectors.joining(", ", "[", length > 5 ? ", ...]" : "]"));
    }

    private String collectionToString(Collection<?> collection, int depth) {
        if (collection.isEmpty()) return "[]";

        return collection.stream()
                .limit(5)
                .map(item -> argToString(item, depth + 1))
                .collect(Collectors.joining(", ", "[", collection.size() > 5 ? ", ...]" : "]"));
    }
}
