package com.example.resource.keycloak.mapper;


import com.example.resource.keycloak.dto.KeycloakUser;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class KeycloakUserMapper {

    public static KeycloakUser map(UserRepresentation userRepresentation, List<GroupRepresentation> groups, List<RoleRepresentation> roles) {
        KeycloakUser keyCloakUser  = KeycloakUser.builder().build();
        keyCloakUser.setId(userRepresentation.getId());
        keyCloakUser.setUserName(userRepresentation.getUsername());
        keyCloakUser.setFirstName(userRepresentation.getFirstName());
        keyCloakUser.setLastName(userRepresentation.getLastName());
        keyCloakUser.setEmail(userRepresentation.getEmail());
        if (userRepresentation.getAttributes() != null && userRepresentation.getAttributes().containsKey("locale")) {
            keyCloakUser.setLocale(userRepresentation.getAttributes().get("locale").get(0));
        }
        if (groups != null) {
            keyCloakUser.setGroups(groups.stream().map(GroupRepresentation::getName).collect(Collectors.toList()));
        }
        if (roles != null) {
            keyCloakUser.setRoles(roles.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
        }
        return keyCloakUser;
    }

    public static UserRepresentation map(KeycloakUser user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(user.getId());
        userRepresentation.setUsername(user.getUserName());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEmail(user.getEmail());
        addLocaleToUserRepresentation(user, userRepresentation);
        userRepresentation.setGroups(user.getGroups());
        return userRepresentation;
    }

    public static void addLocaleToUserRepresentation(KeycloakUser source, UserRepresentation target) {
        if (source.getLocale() != null) {

            Map<String, List<String>> attributes = new HashMap<>();
            if (target.getAttributes() != null) {
                attributes.putAll(target.getAttributes());
            }
            attributes.put("locale", Collections.singletonList(source.getLocale()));

            target.setAttributes(attributes);
        }
    }

}
