package com.example.resource.keycloak.dto;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A user in keycloak. This is the model class to be used when managing users in keycloak with this wrapper class.
 * It contains more fields than the normal UserRepresentation class from keycloak, like roles and groups.
 */


@Data
@Setter
@Getter
@Builder
public final class KeycloakUser {

    private  String id;
    private  String userName;
    private  String firstName;
    private  String lastName;
    private  String email;
    private  String locale;
    private  List<String> groups;
    private  List<String> roles;



}
