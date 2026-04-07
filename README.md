- Listar pendências vencidas
- Listar pendências próximas do vencimento

---

## 🏗️ Arquitetura do Projeto

O projeto segue o padrão de arquitetura em camadas:

- **Controller** → Recebe as requisições HTTP  
- **Service** → Contém as regras de negócio  
- **Repository** → Comunicação com o banco de dados  
- **Entity** → Representação das tabelas  
- **DTO** → Transferência de dados entre camadas  
- **Exception Handler** → Tratamento global de erros  

---

## 🔌 Endpoints (exemplos)

### ➤ Criar pendência

POST /pendencias


### ➤ Listar todas

GET /pendencias


### ➤ Buscar por ID

GET /pendencias/{id}


### ➤ Filtrar por status

GET /pendencias/status/{status}


### ➤ Filtrar por responsável

GET /pendencias/responsavel/{nome}


---

## ▶️ Como executar o projeto

### Pré-requisitos
- Java 17+
- Maven

### Clone o repositório

git clone https://github.com/leodev-est/pendencias-manager-api.git

cd pendencias-manager-api


### Rodar aplicação

#### Linux / Mac

./mvnw spring-boot:run


#### Windows

mvnw.cmd spring-boot:run


---

## 📊 Documentação da API

Após rodar o projeto, acesse:


http://localhost:8080/swagger-ui.html


ou


http://localhost:8080/swagger-ui/index.html


---

## 🧪 Testes

O projeto possui estrutura preparada para testes unitários e de integração.

(Em evolução)

---

## 📌 Melhorias futuras

- Autenticação e autorização com JWT  
- Banco de dados PostgreSQL  
- Deploy em cloud (AWS / Railway / Render)  
- Testes automatizados mais completos  
- Integração com front-end  

---

## 💡 Diferenciais do projeto

- Baseado em cenário real de negócio  
- Aplicação de regras de priorização e vencimento  
- Estrutura escalável e organizada  
- Foco em clareza e manutenção de código  

---

## 📸 Preview da API (Swagger)

![Swagger Preview](./swagger-preview.png)

## 👨‍💻 Autor

Leonardo Silva Esteves  
🔗 https://github.com/leodev-est  
🔗 https://www.linkedin.com/in/leonardo-silva-esteves  

---

## ⭐ Considerações finais

Este projeto faz parte do meu portfólio com foco em desenvolvimento backend e evolução contínua como desenvolvedor.

Se esse projeto te chamou atenção, fico à disposição para trocar ideias ou falar mais sobre minha experiência 🙂
