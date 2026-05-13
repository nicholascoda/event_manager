package com.titu.core.repository;

import com.titu.core.model.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    // Traz ordenado pelo vencimento, do mais antigo pro mais novo
    List<Despesa> findAllByOrderByDataVencimentoAsc();
}