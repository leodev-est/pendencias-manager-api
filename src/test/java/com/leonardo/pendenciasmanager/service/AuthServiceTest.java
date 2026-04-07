package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.dto.Request.AuthLoginRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.AuthResponseDTO;
import com.leonardo.pendenciasmanager.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginDeveRetornarTokenQuandoCredenciaisForemValidas() {
        AuthLoginRequestDTO request = new AuthLoginRequestDTO();
        request.setEmail("leo@email.com");
        request.setSenha("123456");

        Usuario usuario = new Usuario();
        usuario.setEmail("leo@email.com");
        usuario.setSenha("senha-criptografada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);
        when(jwtService.generateToken(usuario)).thenReturn("jwt-token");

        AuthResponseDTO response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
    }
}
