package com.leonardo.pendenciasmanager.service;

import com.leonardo.pendenciasmanager.dto.Request.UsuarioRequestDTO;
import com.leonardo.pendenciasmanager.dto.Response.UsuarioResponseDTO;
import com.leonardo.pendenciasmanager.entity.Usuario;
import com.leonardo.pendenciasmanager.exception.BusinessException;
import com.leonardo.pendenciasmanager.repository.PendenciaRepository;
import com.leonardo.pendenciasmanager.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PendenciaRepository pendenciaRepository;

    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Já existe um usuário com esse email.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setCargo(dto.getCargo());

        Usuario salvo = repository.save(usuario);

        return toResponseDTO(salvo);
    }

    public List<UsuarioResponseDTO> listar() {
        return repository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado."));

        return toResponseDTO(usuario);
    }

    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado."));

        repository.findByEmail(dto.getEmail())
                .filter(outroUsuario -> !outroUsuario.getId().equals(id))
                .ifPresent(outroUsuario -> {
                    throw new BusinessException("Ja existe um usuario com esse email.");
                });

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(dto.getSenha());
        usuario.setCargo(dto.getCargo());

        Usuario atualizado = repository.save(usuario);
        return toResponseDTO(atualizado);
    }

    public void deletar(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado."));

        if (pendenciaRepository.existsByResponsavelId(id)) {
            throw new BusinessException("Usuario possui pendencias vinculadas e nao pode ser removido.");
        }

        repository.delete(usuario);
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setCargo(usuario.getCargo());
        return dto;
    }
}
