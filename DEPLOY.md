# Deploy

Este projeto esta preparado para deploy em producao no Render com banco PostgreSQL gerenciado.

## O que ja esta pronto

- Aplicacao Spring Boot empacotada por `Dockerfile`
- Porta dinamica suportada via `PORT`
- Perfil de producao via `SPRING_PROFILES_ACTIVE=prod`
- PostgreSQL configurado por variaveis de ambiente
- JWT configurado por variaveis de ambiente
- Swagger e OpenAPI disponiveis para demonstracao
- Blueprint do Render em `render.yaml`

## Variaveis de ambiente usadas em producao

- `PORT`
- `SPRING_PROFILES_ACTIVE`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `SPRING_JPA_DATABASE_PLATFORM`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `JWT_SECRET`
- `JWT_EXPIRATION`

## Publicando no Render

1. Envie o repositorio para o GitHub.
2. No Render, escolha a opcao de criar recursos via Blueprint.
3. Selecione este repositorio e confirme o arquivo `render.yaml`.
4. Aguarde a criacao do banco `pendencias-manager-db`.
5. Aguarde o build e o deploy do servico `pendencias-manager-api`.
6. Copie a URL publica gerada pelo Render e atualize o `README.md`.

## URLs apos a publicacao

Substitua `SEU-SERVICO` pelo hostname real do Render:

- API: `https://SEU-SERVICO.onrender.com`
- Swagger UI: `https://SEU-SERVICO.onrender.com/swagger-ui/index.html`
- OpenAPI: `https://SEU-SERVICO.onrender.com/v3/api-docs`

## Validacao recomendada

1. Abrir o Swagger UI.
2. Chamar `POST /auth/login` com um usuario valido.
3. Copiar o token JWT retornado.
4. Testar endpoints protegidos com `Authorization: Bearer {token}`.
5. Criar ou alterar registros e verificar persistencia apos reinicio do servico.

## Observacoes

- O perfil `prod` exige variaveis reais de banco e JWT.
- Credenciais nao devem ser versionadas no repositorio.
- O H2 console permanece desabilitado em producao.
- Se quiser ambiente local com PostgreSQL, continue usando `docker-compose.yml`.
