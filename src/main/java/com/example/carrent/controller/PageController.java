package com.example.carrent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    public String taxi() {
        return "cars-taxi";
    }

    @GetMapping("/cars-delivery")
    public String delivery() {
        return "cars-delivery";
    }
}