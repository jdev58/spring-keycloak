package com.example.resource.controller;

import com.example.resource.keycloak.KeycloakUserApi;
import com.example.resource.keycloak.dto.KeycloakUser;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Random;

@RestController
@RequestMapping("/user")
@Slf4j
public class MyRest {


    //private KeycloakInitializer keycloakInitializer;

    @Autowired
    private Keycloak keycloak;

    @Autowired
    KeycloakUserApi keycloakUserApi;

    /*public MyRest(KeycloakInitializer keycloakInitializer) {
        this.keycloakInitializer = keycloakInitializer;
    }*/

    @GetMapping("/get")
    public String getUser(){
        return "1";
    }

    @GetMapping("/register")
    public String getRegister(){
        //KeycloakUser user = new KeycloakUser("ali5", "password", "email5@email.com", false);

        //keycloakInitializer.initKeycloakUser(user);

        Keycloak key1 =  keycloakUserApi.getToken("ali-1974901646@ernyka.com", "1234");

        AccessTokenResponse accessToken2 = key1.tokenManager().getAccessToken();


        return "1";
    }

    private void doSome(){

        AccessTokenResponse accessToken = keycloak.tokenManager().getAccessToken();

        System.out.println(accessToken.getToken());

        Integer random = new Random().nextInt();

        String id = String.valueOf(System.currentTimeMillis()).toString();
        String userName = "ali"+ random;
        log.info("user name is {}", userName);
        String password = "1234";
        KeycloakUser user = KeycloakUser.builder().id(id).userName(userName).firstName(userName).lastName(userName)
                .email(userName+"@ernyka.com").groups(Arrays.asList("GROUP_USER")).roles(Arrays.asList("ROLE_USER")).build();

        KeycloakUser myUser = keycloakUserApi.createUser(user, password);

        Keycloak key1 =  keycloakUserApi.getToken(userName+"@ernyka.com", password);

        AccessTokenResponse accessToken2 = key1.tokenManager().getAccessToken();
        System.out.println(accessToken2.getToken());

    }

}
