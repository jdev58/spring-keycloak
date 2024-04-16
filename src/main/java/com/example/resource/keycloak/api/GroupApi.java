package com.example.resource.keycloak.api;


import com.example.resource.keycloak.dto.KeycloakUser;

import java.util.List;

public interface GroupApi {
    void createGroup(String group);

    List<KeycloakUser> getGroupMembers(String groupName);
}
