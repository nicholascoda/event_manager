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
    // 🏢 DADOS DO PARCEIRO (ESTILO SAP)
    // ==========================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'CLIENTE'")
    @Builder.Default
    private TipoParceiro tipoParceiro = TipoParceiro.CLIENTE;

    @NotBlank(message = "Nome da empresa é obrigatório")
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

    private String cnpj;

    // ==========================================
    // 🤖 CONFIGURAÇÕES DO ROBÔ DE COBRANÇA
    // ==========================================

    @Builder.Default
    private Boolean usarRegrasGlobais = true;

    @Builder.Default
    private Boolean preventivoAtivo = true;

    @Builder.Default
    private Boolean vencimentoAtivo = true;

    @Builder.Default
    private Boolean atrasoAtivo = true;

    @Builder.Default
    private String tomMensagem = "MEDIO";

    @Column(updatable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
    }
}