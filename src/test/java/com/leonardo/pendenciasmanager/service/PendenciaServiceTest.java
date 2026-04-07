package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.dto.Request.PendenciaRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.PendenciaResponseDTO;
import com.leonardo.pendenciasmanager.entity.Pendencia;
import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.repository.PendenciaRepository;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void criarDeveSalvarPendenciaQuandoResponsavelExiste() {
        Usuario responsavel = criarUsuario(10L, "Maria");
        PendenciaRequestDTO dto = criarPendenciaRequest();

        Pendencia pendenciaSalva = criarPendencia(1L, dto, responsavel);

        when(usuarioRepository.findById(dto.getResponsavelId())).thenReturn(Optional.of(responsavel));
        when(pendenciaRepository.save(any(Pendencia.class))).thenReturn(pendenciaSalva);

        PendenciaResponseDTO response = service.criar(dto);

        assertEquals(1L, response.getId());
        assertEquals(dto.getTitulo(), response.getTitulo());
        assertEquals(dto.getStatus(), response.getStatus());
        assertEquals(responsavel.getId(), response.getResponsavelId());
        assertEquals("Maria", response.getResponsavelNome());
    }

    @Test
    void criarDeveLancarExcecaoQuandoResponsavelNaoExiste() {
        PendenciaRequestDTO dto = criarPendenciaRequest();
        when(usuarioRepository.findById(dto.getResponsavelId())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.criar(dto));

        assertTrue(exception.getMessage().toLowerCase().contains("respons"));
        verify(pendenciaRepository, never()).save(any(Pendencia.class));
    }

    @Test
    void listarDeveRetornarPendenciasMapeadas() {
        Usuario responsavel = criarUsuario(10L, "Maria");
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), responsavel);

        when(pendenciaRepository.findAll()).thenReturn(List.of(pendencia));

        List<PendenciaResponseDTO> response = service.listar();

        assertEquals(1, response.size());
        assertEquals("Corrigir bug", response.get(0).getTitulo());
        assertEquals("Maria", response.get(0).getResponsavelNome());
    }

    @Test
    void listarPorStatusDeveRetornarPendenciasFiltradas() {
        Usuario responsavel = criarUsuario(10L, "Maria");
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), responsavel);

        when(pendenciaRepository.findByStatus(StatusPendencia.PENDENTE)).thenReturn(List.of(pendencia));

        List<PendenciaResponseDTO> response = service.listarPorStatus(StatusPendencia.PENDENTE);

        assertEquals(1, response.size());
        assertEquals(StatusPendencia.PENDENTE, response.get(0).getStatus());
    }

    @Test
    void listarPorResponsavelDeveRetornarPendenciasDoResponsavel() {
        Usuario responsavel = criarUsuario(10L, "Maria");
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), responsavel);

        when(pendenciaRepository.findByResponsavelId(10L)).thenReturn(List.of(pendencia));

        List<PendenciaResponseDTO> response = service.listarPorResponsavel(10L);

        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).getResponsavelId());
    }

    @Test
    void listarVencidasDeveConsultarDataAtualEStatusConcluida() {
        Usuario responsavel = criarUsuario(10L, "Maria");
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), responsavel);

        when(pendenciaRepository.findByDataVencimentoBeforeAndStatusNot(any(LocalDate.class), any(StatusPendencia.class)))
                .thenReturn(List.of(pendencia));

        List<PendenciaResponseDTO> response = service.listarVencidas();

        ArgumentCaptor<LocalDate> dataCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<StatusPendencia> statusCaptor = ArgumentCaptor.forClass(StatusPendencia.class);
        verify(pendenciaRepository).findByDataVencimentoBeforeAndStatusNot(dataCaptor.capture(), statusCaptor.capture());

        assertNotNull(dataCaptor.getValue());
        assertEquals(StatusPendencia.CONCLUIDA, statusCaptor.getValue());
        assertEquals(1, response.size());
    }

    @Test
    void listarProximos7DiasDeveConsultarIntervaloCorreto() {
        Usuario responsavel = criarUsuario(10L, "Maria");
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), responsavel);

        when(pendenciaRepository.findByDataVencimentoBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(pendencia));

        List<PendenciaResponseDTO> response = service.listarProximos7Dias();

        ArgumentCaptor<LocalDate> inicioCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> fimCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(pendenciaRepository).findByDataVencimentoBetween(inicioCaptor.capture(), fimCaptor.capture());

        assertEquals(inicioCaptor.getValue().plusDays(7), fimCaptor.getValue());
        assertEquals(1, response.size());
    }

    @Test
    void buscarPorIdDeveRetornarPendenciaQuandoExistir() {
        Usuario responsavel = criarUsuario(10L, "Maria");
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), responsavel);

        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendencia));

        PendenciaResponseDTO response = service.buscarPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("Maria", response.getResponsavelNome());
    }

    @Test
    void buscarPorIdDeveLancarExcecaoQuandoNaoExistir() {
        when(pendenciaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.buscarPorId(99L));

        assertTrue(exception.getMessage().toLowerCase().contains("pend"));
    }

    @Test
    void atualizarDeveSalvarPendenciaAtualizada() {
        Usuario responsavelAntigo = criarUsuario(1L, "Joao");
        Usuario novoResponsavel = criarUsuario(2L, "Maria");
        Pendencia pendenciaExistente = criarPendencia(1L, criarPendenciaRequest(), responsavelAntigo);
        PendenciaRequestDTO dto = criarPendenciaRequest();
        dto.setResponsavelId(2L);
        dto.setTitulo("Atualizada");

        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendenciaExistente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(novoResponsavel));
        when(pendenciaRepository.save(any(Pendencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PendenciaResponseDTO response = service.atualizar(1L, dto);

        assertEquals("Atualizada", response.getTitulo());
        assertEquals(2L, response.getResponsavelId());
        assertEquals("Maria", response.getResponsavelNome());
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoPendenciaNaoExistir() {
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.atualizar(1L, criarPendenciaRequest()));

        assertTrue(exception.getMessage().toLowerCase().contains("pend"));
        verify(usuarioRepository, never()).findById(any(Long.class));
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoResponsavelNaoExistir() {
        Pendencia pendenciaExistente = criarPendencia(1L, criarPendenciaRequest(), criarUsuario(1L, "Joao"));

        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendenciaExistente));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.atualizar(1L, criarPendenciaRequest()));

        assertTrue(exception.getMessage().toLowerCase().contains("respons"));
        verify(pendenciaRepository, never()).save(any(Pendencia.class));
    }

    @Test
    void deletarDeveRemoverPendenciaQuandoExistir() {
        Pendencia pendencia = criarPendencia(1L, criarPendenciaRequest(), criarUsuario(10L, "Maria"));
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.of(pendencia));

        service.deletar(1L);

        verify(pendenciaRepository).delete(pendencia);
    }

    @Test
    void deletarDeveLancarExcecaoQuandoPendenciaNaoExistir() {
        when(pendenciaRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.deletar(1L));

        assertTrue(exception.getMessage().toLowerCase().contains("pend"));
        verify(pendenciaRepository, never()).delete(any(Pendencia.class));
    }

    private PendenciaRequestDTO criarPendenciaRequest() {
        PendenciaRequestDTO dto = new PendenciaRequestDTO();
        dto.setTitulo("Corrigir bug");
        dto.setDescricao("Ajustar fluxo de cadastro");
        dto.setStatus(StatusPendencia.PENDENTE);
        dto.setDataVencimento(LocalDate.of(2026, 4, 10));
        dto.setPrioridade("Alta");
        dto.setOrigem("Sistema");
        dto.setResponsavelId(10L);
        return dto;
    }

    private Usuario criarUsuario(Long id, String nome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(nome.toLowerCase() + "@email.com");
        usuario.setCargo("Analista");
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
        pendencia.setResponsavel(responsavel);
        return pendencia;
    }
}
