package com.example.resource.keycloak;

import com.example.resource.initializer.KeycloakInitializerConfigurationProperties;
import com.example.resource.keycloak.api.GroupApi;
import com.example.resource.keycloak.dto.KeycloakUser;
import com.example.resource.keycloak.helper.ResponseHelper;
import com.example.resource.keycloak.mapper.KeycloakUserMapper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * An easy to use wrapper around the keycloak admin API group management rest calls.
 * <p>
 * The {@link KeycloakGroupApi} uses a user session and must be closed (=logout for the given admin user) when no longer needed.
 * It is implemented as an {@link AutoCloseable}.
 */
@SuppressWarnings("unused")

@Service
public class KeycloakGroupApi implements GroupApi {
    private static final Logger LOG = LoggerFactory.getLogger(KeycloakGroupApi.class);


    @Autowired
    private Keycloak keycloak;


    @Autowired
    KeycloakInitializerConfigurationProperties keyClockConfig;




    public RealmResource getRealmResource() {
        return keycloak.realm(keyClockConfig.getApplicationRealm());
    }


    @Override
    public void createGroup(String group) {
        RealmResource realm = getRealmResource();
        GroupsResource groupsResource = realm.groups();
        GroupRepresentation groupRepresentation = new GroupRepresentation();
        groupRepresentation.setName(group);
        Response response = groupsResource.add(groupRepresentation);
        ResponseHelper.checkCreateResponse(group, response);
        String groupId = ResponseHelper.getIdFromLocation(response);
    }

    /**
     * retrieves all members of the given group.
     */
    @Override
    public List<KeycloakUser> getGroupMembers(String groupName) {
        RealmResource realm = getRealmResource();
        GroupsResource groups = realm.groups();
        List<GroupRepresentation> groupRepresentations = ResponseHelper.retryWithException(groups::groups);
        for (GroupRepresentation groupRepresentation : groupRepresentations) {
            if (groupRepresentation.getName().equals(groupName)) {
                GroupResource group = groups.group(groupRepresentation.getId());
                return group.members().stream().map(user -> KeycloakUserMapper.map(user, Collections.emptyList(), Collections.emptyList())).collect(Collectors.toList());
            }
        }
        LOG.warn("Group " + groupName + " not found in keycloak.");
        return Collections.emptyList();
    }
}
