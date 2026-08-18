package com.apihub.common.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtUtilTest {

    @Test
    void createTokenAndParseShouldRoundTripUserClaims() {
        JwtUtil jwtUtil = new JwtUtil("0123456789abcdef0123456789abcdef", 3600L);

        String token = jwtUtil.createToken(42L, "alice", List.of("ADMIN", "USER"));
        Claims claims = jwtUtil.parse(token);

        assertNotNull(token);
        assertEquals("42", claims.getSubject());
        assertEquals("alice", claims.get("username", String.class));
        assertEquals(List.of("ADMIN", "USER"), claims.get("roles", List.class));
        assertEquals(42L, jwtUtil.getUserId(claims));
    }
}
