# 🚀 Pendências Manager API

API REST para gestão de pendências e responsáveis, desenvolvida com Java e Spring Boot.

---

## 📌 Objetivo

Este projeto simula um sistema real de acompanhamento de pendências, permitindo organizar tarefas, responsáveis, prazos e status, com foco em boas práticas de backend.

---

## 🛠️ Tecnologias utilizadas

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 Database (em memória)
- Maven
- Swagger / OpenAPI

---

## ⚙️ Funcionalidades

### 👤 Usuários
- Criar usuário
- Listar usuários

### 📋 Pendências
- Criar pendência
- Listar pendências
- Buscar pendência por ID
- Atualizar pendência
- Deletar pendência

### 🔎 Filtros
- Buscar por status
- Buscar por responsável
- Listar pendências vencidas
- Listar pendências dos próximos 7 dias

---

## 🧱 Arquitetura

O projeto segue uma arquitetura em camadas:

- Controller → entrada das requisições
- Service → regras de negócio
- Repository → acesso ao banco
- DTO → transporte de dados
- Entity → modelo de dados
- Exception → tratamento global de erros

---

## ▶️ Como executar o projeto

### Pré-requisitos
- Java 17 instalado

### Rodando localmente

```bash
mvnw.cmd spring-boot:run
