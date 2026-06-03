package com.titu.core.repository;

import com.titu.core.model.ColunaDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColunaDiarioRepository extends JpaRepository<ColunaDiario, Long> {
    boolean existsByChaveBanco(String chaveBanco);
}