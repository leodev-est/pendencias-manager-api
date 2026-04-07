package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.dto.Request.PendenciaRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.PendenciaResponseDTO;
import com.leonardo.pendenciasmanager.entity.Pendencia;
import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.repository.PendenciaRepository;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PendenciaService {

    @Autowired
    private PendenciaRepository pendenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public PendenciaResponseDTO criar(PendenciaRequestDTO dto) {
        Usuario usuarioAutenticado = getUsuarioAutenticado();

        Pendencia pendencia = new Pendencia();
        pendencia.setTitulo(dto.getTitulo());
        pendencia.setDescricao(dto.getDescricao());
        pendencia.setStatus(dto.getStatus());
        pendencia.setDataVencimento(dto.getDataVencimento());
        pendencia.setPrioridade(dto.getPrioridade());
        pendencia.setOrigem(dto.getOrigem());
        pendencia.setResponsavel(usuarioAutenticado);

        return toResponseDTO(pendenciaRepository.save(pendencia));
    }

    public List<PendenciaResponseDTO> listar() {
        Usuario usuarioAutenticado = getUsuarioAutenticado();

        return pendenciaRepository.findByUsuarioId(usuarioAutenticado.getId())
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public PendenciaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarPendenciaDoUsuario(id));
    }

    public PendenciaResponseDTO atualizar(Long id, PendenciaRequestDTO dto) {
        Pendencia pendencia = buscarPendenciaDoUsuario(id);

        pendencia.setTitulo(dto.getTitulo());
        pendencia.setDescricao(dto.getDescricao());
        pendencia.setStatus(dto.getStatus());
        pendencia.setDataVencimento(dto.getDataVencimento());
        pendencia.setPrioridade(dto.getPrioridade());
        pendencia.setOrigem(dto.getOrigem());

        return toResponseDTO(pendenciaRepository.save(pendencia));
    }

    public void deletar(Long id) {
        pendenciaRepository.delete(buscarPendenciaDoUsuario(id));
    }

    public List<PendenciaResponseDTO> listarPorStatus(StatusPendencia status) {
        Usuario usuarioAutenticado = getUsuarioAutenticado();

        return pendenciaRepository.findByResponsavelIdAndStatus(usuarioAutenticado.getId(), status)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PendenciaResponseDTO> listarVencidas() {
        Usuario usuarioAutenticado = getUsuarioAutenticado();
        LocalDate hoje = LocalDate.now();

        return pendenciaRepository
                .findByResponsavelIdAndDataVencimentoBeforeAndStatusNot(
                        usuarioAutenticado.getId(),
                        hoje,
                        StatusPendencia.CONCLUIDA
                )
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PendenciaResponseDTO> listarProximos7Dias() {
        Usuario usuarioAutenticado = getUsuarioAutenticado();
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(7);

        return pendenciaRepository
                .findByResponsavelIdAndDataVencimentoBetween(usuarioAutenticado.getId(), hoje, limite)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Usuario nao autenticado.");
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado nao encontrado."));
    }

    private Pendencia buscarPendenciaDoUsuario(Long id) {
        Usuario usuarioAutenticado = getUsuarioAutenticado();
        Pendencia pendencia = pendenciaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pendencia nao encontrada."));

        if (pendencia.getResponsavel() == null || !usuarioAutenticado.getId().equals(pendencia.getResponsavel().getId())) {
            throw new AccessDeniedException("Acesso negado a esta pendencia.");
        }

        return pendencia;
    }

    private PendenciaResponseDTO toResponseDTO(Pendencia pendencia) {
        PendenciaResponseDTO dto = new PendenciaResponseDTO();
        dto.setId(pendencia.getId());
        dto.setTitulo(pendencia.getTitulo());
        dto.setDescricao(pendencia.getDescricao());
        dto.setStatus(pendencia.getStatus());
        dto.setDataVencimento(pendencia.getDataVencimento());
        dto.setPrioridade(pendencia.getPrioridade());
        dto.setOrigem(pendencia.getOrigem());
        dto.setResponsavelId(pendencia.getResponsavel().getId());
        dto.setResponsavelNome(pendencia.getResponsavel().getNome());
        return dto;
    }
}
