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

## Estrutura do projeto

```
src/main/java/com/andynn/patterns
├── DioCitDesignPatternsSpringApplication.java
├── controller
│   └── ClienteRestController.java
├── exception
│   ├── CepInvalidoException.java
│   ├── ClienteNaoEncontradoException.java
│   └── GlobalExceptionHandler.java
├── model
│   ├── Cliente.java
│   └── Endereco.java
├── repository
│   ├── ClienteRepository.java
│   └── EnderecoRepository.java
└── service
    ├── ClienteService.java
    ├── ViaCepService.java
    └── impl
        └── ClienteServiceImpl.java
```

### Aplicação

- **`DioCitDesignPatternsSpringApplication`**: ponto de entrada da aplicação. O `@SpringBootApplication` inicia o Spring e o `@EnableFeignClients` ativa os clients HTTP do OpenFeign.

### `controller`: camada de entrada

- **`ClienteRestController`**: recebe as requisições HTTP em `/clientes` e devolve as respostas com o status correto (`200`, `201`, `204`). Depende apenas da interface `ClienteService`, sem conhecer o ViaCEP nem o banco de dados.

### `model`: entidades

- **`Cliente`**: entidade JPA com `id` gerado automaticamente, `nome`, `telefone` e uma relação `@ManyToOne` com `Endereco` (vários clientes podem compartilhar o mesmo CEP).
- **`Endereco`**: entidade JPA cujo identificador é o próprio CEP. Os atributos seguem o JSON do ViaCEP, então o mesmo objeto recebe a resposta da API e é salvo no banco.

### `repository`: acesso a dados

- **`ClienteRepository`**: estende `JpaRepository` e ganha o CRUD pronto. O método `findByEnderecoLocalidadeIgnoreCase` é uma *query derivada*: o Spring Data gera a consulta SQL a partir do nome do método.
- **`EnderecoRepository`**: CRUD de endereços, usado para reaproveitar um CEP já consultado e evitar chamadas repetidas ao ViaCEP.

### `service`: regras de negócio

- **`ClienteService`**: interface que define as operações do sistema (padrão **Strategy**).
- **`ClienteServiceImpl`**: implementação que concentra a lógica (padrão **Facade**): valida e normaliza o CEP, reaproveita o endereço salvo ou consulta o ViaCEP e salva o cliente.
- **`ViaCepService`**: interface anotada com `@FeignClient`. O Spring gera a implementação que faz o `GET` em `viacep.com.br/ws/{cep}/json/`, sem precisar escrever código HTTP manualmente.

### `exception`: tratamento de erros

- **`CepInvalidoException`**: lançada quando o CEP não tem 8 dígitos ou não existe no ViaCEP.
- **`ClienteNaoEncontradoException`**: lançada quando o `id` buscado não existe.
- **`GlobalExceptionHandler`**: com `@RestControllerAdvice`, centraliza o tratamento dos erros e os converte em respostas `ProblemDetail` (padrão RFC 9457), com status `400` ou `404`.

### `resources`

- **`application.properties`**: configura o banco H2 em memória, o console do H2 e a exibição do SQL no log.

## Fluxo de uma requisição

```
POST /clientes
   │
   ▼
ClienteRestController
   │
   ▼
ClienteService (Facade)
   ├── EnderecoRepository: o CEP já está salvo?
   │      ├── sim → reaproveita o endereço
   │      └── não → ViaCepService consulta o ViaCEP → salva o endereço
   └── ClienteRepository: salva o cliente
   │
   ▼
201 Created com o cliente e o endereço completo
```

## O que aprendi

- Aplicar padrões do GoF (**Singleton**, **Strategy** e **Facade**) em um projeto real.
- Como o Spring implementa o **Singleton** por meio da inversão de controle e da injeção de dependências pelo construtor.
- Programar para **interfaces**, deixando o código desacoplado e fácil de evoluir.
- Consumir APIs externas de forma declarativa com **Spring Cloud OpenFeign**.
- Usar o **Spring Data JPA**: repositórios, *queries* derivadas e relacionamentos entre entidades.
- Centralizar o **tratamento de erros** com `@RestControllerAdvice` e `ProblemDetail`.
- Organizar uma API em **camadas** (controller, service, repository e model).
- Versionar o projeto com **commits por etapa** no Git.

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

## Testando com o Insomnia

Com a aplicação rodando em `http://localhost:8080`, abra o [Insomnia](https://insomnia.rest) e siga os passos:

1. Clique em **New Collection** e dê um nome, por exemplo `Design Patterns API`.
2. Dentro da coleção, clique em **New HTTP Request** para cada requisição abaixo.
3. Escolha o **método** (GET, POST, PUT ou DELETE), cole a **URL** e, quando houver corpo, vá na aba **Body → JSON** e cole o JSON.
4. Clique em **Send** e confira a resposta e o status no painel da direita.

### 1. Cadastrar um cliente

- **Método:** `POST`
- **URL:** `http://localhost:8080/clientes`
- **Body (JSON):**

```json
{
  "nome": "Anderson",
  "telefone": "11999999999",
  "endereco": { "cep": "01001000" }
}
```

Resultado esperado: **`201 Created`** com o endereço preenchido automaticamente pelo ViaCEP.

### 2. Listar todos os clientes

- **Método:** `GET`
- **URL:** `http://localhost:8080/clientes`

Resultado esperado: **`200 OK`** com a lista de clientes.

### 3. Buscar um cliente pelo id

- **Método:** `GET`
- **URL:** `http://localhost:8080/clientes/1`

Resultado esperado: **`200 OK`** com o cliente de id 1.

### 4. Buscar clientes por cidade

- **Método:** `GET`
- **URL:** `http://localhost:8080/clientes/cidade/São Paulo`

Resultado esperado: **`200 OK`** com os clientes da cidade.

### 5. Atualizar um cliente

- **Método:** `PUT`
- **URL:** `http://localhost:8080/clientes/1`
- **Body (JSON):**

```json
{
  "nome": "Anderson Melo",
  "telefone": "11988888888",
  "endereco": { "cep": "20040002" }
}
```

Resultado esperado: **`200 OK`** com o novo endereço (Rio de Janeiro).

### 6. Remover um cliente

- **Método:** `DELETE`
- **URL:** `http://localhost:8080/clientes/1`

Resultado esperado: **`204 No Content`**.

### 7. Testando os erros

| Teste | Método e URL | Body | Resultado esperado |
|---|---|---|---|
| CEP com formato inválido | `POST /clientes` | `{"nome": "Teste", "endereco": {"cep": "123"}}` | `400 Bad Request` |
| CEP inexistente | `POST /clientes` | `{"nome": "Teste", "endereco": {"cep": "99999999"}}` | `400 Bad Request` |
| Cliente inexistente | `GET /clientes/999` | - | `404 Not Found` |

Exemplo de resposta de erro (`ProblemDetail`):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "CEP invalido ou nao encontrado: 123",
  "instance": "/clientes"
}
```

## Autor

**Anderson Melo do Nascimento**
[GitHub](https://github.com/andynnnfw) · [LinkedIn](https://www.linkedin.com/in/andynnnfw)
