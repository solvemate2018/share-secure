package com.sharesecure.sharesecure.security.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.sharesecure.sharesecure.security.services.UserDetailsImpl;

import java.util.Date;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private SecretKey jwtSecret = Jwts.SIG.HS512.key().build();

    private int jwtExpirationMs = 2000000;

    @Value("${server.port}")
    private int serverPort;

    @Value("${server.url}")
    private String serverURL;

    private final String jwtIssuer = serverURL + ":"+ serverPort + "/";

    private final JwtParser parser = Jwts.parser().requireIssuer(jwtIssuer).verifyWith(jwtSecret)
    .build();

    public String generateJwtToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        
        Date iss = new Date();

        Date exp = new Date((new Date()).getTime() + jwtExpirationMs);

        String JWT = Jwts.builder()
        .issuer(jwtIssuer)
        .claims()
        .subject(userPrincipal.getUsername())
        .issuedAt(iss)
        .expiration(exp)
        .and()
        .signWith(jwtSecret)
        .compact();

        log.info("JWT generated:" + JWT);

        return JWT;
    }

    public String getUserEmailFromJwtToken(String token){
        String tokenPayload = parser.parseSignedClaims(token).getPayload().toString();

        String[] payloadItems = tokenPayload.split(", ");

        String result = null;
        for (int i = 0; i < payloadItems.length; i++) {
            if(payloadItems[i].startsWith("sub=")){
                result = payloadItems[i].substring(4);
            }
        }
        return result;
    }

    public boolean validateJwtToken(String token) {
		try {
            // file deepcode ignore JwtVerificationBypass: <please specify a reason of ignoring this>
            parser.parse(token);
            log.info("JWT Valid");
			return true;
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}
}

