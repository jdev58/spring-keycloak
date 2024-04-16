package com.example.resource.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginResponse {
    String access_token;
    String token_type;
    String refresh_token;
    Long expires_in;
    String scope;

    Set<RoleEnum> roles;
}
