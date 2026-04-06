package com.leonardo.pendenciasmanager.controller;

import com.leonardo.pendenciasmanager.dto.PendenciaRequestDTO;
import com.leonardo.pendenciasmanager.dto.PendenciaResponseDTO;
import com.leonardo.pendenciasmanager.enums.StatusPendencia;
import com.leonardo.pendenciasmanager.service.PendenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pendencias")
public class PendenciaController {

    @Autowired
    private PendenciaService service;

    @PostMapping
    public PendenciaResponseDTO criar(@RequestBody @Valid PendenciaRequestDTO dto) {
        return service.criar(dto);
    }

    @GetMapping
    public List<PendenciaResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PendenciaResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public PendenciaResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid PendenciaRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }

        @GetMapping("/status")
    public List<PendenciaResponseDTO> listarPorStatus(@RequestParam StatusPendencia status) {
        return service.listarPorStatus(status);
    }

    @GetMapping("/responsavel/{responsavelId}")
    public List<PendenciaResponseDTO> listarPorResponsavel(@PathVariable Long responsavelId) {
        return service.listarPorResponsavel(responsavelId);
    }

    @GetMapping("/vencidas")
    public List<PendenciaResponseDTO> listarVencidas() {
        return service.listarVencidas();
    }

    @GetMapping("/proximos-7-dias")
    public List<PendenciaResponseDTO> listarProximos7Dias() {
        return service.listarProximos7Dias();
    }
}