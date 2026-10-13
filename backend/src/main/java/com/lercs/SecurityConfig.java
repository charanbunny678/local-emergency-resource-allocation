package com.lercs;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
class JwtService {
 private final SecretKey key;private final long expiration;
 JwtService(@Value("${lercs.jwt.secret}")String secret,@Value("${lercs.jwt.expiration-ms}")long expiration){this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expiration=expiration;}
 String create(User u){return Jwts.builder().subject(u.email).claim("role",u.role.name()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiration)).signWith(key).compact();}
 Claims parse(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();}
}

@Component
class JwtFilter extends OncePerRequestFilter {
 private final JwtService jwt;private final Users users;JwtFilter(JwtService jwt,Users users){this.jwt=jwt;this.users=users;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String h=req.getHeader("Authorization");if(h!=null&&h.startsWith("Bearer "))try{Claims c=jwt.parse(h.substring(7));User user=users.findByEmail(c.getSubject()).filter(u->u.active).orElseThrow();var auth=new UsernamePasswordAuthenticationToken(user.email,null,List.of(new SimpleGrantedAuthority("ROLE_"+user.role.name())));org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);}catch(JwtException|IllegalArgumentException|java.util.NoSuchElementException ex){org.springframework.security.core.context.SecurityContextHolder.clearContext();}
  chain.doFilter(req,res);
 }
}

@Configuration @EnableWebSecurity @EnableMethodSecurity
class SecurityConfig {
 @Bean BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean Jackson2ObjectMapperBuilderCustomizer fieldSerialization(){return builder->builder.visibility(PropertyAccessor.FIELD,JsonAutoDetect.Visibility.ANY);}
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,JwtFilter jwtFilter)throws Exception{return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->{res.setStatus(401);res.setContentType("application/json");res.getWriter().write("{\"success\":false,\"message\":\"Authentication required\",\"errors\":{},\"timestamp\":\""+java.time.Instant.now()+"\"}");}).accessDeniedHandler((req,res,ex)->{res.setStatus(403);res.setContentType("application/json");res.getWriter().write("{\"success\":false,\"message\":\"Access denied\",\"errors\":{},\"timestamp\":\""+java.time.Instant.now()+"\"}");})).authorizeHttpRequests(a->a.requestMatchers("/api/auth/register","/api/auth/login","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html","/actuator/health").permitAll().requestMatchers(HttpMethod.GET,"/api/shelters/**","/api/hospitals/**","/api/announcements/active").permitAll().anyRequest().authenticated()).addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class).build();}
 @Bean CorsConfigurationSource corsConfigurationSource(@Value("${lercs.cors-origin}")String origin){CorsConfiguration c=new CorsConfiguration();List<String> origins=Arrays.stream(origin.split(",")).map(String::trim).toList();c.setAllowedOriginPatterns(origins);c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("*"));c.setAllowCredentials(true);UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
}
