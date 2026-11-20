package com.andynn.patterns.service;

import java.util.List;

import com.andynn.patterns.model.Cliente;

public interface ClienteService {

	List<Cliente> buscarTodos();

	Cliente buscarPorId(Long id);

	List<Cliente> buscarPorCidade(String cidade);

	Cliente inserir(Cliente cliente);

	Cliente atualizar(Long id, Cliente cliente);

	void deletar(Long id);
}
