package com.leonardo.pendenciasmanager.controller;

import com.leonardo.pendenciasmanager.dto.UsuarioRequestDTO;
import com.leonardo.pendenciasmanager.dto.UsuarioResponseDTO;
import com.leonardo.pendenciasmanager.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @PostMapping
    public UsuarioResponseDTO criar(@RequestBody @Valid UsuarioRequestDTO dto) {
        return service.criar(dto);
    }

    @GetMapping
    public List<UsuarioResponseDTO> listar() {
        return service.listar();
    }
}