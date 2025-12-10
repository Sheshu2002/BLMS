package com.example.bank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UiController {
    @GetMapping({"/", "/ui"})
    public String index() { return "index"; }

    @GetMapping("/favicon.ico")
    public String favicon() { return "forward:/favicon.svg"; }
}