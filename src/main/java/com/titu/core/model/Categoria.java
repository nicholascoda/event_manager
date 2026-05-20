package com.titu.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição da categoria é obrigatória")
    @Column(nullable = false, unique = true)
    private String descricao;

    // A mágica: O cliente cria o nome que quiser, mas OBRIGATORIAMENTE
    // tem que dizer pro sistema se é Provisão ou Variável.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCategoria tipo;
}