package com.leonardo.pendenciasmanager.dto.Response;

import com.leonardo.pendenciasmanager.enums.Role;
import lombok.Data;

@Data
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String cargo;
    private Role role;
}
