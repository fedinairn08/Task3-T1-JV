package com.task3.service;

import com.task3.commands.Command;
import com.task3.commands.CommandPriority;
import com.task3.exception.CommandQueueFullException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Service
@Validated
public class CommandService {

    private final List<Command> allCommands = new CopyOnWriteArrayList<>();
    private final List<Command> pendingCommands = new CopyOnWriteArrayList<>();
    private final List<Command> executedCommands = new CopyOnWriteArrayList<>();

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(100);
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, queue
    );

    public void executeCommand(Command command) {
        allCommands.add(command);

        if (command.priority() == CommandPriority.CRITICAL) {
            processCommand(command);
        } else {
            if (queue.remainingCapacity() == 0) {
                throw new CommandQueueFullException("Command queue is full");
            }
            pendingCommands.add(command);
            executor.execute(() -> {
                pendingCommands.remove(command);
                processCommand(command);
            });
        }
    }

    private void processCommand(Command command) {
        System.out.printf(
                "[EXEC] %s by %s (Priority: %s, Time: %s)%n",
                command.description(),
                command.author(),
                command.priority(),
                command.time()
        );
        executedCommands.add(command);
    }

    public int getQueueSize() {
        return queue.size();
    }

    public List<Command> getAllCommands() {
        return Collections.unmodifiableList(allCommands);
    }
}
