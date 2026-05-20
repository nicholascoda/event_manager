package com.titu.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CaixinhaDTO {
    private String idInterno; // Código para o banco de dados saber de onde sacar
    private String nomeVisivel; // Título da caixinha
    private String icone; // Ícone do FontAwesome
    private String cor; // Cor do card
    private BigDecimal totalArrecadado;
    private BigDecimal totalSacado;
    private BigDecimal saldoAtual;
}