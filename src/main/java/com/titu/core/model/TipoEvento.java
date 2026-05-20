package com.titu.core.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_evento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false, length = 7)
    @Builder.Default
    private String cor = "#444444";

}