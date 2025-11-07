package app.nail.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import app.nail.common.exception.ApiException;
import app.nail.common.security.JwtKeyProvider;

/**
 * English:
 * - POST /api/auth/login with JSON { "username": "...", "password": "..." }.
 * - If ok, return a JWT containing username and roles.
 * - Client sends it as "Authorization: Bearer <token>" for subsequent requests.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "JWT login APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtKeyProvider jwtKeyProvider;
    private final long ttlSeconds;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtKeyProvider jwtKeyProvider,
                          @Value("${app.security.jwt.ttl-seconds:36000}") long ttlSeconds) {
        this.authenticationManager = authenticationManager;
        this.jwtKeyProvider = jwtKeyProvider;
        this.ttlSeconds = ttlSeconds;
    }

    public static record LoginRequest(String username, String password, Long userId) {}  // English: minimal request DTO
    public static record TokenResponse(String token) {}                      // English: minimal response DTO

    @PostMapping("/login")
    @Operation(summary = "Authenticate with username/password")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        if (req.userId() == null) {
            throw ApiException.businessViolation("userId is required");
        }
        List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .map(a -> a.replaceFirst("^ROLE_", "")) // English: strip ROLE_ prefix
            .toList();

        Instant now = Instant.now();
        String token = Jwts.builder()
            .setSubject(auth.getName())
            .claim("roles", roles)
            .claim("userId", req.userId())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
            .signWith(jwtKeyProvider.getKey(), SignatureAlgorithm.HS256)
            .compact();

        return ResponseEntity.ok(new TokenResponse(token));
    }
}
