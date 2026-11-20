package com.andynn.patterns.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.andynn.patterns.model.Endereco;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, String> {
}
