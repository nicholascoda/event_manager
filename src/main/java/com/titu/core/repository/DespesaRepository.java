package com.titu.core.repository;

import com.titu.core.model.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    // Traz os lançamentos ordenados pela data (do mais novo para o mais velho)
    List<Despesa> findAllByOrderByDataLancamentoDesc();

    // Abordagem blindada: Filtra por um período (do dia 1 até o dia 30/31)
    @Query("SELECT SUM(d.valor) FROM Despesa d WHERE d.dataLancamento >= :inicio AND d.dataLancamento <= :fim")
    BigDecimal somarDespesasPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // O Java vai ler a 'descricao' dentro da classe 'Categoria'
    @Query("SELECT SUM(d.valor) FROM Despesa d WHERE d.categoria.descricao = :categoriaNome AND d.status = 'PAGO' AND d.dataLancamento >= :inicio AND d.dataLancamento <= :fim")
    BigDecimal somarDespesasPagasPorCategoria(@Param("categoriaNome") String categoriaNome, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}