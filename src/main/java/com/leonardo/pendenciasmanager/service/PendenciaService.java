package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.dto.PendenciaRequestDTO;
import com.leonardo.pendenciasmanager.dto.PendenciaResponseDTO;
import com.leonardo.pendenciasmanager.entity.Pendencia;
import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.repository.PendenciaRepository;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
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
        Usuario responsavel = usuarioRepository.findById(dto.getResponsavelId())
                .orElseThrow(() -> new BusinessException("Responsável não encontrado."));

        Pendencia pendencia = new Pendencia();
        pendencia.setTitulo(dto.getTitulo());
        pendencia.setDescricao(dto.getDescricao());
        pendencia.setStatus(dto.getStatus());
        pendencia.setDataVencimento(dto.getDataVencimento());
        pendencia.setPrioridade(dto.getPrioridade());
        pendencia.setOrigem(dto.getOrigem());
        pendencia.setResponsavel(responsavel);

        Pendencia salva = pendenciaRepository.save(pendencia);

        return toResponseDTO(salva);
    }

    public List<PendenciaResponseDTO> listar() {
        return pendenciaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
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
    public List<PendenciaResponseDTO> listarPorStatus(com.leonardo.pendenciasmanager.enums.StatusPendencia status) {
    return pendenciaRepository.findByStatus(status)
            .stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<PendenciaResponseDTO> listarPorResponsavel(Long responsavelId) {
        return pendenciaRepository.findByResponsavelId(responsavelId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PendenciaResponseDTO> listarVencidas() {
        LocalDate hoje = LocalDate.now();

        return pendenciaRepository
                .findByDataVencimentoBeforeAndStatusNot(hoje, StatusPendencia.CONCLUIDA)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PendenciaResponseDTO> listarProximos7Dias() {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(7);

        return pendenciaRepository.findByDataVencimentoBetween(hoje, limite)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public PendenciaResponseDTO buscarPorId(Long id) {
    Pendencia pendencia = pendenciaRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Pendência não encontrada."));

    return toResponseDTO(pendencia);
    }

    public PendenciaResponseDTO atualizar(Long id, PendenciaRequestDTO dto) {
    Pendencia pendencia = pendenciaRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Pendência não encontrada."));

    Usuario responsavel = usuarioRepository.findById(dto.getResponsavelId())
            .orElseThrow(() -> new BusinessException("Responsável não encontrado."));

    pendencia.setTitulo(dto.getTitulo());
    pendencia.setDescricao(dto.getDescricao());
    pendencia.setStatus(dto.getStatus());
    pendencia.setDataVencimento(dto.getDataVencimento());
    pendencia.setPrioridade(dto.getPrioridade());
    pendencia.setOrigem(dto.getOrigem());
    pendencia.setResponsavel(responsavel);

    Pendencia atualizada = pendenciaRepository.save(pendencia);

    return toResponseDTO(atualizada);
    }

    public void deletar(Long id) {
    Pendencia pendencia = pendenciaRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Pendência não encontrada."));

    pendenciaRepository.delete(pendencia);
    }

    
}