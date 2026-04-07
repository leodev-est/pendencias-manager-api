package com.leonardo.pendenciasmanager.dto.Request;

import com.leonardo.pendenciasmanager.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioRequestDTO {

    @NotBlank
    private String nome;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String senha;

    private String cargo;

    private Role role;
}
