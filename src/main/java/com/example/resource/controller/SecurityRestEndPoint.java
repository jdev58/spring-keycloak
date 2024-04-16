package com.example.resource.controller;


import com.example.resource.dto.UserLoginRequest;
import com.example.resource.dto.UserLoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class SecurityRestEndPoint {


    @PostMapping(value = "/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody final UserLoginRequest request) {
        UserLoginResponse response = UserLoginResponse.builder().build();//userFacadeService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
