package com.guardianvision.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET = "ySGpC9iWzNYzCqEQk7btGmEsKb14M2g5dxv6m6vWbfpQAw2DFjWn3z8DfrWyW9Z8Yq345kFycjLFg0drbbbwEg==";
    private static final long EXPIRATION_MS = 86400000; // 1 day

    public static String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public static String getRoleFromToken(String token) {
        return (String) getAllClaimsFromToken(token).get("role");
    }

    private static Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}
