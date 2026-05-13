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

    public List<Despesa> listarTodas() {
        return despesaRepository.findAllByOrderByDataVencimentoAsc();
    }

    @Transactional
    public void salvar(Despesa despesa, Long fornecedorId) {
        // Se o usuário selecionou um fornecedor no select, a gente busca ele e amarra.
        if (fornecedorId != null) {
            Cliente fornecedor = clienteService.buscarPorId(fornecedorId);
            despesa.setFornecedor(fornecedor);
        } else {
            // Se veio vazio, é Despesa Avulsa
            despesa.setFornecedor(null);
        }

        // Garante que uma despesa nova não venha nula no status
        if (despesa.getStatus() == null) {
            despesa.setStatus(StatusDespesa.PENDENTE);
        }

        despesaRepository.save(despesa);
    }

    @Transactional
    public void darBaixa(Long id) {
        Despesa despesa = despesaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada"));

        despesa.setStatus(StatusDespesa.PAGO);
        despesa.setDataPagamento(LocalDate.now());

        despesaRepository.save(despesa);
    }

    @Transactional
    public void excluir(Long id) {
        despesaRepository.deleteById(id);
    }
}