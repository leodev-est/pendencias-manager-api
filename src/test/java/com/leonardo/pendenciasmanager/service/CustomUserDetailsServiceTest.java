package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameDeveRetornarUsuarioQuandoEncontrado() {
        Usuario usuario = new Usuario();
        usuario.setEmail("leo@email.com");
        usuario.setSenha("senha-criptografada");

        when(usuarioRepository.findByEmail("leo@email.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("leo@email.com");

        assertEquals("leo@email.com", userDetails.getUsername());
    }

    @Test
    void loadUserByUsernameDeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("naoexiste@email.com"));
    }
}
