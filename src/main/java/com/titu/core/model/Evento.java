package com.titu.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    // 💰 ENTRADAS
    // ==========================================
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal receitaBar = BigDecimal.ZERO;


    private BigDecimal custoFixo = BigDecimal.ZERO;

    // ==========================================
    // 🔴 CUSTOS DIRETOS [NA HORA] (Sai do caixa no dia)
    // ==========================================
    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal custoProblemas = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal custoDiarias = BigDecimal.ZERO; // Diárias + Compras

    @Builder.Default
    private BigDecimal custoPromoters = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal custoDjPagode = BigDecimal.ZERO; // Podemos chamar de Atrações também

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal custoSeguranca = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal custoSom = BigDecimal.ZERO;

    // ==========================================
    // 📦 PROVISÕES [CAIXINHAS] (Guarda para pagar depois)
    // ==========================================
    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal provisaoCustoBar = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal provisaoSocios = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal provisaoDecoracao = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal provisaoTaxa = BigDecimal.ZERO;

    // ==========================================
    // 🧠 MATEMÁTICA EM TEMPO REAL (Não salva no banco)
    // ==========================================

    // Calcula a soma de TODOS os custos (Na Hora + Provisão)
    @Column(precision = 19, scale = 2)
    @Transient
    public BigDecimal getTotalCustos() {
        return custoProblemas.add(custoDiarias).add(custoPromoters)
                .add(custoDjPagode).add(custoSeguranca).add(custoSom)
                .add(provisaoCustoBar).add(provisaoSocios)
                .add(provisaoDecoracao).add(provisaoTaxa);
    }

    // Calcula o Lucro do Dia (Receita - Total de Custos)
    @Column(precision = 19, scale = 2)
    @Transient
    public BigDecimal getLucroDoDia() {
        return receitaBar.subtract(getTotalCustos());
    }

    // Calcula a Margem Bruta em Porcentagem %
    @Column(precision = 19, scale = 2)
    @Transient
    public BigDecimal getMargem() {
        if (receitaBar.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        // (Lucro / Receita) * 100
        return getLucroDoDia().divide(receitaBar, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    // Descobre o dia da semana sozinho para o JavaScript poder filtrar as opções
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

}