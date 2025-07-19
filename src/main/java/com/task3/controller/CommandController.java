package com.task3.controller;

import com.task3.commands.Command;
import com.task3.service.CommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommandController {

    private final CommandService commandService;

    @PostMapping("/addCommand")
    public ResponseEntity<String> addCommand(@Valid @RequestBody Command command) {
        commandService.executeCommand(command);
        return ResponseEntity.accepted().body("Command accepted");
    }

    @GetMapping("/commands")
    public ResponseEntity<List<Command>> getAllCommands() {
        return ResponseEntity.ok(commandService.getAllCommands());
    }
}
