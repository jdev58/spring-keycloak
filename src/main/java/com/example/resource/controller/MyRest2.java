package com.example.resource.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
public class MyRest2 {
    @GetMapping("/get")
    public String getUser(){
        return "1";
    }

    @GetMapping("/register")
    public String getRegister(){
        return "1";
    }
}
