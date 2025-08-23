package com.shop.userservice.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class UserJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(source);

        var roles = source.getClaimAsStringList("spring_sec_roles");
        if (roles != null) {
            roles.stream()
                    .filter(role -> role.startsWith("app-"))
                    .map(appRole -> "ROLE_" + appRole.substring("app-".length()).toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        UserPrincipal userPrincipal = new UserPrincipal(
                UUID.fromString(source.getClaim("sub")),
                source.getClaim("preferred_username"),
                source.getClaim("email"),
                authorities
        );

        return new UsernamePasswordAuthenticationToken(userPrincipal, "n/a", authorities);
    }
}
