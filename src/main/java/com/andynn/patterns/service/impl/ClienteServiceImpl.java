package com.andynn.patterns.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.andynn.patterns.exception.CepInvalidoException;
import com.andynn.patterns.exception.ClienteNaoEncontradoException;
import com.andynn.patterns.model.Cliente;
import com.andynn.patterns.model.Endereco;
import com.andynn.patterns.repository.ClienteRepository;
import com.andynn.patterns.repository.EnderecoRepository;
import com.andynn.patterns.service.ClienteService;
import com.andynn.patterns.service.ViaCepService;

@Service
public class ClienteServiceImpl implements ClienteService {

	private final ClienteRepository clienteRepository;
	private final EnderecoRepository enderecoRepository;
	private final ViaCepService viaCepService;

	public ClienteServiceImpl(ClienteRepository clienteRepository,
			EnderecoRepository enderecoRepository,
			ViaCepService viaCepService) {
		this.clienteRepository = clienteRepository;
		this.enderecoRepository = enderecoRepository;
		this.viaCepService = viaCepService;
	}

	@Override
	public List<Cliente> buscarTodos() {
		return clienteRepository.findAll();
	}

	@Override
	public Cliente buscarPorId(Long id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new ClienteNaoEncontradoException(id));
	}

	@Override
	public List<Cliente> buscarPorCidade(String cidade) {
		return clienteRepository.findByEnderecoLocalidadeIgnoreCase(cidade);
	}

	@Override
	public Cliente inserir(Cliente cliente) {
		cliente.setId(null);
		return salvarClienteComCep(cliente);
	}

	@Override
	public Cliente atualizar(Long id, Cliente cliente) {
		buscarPorId(id);
		cliente.setId(id);
		return salvarClienteComCep(cliente);
	}

	@Override
	public void deletar(Long id) {
		buscarPorId(id);
		clienteRepository.deleteById(id);
	}

	private Cliente salvarClienteComCep(Cliente cliente) {
		String cep = normalizarCep(cliente.getEndereco() == null ? null : cliente.getEndereco().getCep());

		Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
			Endereco novoEndereco = viaCepService.consultarCep(cep);
			if (novoEndereco == null || novoEndereco.getLocalidade() == null) {
				throw new CepInvalidoException(cep);
			}
			novoEndereco.setCep(cep);
			return enderecoRepository.save(novoEndereco);
		});

		cliente.setEndereco(endereco);
		return clienteRepository.save(cliente);
	}

	private String normalizarCep(String cep) {
		String somenteNumeros = cep == null ? "" : cep.replaceAll("\\D", "");
		if (somenteNumeros.length() != 8) {
			throw new CepInvalidoException(cep);
		}
		return somenteNumeros;
	}
}
