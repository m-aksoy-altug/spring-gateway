package com.api.gateway.utl;


import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {
	
    private String secret="secrets";

    @SuppressWarnings("deprecation")
	public Claims getALlClaims(String token) {  
    	return Jwts.parserBuilder().setSigningKey(secret).build()
        .parseClaimsJws(token).getBody();
    }

    private boolean isTokenExpired(String token ) {
        return this.getALlClaims(token).getExpiration().before(new Date());
    }

    public boolean isInvalid(String token) {
        return this.isTokenExpired(token);
    }
    
    
}