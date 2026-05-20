package com.titu.core.repository;

import com.titu.core.model.FechamentoMes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FechamentoMesRepository extends JpaRepository<FechamentoMes, Long> {

    // Pergunta rápida ao banco: "Esse mês e ano estão trancados?"
    boolean existsByMesAndAnoAndFechadoTrue(Integer mes, Integer ano);

    Optional<FechamentoMes> findByMesAndAno(Integer mes, Integer ano);
}