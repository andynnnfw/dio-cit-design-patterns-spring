package com.andynn.patterns.exception;

public class CepInvalidoException extends RuntimeException {

	public CepInvalidoException(String cep) {
		super("CEP invalido ou nao encontrado: " + cep);
	}
}
