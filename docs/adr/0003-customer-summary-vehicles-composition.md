# ADR 0003: Composição Pragmática de Veículos em Resumo de Clientes para Usabilidade Operacional de Balcão

* **Status**: Aceito (Accepted)
* **Data**: 2026-10-08
* **Escopo**: Específico do Microsserviço `15soat-phase4-api-work-order`
* **Autores**: FIAP SOAT Tech Challenge Team
* **Decisores Técnicos**: Especialistas em Arquitetura de Software e Engenharia de Domínio

---

## 1. Contexto e Declaração do Problema

Tradicionalmente, diretrizes acadêmicas de design RESTful recomendam que schemas de resumo (*Summary*), empregados em coleções e buscas paginadas (como `GET /api/v1/customers`), sejam estritamente planos (*flat*), reservando arrays e agregações filhas exclusivamente para endpoints de detalhes unitários (*Detailed* - `GET /api/v1/customers/{id}`). O objetivo teórico dessa separação é evitar *over-fetching* de rede e o problema de consultas N+1 no banco de dados.

No entanto, a análise do cenário real de negócio de uma oficina mecânica revelou necessidades operacionais específicas:
1. **Fluxo do Atendente de Balcão**: Ao receber um cliente, o atendente busca pelo nome ou documento para iniciar o atendimento. Se a listagem não exibir os veículos, o atendente é forçado a clicar no cliente ou disparar buscas separadas para confirmar a placa do carro que deu entrada na oficina.
2. **Baixa Cardinalidade da Relação (1:N Reduzido)**: Em uma oficina, um cliente físico ou jurídico possui em média 1 a 3 veículos cadastrados. Não há cenários de centenas de itens por cliente.
3. **Padrão de Acesso Esporádico (Dados Mestre)**: Cadastros de clientes e veículos são operações pontuais (realizadas uma única vez na primeira visita e consultadas de forma espaçada no retorno para novas ordens de serviço).

---

## 2. Decisão Arquitetural

Decidimos adotar uma abordagem de **Pragmatismo de Domínio**, flexibilizando o purismo teórico para priorizar a usabilidade e a experiência do usuário de balcão (UX):

1. **Composição Sumarizada em `CustomerSummaryResponse`**:
   - Incluir a propriedade `vehicles` no schema `CustomerSummaryResponse`:
     ```yaml
     vehicles:
       type: array
       description: Lista de veículos vinculados ao cliente
       items:
         $ref: '#/components/schemas/CustomerVehicleItemResponse'
     ```
2. **Schema Dedicado Enxuto (`CustomerVehicleItemResponse`)**:
   - A lista de veículos dentro do cliente reutiliza o schema de item dedicado, contendo apenas os atributos indispensáveis de identificação do carro (`id`, `licensePlate`, `make`, `model`, `manufactureYear`, `createdAt`).
   - O campo `customerDocument` é propositalmente omitido de cada item para evitar redundância, visto que o cliente já é a própria raiz do recurso.
3. **Consulta Otimizada na Camada de Persistência**:
   - Para mitigar riscos de N+1 queries no Hibernate/JPA durante a paginação de clientes, as consultas de listagem com veículos devem utilizar `@EntityGraph(attributePaths = {"vehicles"})` ou projeções otimizadas.

---

## 3. Consequências

### Positivas:
* **Eficiência Operacional no Atendimento**: O atendente identifica imediatamente a placa e o modelo do veículo do cliente diretamente na tela de busca rápida, sem múltiplos cliques ou navegações adicionais.
* **Redução de RTT (Round-Trip Time) da Aplicação**: A interface do usuário não precisa emitir chamadas HTTP adicionais de detalhe apenas para descobrir a frota de um cliente.
* **Payload Controlado e Limpo**: Como a cardinalidade é baixa e o schema de item é enxuto, o impacto no tamanho da resposta HTTP é desprezível (poucos bytes adicionais por cliente).

### Negativas / Trade-offs:
* **Atenção à Paginação no Banco**: Exige consultas com *fetch join* ou *EntityGraph* controlados para que o Hibernate não aplique paginação em memória caso o volume de registros aumente expressivamente no futuro.
