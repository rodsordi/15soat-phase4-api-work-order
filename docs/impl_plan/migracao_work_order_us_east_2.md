# Plano de Implementação: Adaptação e Deploy do Microsserviço api-work-order em us-east-2

> **Decisão do Usuário**: Opção 1 - Alinhada às ADRs / GitOps & Free Tier us-east-2
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo

O microsserviço `api-work-order` gerencia o ciclo de vida das ordens de serviço, clientes e veículos da oficina mecânica. Com o provisionamento bem-sucedido das camadas de infraestrutura base (Kubernetes EKS e Apache Kafka em `iac-k8s`, banco de dados RDS PostgreSQL em `iac-db` e camada serverless em `iac-lamda`), este plano detalha as alterações necessárias para o empacotamento, configuração e deploy automatizado do microsserviço no cluster EKS da conta AWS Free Tier na região `us-east-2`.

---

## 2. Alterações Técnicas

### 2.1 Helm Chart (`helm/values.yaml`)
- Atualizar `image.repository` para o repositório ECR da conta Free Tier: `231268219488.dkr.ecr.us-east-2.amazonaws.com/15soat-phase4-api-work-order`.
- Atualizar `SPRING_DATASOURCE_URL` para o endpoint real do RDS PostgreSQL provisionado em `us-east-2`:
  `jdbc:postgresql://work-order-db-prd.c3yq8yiqukwa.us-east-2.rds.amazonaws.com:5432/work_order_db`.
- Manter `KAFKA_BOOTSTRAP_SERVERS` apontando para o serviço KRaft interno do EKS: `kafka-cluster-kafka-bootstrap.infra-kafka.svc.cluster.local:9092`.

### 2.2 Pipeline CI/CD (`.github/workflows/ci.yml`)
- Atualizar o fallback de `AWS_REGION` de `'us-east-1'` para `'us-east-2'` na linha 97.
- Garantir a sincronização do segredo `work-order-db-prd-credentials-prd` a partir do AWS Secrets Manager em `us-east-2` para o segredo Kubernetes `work-order-db-credentials` no namespace `default`.

---

## 3. Roteiro de Execução

1. Criar branch `feature/freetier-work-order-us-east-2`.
2. Aplicar modificações em `helm/values.yaml` e `.github/workflows/ci.yml`.
3. Executar validação de compilação e testes unitários locais com Maven (`mvn test`).
4. Submeter as alterações à auditoria do subagente `Code Reviewer Sentinel` (Diretriz Global #7).
5. Solicitar permissão explícita do usuário para commit e push (Diretriz Global #1).
6. Abrir Pull Request para a branch `master` e realizar merge com exclusão da branch de feature.
7. Monitorar a esteira no GitHub Actions até o rollout completo do deployment e publicação da release Helm.
