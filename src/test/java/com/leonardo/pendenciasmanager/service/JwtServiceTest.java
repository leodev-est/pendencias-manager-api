package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    void generateTokenDeveGerarTokenValidoComSubjectDoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail("leo@email.com");
        usuario.setSenha("senha-criptografada");

        String token = jwtService.generateToken(usuario);

        assertEquals("leo@email.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, usuario));
    }
}
