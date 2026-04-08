# Pendencias Manager API

API REST para gerenciamento de pendencias e tarefas com autenticacao JWT, controle de ownership, roles, auditoria e documentacao OpenAPI.

## Stack

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- H2 para desenvolvimento e testes
- Docker e Docker Compose
- Swagger / OpenAPI

## Funcionalidades

- CRUD de usuarios
- CRUD de pendencias
- Autenticacao com JWT
- Controle de acesso com roles `USER` e `ADMIN`
- Ownership para proteger registros por usuario autenticado
- Paginacao, filtros e ordenacao
- Auditoria de criacao e atualizacao
- Testes unitarios e de integracao

## Executando localmente

### Opcao 1: Spring Boot com banco em memoria

No Windows:

```bash
mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

### Opcao 2: Docker Compose com PostgreSQL

```bash
docker compose up --build
```

## Documentacao da API

Com a aplicacao em execucao:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

![Swagger Preview](./swagger-preview.png)

## Deploy

O projeto esta preparado para deploy no Render usando o blueprint `render.yaml`.

- Guia de deploy: `DEPLOY.md`
- Perfil de producao: `src/main/resources/application-prod.properties`

### URL publica

- API: pendente de publicacao no provedor
- Swagger: pendente de publicacao no provedor

Depois do primeiro deploy, substitua os campos acima pela URL real gerada pela plataforma.

## Variaveis de ambiente de producao

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver`
- `SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect`
- `SPRING_JPA_HIBERNATE_DDL_AUTO=update`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `PORT`

## Repositorio

- GitHub: `https://github.com/leodev-est/pendencias-manager-api`

## Autor

Leonardo Silva Esteves
