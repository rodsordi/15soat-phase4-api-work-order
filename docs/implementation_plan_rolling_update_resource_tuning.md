# Plano de Implementação: Calibração de RollingUpdate e Requests de CPU no Helm Chart

> **Decisão do Usuário**: Opção 1 - Calibrar strategy.rollingUpdate e resources.requests no Helm
> **Status**: Aprovado para Execução

## Contexto e Objetivo
Durante o deploy do microserviço `15soat-phase4-api-work-order` no cluster Amazon EKS (`fiap-workshop-eks`), o passo de verificação do rollout excedeu o tempo limite (`180s`) porque o novo pod ficou retido no estado `Pending` com a mensagem `FailedScheduling: 0/1 nodes are available: 1 Insufficient cpu`.

O cluster possui 1 nó ativo com 1930m de CPU alocável, dos quais 1900m já se encontram alocados. A estratégia padrão de Deployment do Kubernetes opera com `maxUnavailable: 0` (para 2 réplicas) e `maxSurge: 1`, forçando a criação simultânea de um 3º pod solicitando 250m de CPU antes de encerrar os pods antigos, gerando um deadlock de capacidade.

## Escopo de Alterações

### 1. `helm/templates/deployment.yaml`
Adicionar a especificação explícita de estratégia `RollingUpdate` com `maxUnavailable: 1` e `maxSurge: 0`:
```yaml
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 0
```
Isso garante que o Kubernetes encerre 1 pod antigo primeiro (liberando imediatamente CPU e memória no nó) antes de iniciar o pod com a nova versão.

### 2. `helm/values.yaml`
Calibrar os recursos de CPU solicitados em repouso:
- `requests.cpu`: de `250m` para `100m`
- `limits.cpu`: manter `1000m`
- `requests.memory`: `512Mi`
- `limits.memory`: `1024Mi`

## Etapas de Execução
1. Atualizar `helm/templates/deployment.yaml` e `helm/values.yaml`.
2. Validar a sintaxe do Helm com `helm lint ./helm`.
3. Submeter as alterações via `git commit` e `git push origin master`.
4. Monitorar a execução da esteira no GitHub Actions (`gh run watch`).
5. Confirmar que o rollout no Kubernetes atinge 100% de sucesso (`kubectl rollout status deployment/api-work-order`).
6. Proceder com a execução dos testes BDD E2E em modo Black-Box.
