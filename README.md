# Pendências Manager API

API REST para gestão de pendências e responsáveis, desenvolvida com Spring Boot.

## Objetivo

Este projeto foi criado para simular um sistema real de acompanhamento de pendências, com foco em organização, responsáveis, status e vencimentos.

## Tecnologias utilizadas

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 Database
- Maven
- Swagger / OpenAPI

## Funcionalidades

- Cadastro de usuários
- Cadastro de pendências
- Relacionamento entre usuário e pendência
- Listagem de pendências
- Busca de pendência por ID
- Atualização de pendência
- Exclusão de pendência
- Filtros por:
  - status
  - responsável
  - vencidas
  - próximos 7 dias
- Validação de dados
- Tratamento global de exceções
- Documentação com Swagger

## Estrutura do projeto

O projeto segue uma arquitetura em camadas:

- Controller
- Service
- Repository
- DTO
- Entity
- Exception

## Como executar o projeto

### Pré-requisitos
- Java 17 instalado
- Maven Wrapper incluído no projeto

### Rodando localmente

```bash
./mvnw spring-boot:run
