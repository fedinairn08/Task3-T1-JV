package com.task3.metrics;

import com.task3.service.CommandService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class MetricsPublisher {

    private final ConcurrentHashMap<String, Counter> authorCounters = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    private final CommandService commandService;

    public MetricsPublisher(
            MeterRegistry meterRegistry,
            CommandService commandService
    ) {
        this.meterRegistry = meterRegistry;
        this.commandService = commandService;
        registerQueueGauge();
    }

    @PostConstruct
    private void registerQueueGauge() {
        Gauge.builder("android.queue.size", commandService::getQueueSize)
                .register(meterRegistry);
    }

    public void registerQueueSizeGauge(Supplier<Number> supplier) {
        Gauge.builder("android.queue.size", supplier)
                .register(meterRegistry);
    }

    public void incrementAuthorCounter(String author) {
        Counter counter = authorCounters.computeIfAbsent(author, a ->
                Counter.builder("android.commands.completed")
                        .tag("author", author)
                        .register(meterRegistry)
        );
        counter.increment();
    }
}
