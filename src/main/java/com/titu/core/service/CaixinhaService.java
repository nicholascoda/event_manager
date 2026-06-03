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
import java.util.Map;

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
            // Pega a gaveta do JSON (se não for nula)
            Map<String, BigDecimal> custos = e.getCustosDetalhados();

            if (custos != null) {
                // Abre a gaveta e procura a chave. Se não achar, devolve ZERO.
                provSocios = provSocios.add(custos.getOrDefault("provisaoSocios", BigDecimal.ZERO));
                provCustoBar = provCustoBar.add(custos.getOrDefault("provisaoCustoBar", BigDecimal.ZERO));
                provDecoracao = provDecoracao.add(custos.getOrDefault("provisaoDecoracao", BigDecimal.ZERO));
                provTaxa = provTaxa.add(custos.getOrDefault("provisaoTaxa", BigDecimal.ZERO));
            }
        }

        // 2. Define as datas do mês para a query blindada
        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = YearMonth.of(ano, mes).atEndOfMonth();

        // 3. Monta as caixinhas mantendo os seus ícones e cores originais da tela
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