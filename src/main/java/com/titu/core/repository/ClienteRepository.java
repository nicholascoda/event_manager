package com.titu.core.repository;

import com.titu.core.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // o Spring cria o SQL sozinho
    boolean existsByEmail(String email);
    boolean existsByCnpj(String cnpj);

    java.util.Optional<Cliente> findByEmail(String email);
    java.util.Optional<Cliente> findByCnpj(String cnpj);

    // Busca quem te paga (Para a tela de Títulos)
    @Query("SELECT c FROM Cliente c WHERE c.tipoParceiro = 'CLIENTE' OR c.tipoParceiro = 'AMBOS'")
    List<Cliente> buscarSomenteClientes();

    // Busca para quem você paga (Para a futura tela de Despesas)
    @Query("SELECT c FROM Cliente c WHERE c.tipoParceiro = 'FORNECEDOR' OR c.tipoParceiro = 'AMBOS'")
    List<Cliente> buscarSomenteFornecedores();
}