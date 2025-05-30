package com.cesde.proyecto_integrador.security;

import com.cesde.proyecto_integrador.model.User;
import com.cesde.proyecto_integrador.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                 @NonNull HttpServletResponse response, 
                                 @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String username;

        // Verificar si el encabezado de autorización es nulo o no comienza con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token del encabezado
        token = authHeader.substring(7);
        
        try {
            // Extraer el nombre de usuario del token
            username = jwtUtil.extractUsername(token);
            
            // Si el nombre de usuario no es nulo y no hay autenticación en el contexto actual
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Buscar al usuario en la base de datos
                User user = userRepository.findByEmail(username);
                
                // Si el token es válido, configurar la autenticación
                if (jwtUtil.isTokenValid(token)) {
                    // Crear un objeto de autenticación con el usuario y sus roles
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                    );
                    
                    // Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // En caso de error, limpiar el contexto de seguridad
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
            return;
        }
        
        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
