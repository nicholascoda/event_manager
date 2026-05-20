package com.titu.core.service;

import com.titu.core.model.Cliente;
import com.titu.core.model.Despesa;
import com.titu.core.model.StatusDespesa;
import com.titu.core.repository.DespesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DespesaService {

    private final DespesaRepository despesaRepository;
    private final ClienteService clienteService;
    private final FechamentoService fechamentoService;

    public List<Despesa> listarTodas() {
        return despesaRepository.findAllByOrderByDataLancamentoDesc();
    }

    @Transactional
    public void salvar(Despesa despesa, Long fornecedorId) {
        if (fornecedorId != null) {
            Cliente fornecedor = clienteService.buscarPorId(fornecedorId);
            despesa.setFornecedor(fornecedor);
        } else {
            despesa.setFornecedor(null);
        }

        if (despesa.getStatus() == null) {
            despesa.setStatus(StatusDespesa.PENDENTE);
        }

        if (despesa.getDataLancamento() == null) {
            despesa.setDataLancamento(java.time.LocalDate.now());
        }

        // --- A TRAVA DE SEGURANÇA ---
        int mes = despesa.getDataLancamento().getMonthValue();
        int ano = despesa.getDataLancamento().getYear();

        if (fechamentoService.isMesTrancado(mes, ano)) {
            throw new RuntimeException("Operação bloqueada! O mês financeiro desta despesa já foi fechado.");
        }
        // ----------------------------

        despesaRepository.save(despesa);
    }

    @Transactional
    public void excluir(Long id) {
        // Primeiro acha a despesa para descobrir de qual mês ela é
        Despesa despesa = despesaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada."));

        // --- A TRAVA DE SEGURANÇA ---
        int mes = despesa.getDataLancamento().getMonthValue();
        int ano = despesa.getDataLancamento().getYear();

        if (fechamentoService.isMesTrancado(mes, ano)) {
            throw new RuntimeException("Operação bloqueada! Você não pode excluir despesas de um mês já fechado.");
        }
        // ----------------------------

        despesaRepository.deleteById(id);
    }

    @Transactional
    public void darBaixa(Long id) {
        Despesa despesa = despesaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada"));

        despesa.setStatus(StatusDespesa.PAGO);
        despesa.setDataPagamento(LocalDate.now());

        despesaRepository.save(despesa);
    }




}