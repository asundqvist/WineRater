package se.edugrade.winerater.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AliveController {

    @GetMapping
    public String alive() {
        return "Server is up and running!";
    }
}
