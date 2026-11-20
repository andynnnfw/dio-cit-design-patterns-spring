# Design Patterns com Java e Spring Boot

API REST de cadastro de clientes com **busca automática de endereço pelo CEP** (via [ViaCEP](https://viacep.com.br)), construída para explorar na prática os padrões de projeto **Singleton**, **Strategy** e **Facade** com Spring Boot.

Projeto desenvolvido no desafio *"Design Patterns com Java: Dos Clássicos (GoF) ao Spring Framework"* do **Bootcamp CI&T – Java AI Copilot**, da [DIO](https://www.dio.me).

## Como funciona

1. O cliente é cadastrado informando apenas **nome**, **telefone** e **CEP**.
2. A API verifica se o CEP já existe no banco:
   - se existir, reaproveita o endereço salvo;
   - se não existir, consulta o **ViaCEP**, salva o endereço e o associa ao cliente.
3. O cliente é salvo com o endereço completo (rua, bairro, cidade e UF).

## Padrões de projeto aplicados

| Padrão | Onde | Como |
|---|---|---|
| **Singleton** | `ClienteServiceImpl`, repositórios e `ViaCepService` | O Spring cria uma única instância de cada bean (`@Service`, `@Repository`, `@FeignClient`) e a injeta pelo construtor. |
| **Strategy** | `ClienteService` → `ClienteServiceImpl` | O controller depende da interface, então a implementação pode ser trocada sem alterar quem a usa. |
| **Facade** | `ClienteServiceImpl` | Esconde a complexidade de consultar o ViaCEP, reaproveitar endereços e salvar no banco, expondo métodos simples como `inserir(cliente)`. |

## Melhorias em relação ao projeto base

- Campo **telefone** no cliente.
- Endpoint de **busca de clientes por cidade**.
- **Validação do CEP** (aceita `01001000` ou `01001-000`) e erro claro para CEP inexistente.
- **Tratamento global de erros** com `ProblemDetail` (404 para cliente não encontrado e 400 para CEP inválido).
- Injeção de dependências **pelo construtor**.

## Tecnologias

- Java 21
- Spring Boot 4 (Web MVC e Data JPA)
- Spring Cloud OpenFeign
- H2 Database (em memória)

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/clientes` | Lista todos os clientes |
| `GET` | `/clientes/{id}` | Busca um cliente pelo id |
| `GET` | `/clientes/cidade/{cidade}` | Lista clientes de uma cidade |
| `POST` | `/clientes` | Cadastra um cliente |
| `PUT` | `/clientes/{id}` | Atualiza um cliente |
| `DELETE` | `/clientes/{id}` | Remove um cliente |

Exemplo de corpo para `POST /clientes`:

```json
{
  "nome": "Anderson",
  "telefone": "11999999999",
  "endereco": { "cep": "01001000" }
}
```

Resposta (`201 Created`):

```json
{
  "id": 1,
  "nome": "Anderson",
  "telefone": "11999999999",
  "endereco": {
    "cep": "01001000",
    "logradouro": "Praça da Sé",
    "bairro": "Sé",
    "localidade": "São Paulo",
    "uf": "SP"
  }
}
```

## Como executar

```bash
git clone https://github.com/andynnnfw/dio-cit-design-patterns-spring.git
cd dio-cit-design-patterns-spring
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.
O console do H2 fica em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:clientesdb`, usuário `sa`, sem senha).

## Autor

**Anderson Melo do Nascimento**
[GitHub](https://github.com/andynnnfw) · [LinkedIn](https://www.linkedin.com/in/andynnnfw)
