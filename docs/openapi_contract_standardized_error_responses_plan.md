# Plano de Implementação: Padronização de Respostas de Erro com Componentes Globais Reutilizáveis (RFC 7807/9457)

> **Decisão do Usuário**: Opção 1 - Reversão para Componentes Reutilizáveis ($ref) com Schema ProblemDetail Limpo e Neutro
> **Status**: Concluído e Validado

---

## 1. Contexto e Objetivo
Para garantir conformidade arquitetural com o princípio DRY (*Don't Repeat Yourself*), eliminar redundâncias e assegurar máxima robustez e velocidade de renderização no Swagger UI e ferramentas de modelagem (como IntelliJ OpenAPI Preview), todas as respostas de erro HTTP das 25 operações da API foram padronizadas referenciando os componentes globais em `#/components/responses/*`.

Adicionalmente, o schema canônico `ProblemDetail` (RFC 7807 / RFC 9457) foi higienizado com um exemplo neutro, universal e 100% alinhado com as respostas reais emitidas pela biblioteca compartilhada `mod-web` (`CommonsExceptionAdvice.java`).

Os múltiplos exemplos interativos em Combo Box (`examples` map) nas requisições (`requestBody`) das operações de negócio permanecem preservados integralmente.

---

## 2. Escopo das Alterações Concluídas

1. **Reversão das Respostas nas Rotas (`paths`)**:
   - Todas as operações em `openapi.yaml` agora utilizam estritamente as referências globais padronizadas:
     - `400`: `$ref: '#/components/responses/BadRequest'`
     - `401`: `$ref: '#/components/responses/Unauthorized'`
     - `403`: `$ref: '#/components/responses/Forbidden'`
     - `404`: `$ref: '#/components/responses/NotFound'`
     - `409`: `$ref: '#/components/responses/Conflict'`
     - `422`: `$ref: '#/components/responses/UnprocessableEntity'`
     - `500`: `$ref: '#/components/responses/InternalServerError'`

2. **Descrições Canônicas em `components.responses`**:
   - `BadRequest`: *"Requisição Inválida (Bad Request) - Violação de validação ou argumentos incorretos"*
   - `Unauthorized`: *"Não Autenticado (Unauthorized) - Credenciais ausentes ou token expirado"*
   - `Forbidden`: *"Acesso Negado (Forbidden) - Permissões insuficientes para a operação"*
   - `NotFound`: *"Recurso Não Encontrado (Not Found) - Identificador inexistente na base de dados"*
   - `Conflict`: *"Conflito de Estado ou Duplicidade (Conflict) - Recurso já existente ou estado incompatível"*
   - `UnprocessableEntity`: *"Erro de Regra de Negócio (Unprocessable Entity) - Violação de invariantes de domínio"*
   - `InternalServerError`: *"Erro Inesperado do Servidor (Internal Server Error) - Falha interna não tratada"*

3. **Higienização do Schema `ProblemDetail`**:
   - Removidos exemplos parciais com valores acoplados ou divergentes.
   - Adicionado exemplo universal e neutro na raiz do schema refletindo fielmente a RFC 7807/9457 e o runtime do Spring Boot.

---

## 3. Validação e Qualidade
- **Parser YAML**: 100% válido (verificado via PyYAML e SnakeYAML).
- **Testes Automatizados**: Suíte Maven executada com sucesso (**43 testes verdes**, 0 falhas, `BUILD SUCCESS`).
- **Isolamento de Código**: Nenhum arquivo Java foi alterado; escopo restrito ao contrato OpenAPI.
