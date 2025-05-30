package com.kss.astrologer.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {
    public static final long JWT_TOKEN_VALIDITY = 60 * 60 * 24 * 30;
    public static final String SECRET = "S1NTS3VzaGFsQEFzdHJvR3VydUAxMjM0I1NwcmluZ19Cb290ITEyM0BEZXZlbG9wZXI=";

    public String generateToken(String mobile){
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, mobile);
    }
        
    private String createToken(Map<String, Object> claims, String mobile) {
        return Jwts.builder()
                .claims(claims)
                .subject(mobile)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY *1000 ))
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractMobile(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
        
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
        
    }
        
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractMobile(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
