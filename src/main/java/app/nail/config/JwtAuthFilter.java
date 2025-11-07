package app.nail.config;

import app.nail.common.security.JwtKeyProvider;
import app.nail.common.security.PrincipalUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * English: Stateless JWT parser. Builds Authentication from subject and roles.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtKeyProvider jwtKeyProvider;

    public JwtAuthFilter(JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtKeyProvider.getKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                Object userIdRaw = claims.get("userId");
                Long userId = null;
                if (userIdRaw instanceof Number number) {
                    userId = number.longValue();
                }
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                Collection<SimpleGrantedAuthority> authorities = roles == null ? List.of()
                        : roles.stream()
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                String actorRole = (roles == null || roles.isEmpty()) ? null : roles.get(0);
                PrincipalUser principal = new PrincipalUser(userId, actorRole);
                var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                SecurityContextHolder.clearContext(); // English: invalid token -> anonymous
            }
        }

        filterChain.doFilter(request, response);
    }
}
