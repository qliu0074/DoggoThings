package app.nail.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * English:
 * - POST /api/auth/login with JSON { "username": "...", "password": "..." }.
 * - If ok, return a JWT containing username and roles.
 * - Client sends it as "Authorization: Bearer <token>" for subsequent requests.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final Key key;
    private final long ttlSeconds;

    public AuthController(AuthenticationManager authenticationManager,
                          @Value("${app.security.jwt.secret}") String secret,
                          @Value("${app.security.jwt.ttl-seconds:36000}") long ttlSeconds) {
        this.authenticationManager = authenticationManager;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public static record LoginRequest(String username, String password) {}  // English: minimal request DTO
    public static record TokenResponse(String token) {}                      // English: minimal response DTO

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .map(a -> a.replaceFirst("^ROLE_", "")) // English: strip ROLE_ prefix
            .toList();

        Instant now = Instant.now();
        String token = Jwts.builder()
            .setSubject(auth.getName())
            .claim("roles", roles)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        return ResponseEntity.ok(new TokenResponse(token));
    }
}
