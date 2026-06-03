package com.titu.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataEvento;

    @ManyToOne
    @JoinColumn(name = "tipo_evento_id", nullable = false)
    private TipoEvento tipoEvento;

    // ==========================================
    // 💰 ENTRADAS (FIXO)
    // ==========================================
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal receitaBar = BigDecimal.ZERO;

    // ==========================================
    // 🔴 CUSTOS DIRETOS E PROVISÕES [DINÂMICO - JSONB]
    // ==========================================
    // O Hibernate 6 vai pegar esse Map e transformar nativamente numa coluna JSONB no PostgreSQL!
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, BigDecimal> custosDetalhados = new HashMap<>();

    // ==========================================
    // 🧠 MATEMÁTICA EM TEMPO REAL (Não salva no banco)
    // ==========================================

    // Calcula a soma de TODOS os custos dinâmicos que o cliente inventar dentro do JSON
    @Transient
    public BigDecimal getTotalCustos() {
        if (custosDetalhados == null || custosDetalhados.isEmpty()) {
            return BigDecimal.ZERO;
        }
        // Pega todos os valores do dicionário e soma tudo automaticamente
        return custosDetalhados.values().stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Calcula o Lucro do Dia (Receita - Total de Custos)
    @Transient
    public BigDecimal getLucroDoDia() {
        return receitaBar.subtract(getTotalCustos());
    }

    // Calcula a Margem Bruta em Porcentagem %
    @Transient
    public BigDecimal getMargem() {
        if (receitaBar.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getLucroDoDia().divide(receitaBar, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    // Descobre o dia da semana sozinho
    @Transient
    public String getDiaDaSemana() {
        return switch (dataEvento.getDayOfWeek()) {
            case MONDAY -> "Segunda";
            case TUESDAY -> "Terça";
            case WEDNESDAY -> "Quarta";
            case THURSDAY -> "Quinta";
            case FRIDAY -> "Sexta";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    // Puxa o valor do JSON pro HTML não quebrar
    @Transient
    public BigDecimal getCustoDinamico(String chave) {
        if (custosDetalhados == null || custosDetalhados.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return custosDetalhados.getOrDefault(chave, BigDecimal.ZERO);
    }

}