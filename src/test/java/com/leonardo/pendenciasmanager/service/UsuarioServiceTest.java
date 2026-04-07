package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.dto.Request.UsuarioRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.UsuarioResponseDTO;
import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.enums.Role;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.repository.PendenciaRepository;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

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

    @Mock
    private PendenciaRepository pendenciaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    void criarDeveRetornarUsuarioQuandoEmailNaoExiste() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Leonardo");
        dto.setEmail("leo@email.com");
        dto.setSenha("123456");
        dto.setCargo("Admin");
        dto.setRole(Role.ADMIN);

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome(dto.getNome());
        usuarioSalvo.setEmail(dto.getEmail());
        usuarioSalvo.setSenha(dto.getSenha());
        usuarioSalvo.setCargo(dto.getCargo());
        usuarioSalvo.setRole(dto.getRole());

        when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("senha-criptografada");
        when(repository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        UsuarioResponseDTO response = service.criar(dto);

        assertEquals(1L, response.getId());
        assertEquals("Leonardo", response.getNome());
        assertEquals("leo@email.com", response.getEmail());
        assertEquals("Admin", response.getCargo());
        assertEquals(Role.ADMIN, response.getRole());
        verify(repository).save(any(Usuario.class));
    }

    @Test
    void criarDeveAtribuirRoleUserQuandoNaoInformada() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Leonardo");
        dto.setEmail("leo@email.com");
        dto.setSenha("123456");

        when(repository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("senha-criptografada");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponseDTO response = service.criar(dto);

        assertEquals(Role.USER, response.getRole());
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
        usuario1.setRole(Role.USER);

        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setNome("Bruno");
        usuario2.setEmail("bruno@email.com");
        usuario2.setCargo("Gestor");
        usuario2.setRole(Role.ADMIN);

        when(repository.findAll()).thenReturn(List.of(usuario1, usuario2));

        List<UsuarioResponseDTO> response = service.listar();

        assertEquals(2, response.size());
        assertEquals("Ana", response.get(0).getNome());
        assertEquals("bruno@email.com", response.get(1).getEmail());
        assertEquals("Gestor", response.get(1).getCargo());
        assertEquals(Role.ADMIN, response.get(1).getRole());
    }

    @Test
    void buscarPorIdDeveRetornarUsuarioQuandoExistir() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana");
        usuario.setEmail("ana@email.com");
        usuario.setCargo("Analista");
        usuario.setRole(Role.USER);

        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponseDTO response = service.buscarPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("Ana", response.getNome());
        assertEquals("ana@email.com", response.getEmail());
    }

    @Test
    void buscarPorIdDeveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.buscarPorId(99L));

        assertTrue(exception.getMessage().toLowerCase().contains("usuario"));
    }

    @Test
    void atualizarDeveAtualizarUsuarioQuandoEmailNaoPertencerAOutroUsuario() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Ana Maria");
        dto.setEmail("ana@email.com");
        dto.setSenha("nova-senha");
        dto.setCargo("Senior");
        dto.setRole(Role.ADMIN);

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);
        usuarioExistente.setNome("Ana");
        usuarioExistente.setEmail("ana@email.com");
        usuarioExistente.setSenha("senha-antiga");
        usuarioExistente.setCargo("Analista");
        usuarioExistente.setRole(Role.USER);

        when(repository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(repository.findByEmail(dto.getEmail())).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("senha-criptografada");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponseDTO response = service.atualizar(1L, dto);

        assertEquals("Ana Maria", response.getNome());
        assertEquals("ana@email.com", response.getEmail());
        assertEquals("Senior", response.getCargo());
        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void atualizarDeveManterRoleAtualQuandoNaoInformada() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Ana Maria");
        dto.setEmail("ana@email.com");
        dto.setSenha("nova-senha");
        dto.setCargo("Senior");

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);
        usuarioExistente.setNome("Ana");
        usuarioExistente.setEmail("ana@email.com");
        usuarioExistente.setSenha("senha-antiga");
        usuarioExistente.setCargo("Analista");
        usuarioExistente.setRole(Role.ADMIN);

        when(repository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(repository.findByEmail(dto.getEmail())).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("senha-criptografada");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponseDTO response = service.atualizar(1L, dto);

        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.atualizar(1L, new UsuarioRequestDTO()));

        assertTrue(exception.getMessage().toLowerCase().contains("usuario"));
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoEmailJaPertencerAOutroUsuario() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setEmail("duplicado@email.com");

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);

        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setEmail("duplicado@email.com");

        when(repository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(repository.findByEmail(dto.getEmail())).thenReturn(Optional.of(outroUsuario));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.atualizar(1L, dto));

        assertTrue(exception.getMessage().contains("email"));
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void deletarDeveRemoverUsuarioQuandoNaoPossuirPendencias() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pendenciaRepository.existsByResponsavelId(1L)).thenReturn(false);

        service.deletar(1L);

        verify(repository).delete(usuario);
    }

    @Test
    void deletarDeveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.deletar(1L));

        assertTrue(exception.getMessage().toLowerCase().contains("usuario"));
        verify(repository, never()).delete(any(Usuario.class));
    }

    @Test
    void deletarDeveLancarExcecaoQuandoUsuarioPossuirPendencias() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pendenciaRepository.existsByResponsavelId(1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.deletar(1L));

        assertTrue(exception.getMessage().toLowerCase().contains("pendencias"));
        verify(repository, never()).delete(any(Usuario.class));
    }
}
