package com.titu.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // 🏢 DADOS DO PARCEIRO (Fornecedores, Equipe, Promoters)
    // ==========================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'CLIENTE'")
    @Builder.Default
    private TipoParceiro tipoParceiro = TipoParceiro.CLIENTE;

    @NotBlank(message = "Nome da empresa/parceiro é obrigatório")
    @Column(nullable = false)
    private String nomeEmpresa;

    @NotBlank(message = "Nome do responsável é obrigatório")
    private String nomeResponsavel;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "Telefone/WhatsApp é obrigatório")
    private String telefone;

    private String cnpj; // Pode ser CPF também na prática

    @Column(updatable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
    }
}