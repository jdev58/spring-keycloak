package com.example.resource.keycloak;

import com.example.resource.initializer.KeycloakInitializerConfigurationProperties;
import com.example.resource.keycloak.api.UserApi;
import com.example.resource.keycloak.dto.*;
import com.example.resource.keycloak.exception.MailAlreadyExistsException;
import com.example.resource.keycloak.exception.UnsupportedLocaleException;
import com.example.resource.keycloak.helper.ResponseHelper;
import com.example.resource.keycloak.mapper.KeycloakUserMapper;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An easy to use wrapper around the keycloak admin API user management rest calls.
 *
 * The {@link KeycloakUserApi} uses a user session and must be closed (=logout for the given admin user) when no longer needed.
 * It is implemented as an {@link AutoCloseable}.
 */
@SuppressWarnings("unused")
@Service
@Slf4j
public class KeycloakUserApi implements UserApi {

    /**
     * Creates a new {@link KeycloakUserApi} instance. The given user must have realm-management rights on client realm-admin!
     */

    @Autowired
    KeycloakInitializerConfigurationProperties keyClockConfig;


    @Autowired
    private Keycloak keyClockAdmin;


    @Override
    public Keycloak getToken(String userName, String password) {
        log.info("keyClockConfig.getUrl() is {}", keyClockConfig.getUrl());
        log.info("keyClockConfig.getApplicationRealm() is {}", keyClockConfig.getApplicationRealm());
        log.info("userName is  {}", userName);
        log.info("password is {}", password);
        log.info("keyClockConfig.getClientId() is {}", keyClockConfig.getClientId());

        Keycloak myKeyCloak =  Keycloak.getInstance(keyClockConfig.getUrl(), keyClockConfig.getApplicationRealm(), userName, password, "spring-resource-client", "M5RHSX0lelzDEQXjsDKKdU9GOjz3rq5F");

        return myKeyCloak;
    }




    public void close(String userName) throws Exception {
        RealmResource realm = getRealmResource();
        UsersResource usersResource = realm.users();
        List<UserRepresentation> backendUsers = usersResource.search(userName, null, null, null, 0, 1);
        if (backendUsers.size() == 1) {
            UserRepresentation backendUser = backendUsers.get(0);
            String backendUserId = backendUser.getId();
            UserResource userResource = usersResource.get(backendUserId);
            userResource.logout();
        }

    }



    public RealmResource getRealmResource() {
        return keyClockAdmin.realm(keyClockConfig.getApplicationRealm());
    }

    /*public KeycloakUserApi(AdapterConfig keycloakConfiguration, String userName, String password) {
        super(keycloakConfiguration, userName, password);
    }*/

    /**
     * Retrieves a user from keycloak.
     *
     * @param userId The keycloak user id
     */
    @Override
    public KeycloakUser getUser(String userId) {
        RealmResource realm = getRealmResource();
        UserResource userResource = realm.users().get(userId);
        UserRepresentation userRepresentation;
        try {
            userRepresentation = ResponseHelper.retryWithException(userResource::toRepresentation);
        } catch (NotFoundException e) {
            return null;
        }
        List<GroupRepresentation> groups = userResource.groups();
        List<RoleRepresentation> roles = userResource.roles().clientLevel(getClientUUID()).listEffective();
        return KeycloakUserMapper.map(userRepresentation, groups, roles);
    }

    /**
     * Retrieves a user by its email in keycloak.
     */
    @Override
    public KeycloakUser findUserByEmail(String email) {
        RealmResource realm = getRealmResource();
        UsersResource usersResource = realm.users();
        List<UserRepresentation> users = ResponseHelper.retryWithException(() -> usersResource.search(null, null, null, email, null, null));
        if (users.isEmpty()) {
            return null;
        }
        UserRepresentation userRepresentation = users.get(0);
        UserResource userResource = usersResource.get(userRepresentation.getId());
        List<GroupRepresentation> groups = userResource.groups();
        List<RoleRepresentation> roles = userResource.roles().clientLevel(getClientUUID()).listEffective();
        return KeycloakUserMapper.map(userRepresentation, groups, roles);
    }

    /**
     * Creates a new user without an initial password. The new user retrieves an email to set its initial password.
     *
     * @throws MailAlreadyExistsException when the email given is already used by another user.
     */
    @Override
    public KeycloakUser createUser(KeycloakUser user) throws MailAlreadyExistsException {
        return createUser(user, null);
    }

    /**
     * Creates a new user with (or without) an initial password.
     *
     * @param password (optional) The initial password to be set at the user. If not given the user will retrieve an email to set its initial password.
     *
     * @throws MailAlreadyExistsException when the email given is already used by another user.
     */
    @Override
    public KeycloakUser createUser(KeycloakUser user, String password)
            throws MailAlreadyExistsException, UnsupportedLocaleException {
        RealmResource realm = getRealmResource();
        validateLocales(realm.toRepresentation(), user.getLocale());
        UsersResource usersResource = realm.users();
        UserRepresentation userRepresentation = KeycloakUserMapper.map(user);
        UserRepresentation finalUserRepresentation = userRepresentation;
        Response response = ResponseHelper.retryOnWrongStatusCode(() -> usersResource.create(finalUserRepresentation));
        if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
            throw new MailAlreadyExistsException();
        }
        ResponseHelper.checkCreateResponse(user.toString(), response);
        String userId = ResponseHelper.getIdFromLocation(response);

        UserResource userResource = usersResource.get(userId);
        userRepresentation = userResource.toRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userResource.update(userRepresentation);

        if (password == null || password.trim().isEmpty()) {
            userResource.executeActionsEmail(Collections.singletonList("UPDATE_PASSWORD"));
        } else {
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(password);
            credentialRepresentation.setTemporary(false);
            userResource.resetPassword(credentialRepresentation);
        }
        List<GroupRepresentation> joinedGroups = updateGroups(user, realm, usersResource, userId);
        List<RoleRepresentation> roles = userResource.roles().clientLevel(getClientUUID()).listEffective();
        return KeycloakUserMapper.map(userRepresentation, joinedGroups, roles);
    }

    /**
     * Updates the keycloak user with the new data in the given user object.
     */
    @Override
    public void updateUser(KeycloakUser user) throws UnsupportedLocaleException {
        RealmResource realm = getRealmResource();
        validateLocales(realm.toRepresentation(), user.getLocale());
        UsersResource usersResource = realm.users();
        UserResource userResource = usersResource.get(user.getId());
        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEmail(user.getEmail());
        KeycloakUserMapper.addLocaleToUserRepresentation(user, userRepresentation);
        userResource.update(userRepresentation);
        List<GroupRepresentation> groups = updateGroups(user, realm, usersResource, user.getId());
    }

    /**
     * Sets a new password for the user with the given keycloakUserId.
     */
    @Override
    public void updatePassword(String password, String keycloakUserId) {
        RealmResource realm = getRealmResource();
        UsersResource usersResource = realm.users();
        UserResource userResource = usersResource.get(keycloakUserId);
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);
        credentialRepresentation.setTemporary(false);
        userResource.resetPassword(credentialRepresentation);
    }

    /**
     * Triggers a new update password email for the user with the given keycloakUserId.
     */
    @Override
    public void forgotPassword(String keycloakId) {
        RealmResource realm = getRealmResource();
        UsersResource usersResource = realm.users();
        UserResource userResource = usersResource.get(keycloakId);
        userResource.executeActionsEmail(Collections.singletonList("UPDATE_PASSWORD"));
    }

    /**
     * Disables the given user. The user can than no longer login.
     */
    @Override
    public void disableUser(String keycloakUserId) {
        enableDisableUser(keycloakUserId, false);
    }

    /**
     * Enables the given user. The user can login again.
     */
    @Override
    public void enableUser(String keycloakUserId) {
        enableDisableUser(keycloakUserId, true);
    }


    private List<GroupRepresentation> updateGroups(KeycloakUser user, RealmResource realm, UsersResource usersResource, String userId) {
        List<GroupRepresentation> joinedGroups = new ArrayList<>();
        List<GroupRepresentation> realmGroups = realm.groups().groups();
        UserResource userResource = usersResource.get(userId);
        // First leave all groups, then join the ones selected
        userResource.groups().forEach(groupRepresentation -> {
            // We need to get a new userResource, otherwise only the first group is left
            UserResource userRes = usersResource.get(userId);
            userRes.leaveGroup(groupRepresentation.getId());
        });
        realmGroups.stream().filter(groupRepresentation -> user.getGroups().contains(groupRepresentation.getName())).forEach(groupRepresentation -> {
            // We need to get a new userResource, otherwise only the first group is joined
            UserResource userRes = usersResource.get(userId);
            userRes.joinGroup(groupRepresentation.getId());
            joinedGroups.add(groupRepresentation);
        });
        return joinedGroups;
    }

    private void enableDisableUser(String keycloakUserId, boolean enabled) {
        RealmResource realm = getRealmResource();
        UsersResource usersResource = realm.users();
        UserResource userResource = usersResource.get(keycloakUserId);
        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setEnabled(enabled);
        userResource.update(userRepresentation);
    }

    public UserResource getUserResource(String userId) {
        RealmResource realm = getRealmResource();
        return realm.users().get(userId);
    }

    private String getClientUUID() {
        return getClient().getId();
    }

    private ClientRepresentation getClient() {
        return getRealmResource().clients().findByClientId(keyClockConfig.getClientId()).get(0);
    }

    private void validateLocales(RealmRepresentation realmRepresentation, String givenLocale)
            throws UnsupportedLocaleException {
        if (givenLocale != null) {
            Set<String> supportedLocales = realmRepresentation.getSupportedLocales();
            if (!supportedLocales.contains(givenLocale)) {
                throw new UnsupportedLocaleException(supportedLocales);
            }
        }
    }
}
