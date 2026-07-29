# In-Game Permissions documentation update

## Goal

Bring the existing In-Game Permissions documentation in line with the current permissions platform. The documentation should explain how administrators, plugin developers, and platform users interact with permissions without exposing internal deployment details that are not needed to use or understand the system.

## Audience

- Project owners, editors, and viewers who inspect or manage project permissions
- Platform administrators who manage production permissions
- Plugin developers who register permissions and consume runtime snapshots
- Engineers who need a public architectural and security overview

## Information architecture

Keep the existing five-page section and its current navigation:

1. Overview
2. Administration
3. Runtime integration
4. Permission catalog
5. Infrastructure

No new top-level page or navigation entry is required.

## Page changes

### Overview

- Explain the production, stage, and per-project permission environments.
- Add the environment scope to the permission model.
- Document scope precedence as `global < environment < server type < server`.
- Retain the existing explanations of roles, inheritance, direct grants, group mappings, and deny precedence.

### Administration

- Explain environment-scoped grants.
- Document project access: owners and editors can manage permissions; viewers have read-only access.
- Document the global-to-project import workflow, including preview, per-conflict resolution, target-state protection, optional Keycloak mappings, and the exclusion of player-specific grants.
- Distinguish project administration from platform production administration without documenting internal authorization identifiers.

### Runtime integration

- Replace the obsolete gRPC configuration with the authenticated REST integration.
- Document the Forge permissions service declaration and least-privilege capabilities.
- Explain the injected service URL and projected token file without showing real tokens.
- Document the server type, server ID, environment, and refresh interval configuration.
- Describe snapshot caching, refresh behavior, token rotation, and fail-closed behavior at a user-relevant level.
- Retain Velocity and Minestom usage examples and make them consistent with the current plugin API.

### Permission catalog

- Retain the requirement for a non-empty manifest, unique permission keys, descriptions, and supported scopes.
- Explain that a workload may register only manifest sources declared through `catalog:register`.
- Show that the manifest source must match the source authorized in the Forge service declaration.
- Describe asynchronous registration and retry behavior without documenting internal retry constants.

### Infrastructure

- Describe the three deployment shapes: production, stage, and per-project instances.
- Explain the separation between browser-facing administration routes and private runtime routes.
- Describe workload authentication through short-lived projected tokens and exact least-privilege authorization.
- Explain the Keycloak event flow, durable central delivery, Forge relay to project brokers, and periodic reconciliation as the recovery path.
- State the current stage behavior where periodic reconciliation provides identity freshness without presenting it as the desired permanent implementation.

## Security boundaries

The documentation must make these guarantees clear:

- Runtime endpoints are not exposed as public browser APIs.
- Workloads receive short-lived tokens through files rather than static credentials in configuration.
- Each workload receives only the snapshot and catalog-source access it declares.
- Tokens, credentials, internal service-account names, and cluster-specific resource names must not appear in examples.
- Project access follows project roles, while production administration uses separately authorized platform access.

## Deliberate exclusions

Do not document:

- Pulumi or Argo resource names and implementation ownership details
- Kubernetes ClusterRole or service-account names
- Database replica counts, backup schedules, or retention values
- Internal hostnames, broker credentials, secrets, or token values
- Planned behavior as if it were already deployed

## Verification

- Run `mintlify validate` from the documentation repository root.
- Run `git diff --check`.
- Review all examples against the current `main` branches of service-permissions, plugin-permissions, grounds-forge, grounds-portal, grounds-pulumi, deploy, charts, and library-platform-bundle.
- Search the updated section for obsolete gRPC variables and claims.
