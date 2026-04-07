package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.dto.Request.UsuarioRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.UsuarioResponseDTO;
import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioService service;

    @Test
    void criarDeveRetornarUsuarioQuandoEmailNaoExiste() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Leonardo");
        dto.setEmail("leo@email.com");
        dto.setSenha("123456");
        dto.setCargo("Admin");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome(dto.getNome());
        usuarioSalvo.setEmail(dto.getEmail());
        usuarioSalvo.setSenha(dto.getSenha());
        usuarioSalvo.setCargo(dto.getCargo());

        when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        UsuarioResponseDTO response = service.criar(dto);

        assertEquals(1L, response.getId());
        assertEquals("Leonardo", response.getNome());
        assertEquals("leo@email.com", response.getEmail());
        assertEquals("Admin", response.getCargo());
        verify(repository).save(any(Usuario.class));
    }

    @Test
    void criarDeveLancarExcecaoQuandoEmailJaExiste() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setEmail("duplicado@email.com");

        when(repository.existsByEmail(dto.getEmail())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.criar(dto));

        assertTrue(exception.getMessage().contains("email"));
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void listarDeveRetornarUsuariosMapeados() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setNome("Ana");
        usuario1.setEmail("ana@email.com");
        usuario1.setCargo("Analista");

        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setNome("Bruno");
        usuario2.setEmail("bruno@email.com");
        usuario2.setCargo("Gestor");

        when(repository.findAll()).thenReturn(List.of(usuario1, usuario2));

        List<UsuarioResponseDTO> response = service.listar();

        assertEquals(2, response.size());
        assertEquals("Ana", response.get(0).getNome());
        assertEquals("bruno@email.com", response.get(1).getEmail());
        assertEquals("Gestor", response.get(1).getCargo());
    }
}
