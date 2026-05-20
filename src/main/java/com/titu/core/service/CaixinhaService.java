package com.titu.core.service;

import com.titu.core.dto.CaixinhaDTO;
import com.titu.core.model.Evento;
import com.titu.core.repository.EventoRepository;
import com.titu.core.repository.DespesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaixinhaService {

    private final EventoRepository eventoRepository;
    private final DespesaRepository despesaRepository; // Conectado com a Aba 3!

    public List<CaixinhaDTO> processarCaixinhasDoMes(int ano, int mes) {
        // 1. Puxa as provisões do Controle Diário (Aba 4)
        List<Evento> eventos = eventoRepository.findByMesEAno(ano, mes);

        BigDecimal provSocios = BigDecimal.ZERO;
        BigDecimal provCustoBar = BigDecimal.ZERO;
        BigDecimal provDecoracao = BigDecimal.ZERO;
        BigDecimal provTaxa = BigDecimal.ZERO;

        for (Evento e : eventos) {
            if (e.getProvisaoSocios() != null) provSocios = provSocios.add(e.getProvisaoSocios());
            if (e.getProvisaoCustoBar() != null) provCustoBar = provCustoBar.add(e.getProvisaoCustoBar());
            if (e.getProvisaoDecoracao() != null) provDecoracao = provDecoracao.add(e.getProvisaoDecoracao());
            if (e.getProvisaoTaxa() != null) provTaxa = provTaxa.add(e.getProvisaoTaxa());
        }

        // 2. Define as datas do mês para a query blindada
        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = YearMonth.of(ano, mes).atEndOfMonth();

        // 3. Monta as caixinhas (ATENÇÃO: O primeiro texto DEVE ser igual ao nome da Categoria salva no seu banco!)
        List<CaixinhaDTO> caixinhas = new ArrayList<>();
        caixinhas.add(montarDTO("Sócios", "fas fa-user-tie", "bg-primary", provSocios, inicioMes, fimMes));
        caixinhas.add(montarDTO("Custo Bar", "fas fa-beer", "bg-warning text-dark", provCustoBar, inicioMes, fimMes));
        caixinhas.add(montarDTO("Decoração", "fas fa-paint-roller", "bg-info", provDecoracao, inicioMes, fimMes));
        caixinhas.add(montarDTO("Taxa", "fas fa-file-invoice", "bg-danger", provTaxa, inicioMes, fimMes));

        return caixinhas;
    }

    private CaixinhaDTO montarDTO(String nomeCategoria, String icone, String cor, BigDecimal provisionado, LocalDate inicio, LocalDate fim) {
        // Vai na Aba 3 e pergunta: Quanto já pagamos dessa categoria esse mês?
        BigDecimal jaPago = despesaRepository.somarDespesasPagasPorCategoria(nomeCategoria, inicio, fim);
        if (jaPago == null) jaPago = BigDecimal.ZERO;

        // Saldo = Provisionado - Já Pago
        BigDecimal saldo = provisionado.subtract(jaPago);

        return new CaixinhaDTO(nomeCategoria, nomeCategoria, icone, cor, provisionado, jaPago, saldo);
    }
}