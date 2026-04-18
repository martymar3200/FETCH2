# Build

Build scripts, infrastructure provisioning, and cluster management for the FETCH2 application.

> **ℹ️ Status: Infrastructure Configs**
>
> This directory contains deployment and operations tooling inherited from the original FETCH system. These configurations may need customization for your specific environment.

## Contents

### Ansible (`ansible/`)
Ansible playbooks for provisioning and configuring infrastructure:

- **`core/`** — Base server provisioning (system tools, monitoring agents, Postfix)
- **`haproxy/`** — Load balancer installation and configuration
- **`inventory/`** — Environment-specific host inventory files (`dev.ini`, `test.ini`, `stage.ini`, `prod.ini`)
- **`k8s/`** — Kubernetes cluster setup (core install, pod networking, custom rules)
- **`podman/`** — Podman container runtime management

### Docker (`docker/`)
- **`prometheus-grafana/`** — Monitoring stack with Prometheus and Grafana via Docker Compose. Includes per-environment scrape configs (`prometheus-dev.yml`, `prometheus-test.yml`).

### Tools (`tools/`)
- **`k8s/`** — Kubernetes management utilities
- **`sonarqube/`** — SonarQube code quality server via Docker Compose

## Kubernetes

[Kubernetes: User Certs and Roles](ansible/k8s/README.md)