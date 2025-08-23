package com.shop.userservice.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;
import java.util.UUID;

@AllArgsConstructor
public class UserPrincipal  implements Principal {

    @Getter
    private UUID userUUID;

    private String username;

    @Getter
    private String email;

    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public String getName() {
        return username;
    }
}
