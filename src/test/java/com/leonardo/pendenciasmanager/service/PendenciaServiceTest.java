package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.dto.Request.PendenciaRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.PageResponseDTO;
import com.leonardo.pendenciasmanager.dto.Response.PendenciaResponseDTO;
import com.leonardo.pendenciasmanager.entity.Pendencia;
import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.enums.Role;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.repository.PendenciaRepository;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendenciaServiceTest {

    @Mock
    private PendenciaRepository pendenciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PendenciaService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void criarDeveSalvarPendenciaParaUsuarioAutenticado() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        PendenciaRequestDTO dto = criarPendenciaRequest();
        Pendencia pendenciaSalva = criarPendencia(1L, dto, usuarioAutenticado);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.save(any(Pendencia.class))).thenReturn(pendenciaSalva);

        PendenciaResponseDTO response = service.criar(dto);

        assertEquals(1L, response.getId());
        assertEquals(usuarioAutenticado.getId(), response.getResponsavelId());
        assertEquals("Maria", response.getResponsavelNome());
    }

    @Test
    void criarDeveLancarExcecaoQuandoUsuarioAutenticadoNaoExistir() {
        PendenciaRequestDTO dto = criarPendenciaRequest();
        autenticar("maria@email.com");
        when(usuarioRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> service.criar(dto));

        assertTrue(exception.getMessage().toLowerCase().contains("autenticado"));
        verify(pendenciaRepository, never()).save(any(Pendencia.class));
    }

    @Test
    void listarDeveRetornarPaginaDePendenciasDoUsuarioAutenticado() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), usuarioAutenticado);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pendencia)));

        PageResponseDTO<PendenciaResponseDTO> response = service.listar(0, 10, "createdAt,desc", null, null, null, null, null);

        assertEquals(1, response.getContent().size());
        assertEquals("Maria", response.getContent().get(0).getResponsavelNome());
        assertEquals(1, response.getTotalElements());
        assertEquals(LocalDateTime.of(2026, 4, 7, 10, 30, 0), response.getContent().get(0).getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 4, 7, 11, 0, 0), response.getContent().get(0).getUpdatedAt());
    }

    @Test
    void listarDeveAplicarPaginacaoOrdenacaoEFiltrosCombinados() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), usuarioAutenticado);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pendencia)));

        PageResponseDTO<PendenciaResponseDTO> response = service.listar(
                0,
                5,
                "dataLimite,asc",
                StatusPendencia.PENDENTE,
                "Alta",
                "cliente",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(pendenciaRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), pageableCaptor.capture());

        assertEquals(1, response.getContent().size());
        assertEquals(StatusPendencia.PENDENTE, response.getContent().get(0).getStatus());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
        assertEquals("dataVencimento: ASC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void listarDeveLancarExcecaoQuandoPageForInvalida() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.listar(-1, 10, "createdAt,desc", null, null, null, null, null)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("page"));
    }

    @Test
    void listarDeveLancarExcecaoQuandoIntervaloDeDatasForInvalido() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.listar(
                        0,
                        10,
                        "createdAt,desc",
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 4, 30),
                        LocalDate.of(2026, 4, 1)
                )
        );

        assertTrue(exception.getMessage().toLowerCase().contains("datainicio"));
    }

    @Test
    void listarDeveLancarExcecaoQuandoCampoDeOrdenacaoForInvalido() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.listar(0, 10, "responsavel,asc", null, null, null, null, null)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("ordenacao"));
    }

    @Test
    void listarVencidasDeveConsultarDataAtualEStatusConcluidaDoUsuarioAutenticado() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), usuarioAutenticado);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findByResponsavelIdAndDataVencimentoBeforeAndStatusNot(any(Long.class), any(LocalDate.class), any(StatusPendencia.class)))
                .thenReturn(List.of(pendencia));

        List<PendenciaResponseDTO> response = service.listarVencidas();

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDate> dataCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<StatusPendencia> statusCaptor = ArgumentCaptor.forClass(StatusPendencia.class);
        verify(pendenciaRepository).findByResponsavelIdAndDataVencimentoBeforeAndStatusNot(
                idCaptor.capture(),
                dataCaptor.capture(),
                statusCaptor.capture()
        );

        assertEquals(10L, idCaptor.getValue());
        assertNotNull(dataCaptor.getValue());
        assertEquals(StatusPendencia.CONCLUIDA, statusCaptor.getValue());
        assertEquals(1, response.size());
    }

    @Test
    void listarProximos7DiasDeveConsultarIntervaloCorretoDoUsuarioAutenticado() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), usuarioAutenticado);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findByResponsavelIdAndDataVencimentoBetween(any(Long.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(pendencia));

        List<PendenciaResponseDTO> response = service.listarProximos7Dias();

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDate> inicioCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> fimCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(pendenciaRepository).findByResponsavelIdAndDataVencimentoBetween(
                idCaptor.capture(),
                inicioCaptor.capture(),
                fimCaptor.capture()
        );

        assertEquals(10L, idCaptor.getValue());
        assertEquals(inicioCaptor.getValue().plusDays(7), fimCaptor.getValue());
        assertEquals(1, response.size());
    }

    @Test
    void buscarPorIdDeveRetornarPendenciaQuandoPertencerAoUsuarioAutenticado() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), usuarioAutenticado);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendencia));

        PendenciaResponseDTO response = service.buscarPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("Maria", response.getResponsavelNome());
    }

    @Test
    void buscarPorIdDeveLancarExcecaoQuandoPendenciaNaoExistir() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.buscarPorId(99L));

        assertTrue(exception.getMessage().toLowerCase().contains("pendencia"));
    }

    @Test
    void buscarPorIdDeveLancarExcecaoQuandoPendenciaForDeOutroUsuario() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Usuario outroUsuario = criarUsuario(20L, "Joao", "joao@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), outroUsuario);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendencia));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> service.buscarPorId(1L));

        assertTrue(exception.getMessage().toLowerCase().contains("acesso negado"));
    }

    @Test
    void atualizarDeveSalvarPendenciaAtualizadaQuandoPertencerAoUsuarioAutenticado() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Pendencia pendenciaExistente = criarPendencia(1L, criarPendenciaRequest(), usuarioAutenticado);
        PendenciaRequestDTO dto = criarPendenciaRequest();
        dto.setTitulo("Atualizada");

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendenciaExistente));
        when(pendenciaRepository.save(any(Pendencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PendenciaResponseDTO response = service.atualizar(1L, dto);

        assertEquals("Atualizada", response.getTitulo());
        assertEquals(10L, response.getResponsavelId());
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoPendenciaForDeOutroUsuario() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Usuario outroUsuario = criarUsuario(20L, "Joao", "joao@email.com", Role.USER);
        Pendencia pendenciaExistente = criarPendencia(1L, criarPendenciaRequest(), outroUsuario);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendenciaExistente));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.atualizar(1L, criarPendenciaRequest())
        );

        assertTrue(exception.getMessage().toLowerCase().contains("acesso negado"));
        verify(pendenciaRepository, never()).save(any(Pendencia.class));
    }

    @Test
    void deletarDeveRemoverPendenciaQuandoPertencerAoUsuarioAutenticado() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), usuarioAutenticado);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendencia));

        service.deletar(1L);

        verify(pendenciaRepository).delete(pendencia);
    }

    @Test
    void deletarDeveLancarExcecaoQuandoPendenciaForDeOutroUsuario() {
        Usuario usuarioAutenticado = criarUsuario(10L, "Maria", "maria@email.com", Role.USER);
        Usuario outroUsuario = criarUsuario(20L, "Joao", "joao@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), outroUsuario);

        autenticar(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail())).thenReturn(Optional.of(usuarioAutenticado));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendencia));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> service.deletar(1L));

        assertTrue(exception.getMessage().toLowerCase().contains("acesso negado"));
        verify(pendenciaRepository, never()).delete(any(Pendencia.class));
    }

    private void autenticar(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of())
        );
    }

    private PendenciaRequestDTO criarPendenciaRequest() {
        PendenciaRequestDTO dto = new PendenciaRequestDTO();
        dto.setTitulo("Corrigir bug");
        dto.setDescricao("Ajustar fluxo de cadastro");
        dto.setStatus(StatusPendencia.PENDENTE);
        dto.setDataVencimento(LocalDate.of(2026, 4, 10));
        dto.setPrioridade("Alta");
        dto.setOrigem("Sistema");
        return dto;
    }

    @Test
    void listarDeveRetornarTodasAsPendenciasQuandoUsuarioForAdmin() {
        Usuario admin = criarUsuario(99L, "Admin", "admin@email.com", Role.ADMIN);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), criarUsuario(10L, "Maria", "maria@email.com", Role.USER));

        autenticar(admin.getEmail());
        when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(pendenciaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pendencia)));

        PageResponseDTO<PendenciaResponseDTO> response = service.listar(0, 10, "createdAt,desc", null, null, null, null, null);

        assertEquals(1, response.getContent().size());
        assertEquals("Maria", response.getContent().get(0).getResponsavelNome());
    }

    @Test
    void buscarPorIdDevePermitirAcessoQuandoUsuarioForAdmin() {
        Usuario admin = criarUsuario(99L, "Admin", "admin@email.com", Role.ADMIN);
        Usuario outroUsuario = criarUsuario(20L, "Joao", "joao@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), outroUsuario);

        autenticar(admin.getEmail());
        when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendencia));

        PendenciaResponseDTO response = service.buscarPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("Joao", response.getResponsavelNome());
    }

    @Test
    void atualizarDevePermitirAtualizacaoQuandoUsuarioForAdmin() {
        Usuario admin = criarUsuario(99L, "Admin", "admin@email.com", Role.ADMIN);
        Usuario outroUsuario = criarUsuario(20L, "Joao", "joao@email.com", Role.USER);
        Pendencia pendenciaExistente = criarPendencia(1L, criarPendenciaRequest(), outroUsuario);
        PendenciaRequestDTO dto = criarPendenciaRequest();
        dto.setTitulo("Atualizada pelo admin");

        autenticar(admin.getEmail());
        when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendenciaExistente));
        when(pendenciaRepository.save(any(Pendencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PendenciaResponseDTO response = service.atualizar(1L, dto);

        assertEquals("Atualizada pelo admin", response.getTitulo());
        assertEquals("Joao", response.getResponsavelNome());
    }

    @Test
    void deletarDevePermitirRemocaoQuandoUsuarioForAdmin() {
        Usuario admin = criarUsuario(99L, "Admin", "admin@email.com", Role.ADMIN);
        Usuario outroUsuario = criarUsuario(20L, "Joao", "joao@email.com", Role.USER);
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), outroUsuario);

        autenticar(admin.getEmail());
        when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendencia));

        service.deletar(1L);

        verify(pendenciaRepository).delete(pendencia);
    }

    private Usuario criarUsuario(Long id, String nome, String email, Role role) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setCargo("Analista");
        usuario.setRole(role);
        return usuario;
    }

    private Pendencia criarPendencia(Long id, PendenciaRequestDTO dto, Usuario responsavel) {
        Pendencia pendencia = new Pendencia();
        pendencia.setId(id);
        pendencia.setTitulo(dto.getTitulo());
        pendencia.setDescricao(dto.getDescricao());
        pendencia.setStatus(dto.getStatus());
        pendencia.setDataVencimento(dto.getDataVencimento());
        pendencia.setPrioridade(dto.getPrioridade());
        pendencia.setOrigem(dto.getOrigem());
        pendencia.setCreatedAt(LocalDateTime.of(2026, 4, 7, 10, 30, 0));
        pendencia.setUpdatedAt(LocalDateTime.of(2026, 4, 7, 11, 0, 0));
        pendencia.setResponsavel(responsavel);
        return pendencia;
    }
}
