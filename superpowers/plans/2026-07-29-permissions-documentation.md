# In-Game Permissions Documentation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Update the existing In-Game Permissions documentation so it accurately describes the current REST runtime, environment-aware policy, project synchronization, identity propagation, and security boundaries.

**Architecture:** Keep the existing five-page Mintlify information architecture. Update each page according to its current responsibility, use the current `main` branches of the implementation repositories as the source of truth, and document only public architecture plus security-relevant operational behavior.

**Tech Stack:** Mintlify, MDX, Mermaid, Kotlin examples, YAML workload manifests

## Global Constraints

- Keep the existing five-page In-Game Permissions navigation unchanged.
- Document public architecture and security boundaries, not internal resource names or deployment parameters.
- Use `global < environment < server type < server` as the exact scope precedence.
- Describe the runtime as authenticated REST; remove all gRPC configuration and topology claims.
- Never include credentials, tokens, internal hostnames, service-account names, or cluster-specific resource names.
- Do not present planned behavior as deployed behavior.
- Follow `AGENTS.md` and `docs/style/mintlify-writing.md`.

---

### Task 1: Update the permission model and administration workflow

**Files:**
- Modify: `reference/plugins/in-game-permissions/index.mdx:6-76`
- Modify: `reference/plugins/in-game-permissions/administration.mdx:6-107`

**Interfaces:**
- Consumes: Current service scope semantics and current Portal project access and import behavior.
- Produces: The terminology and scope precedence reused by the runtime, catalog, and infrastructure pages.

- [ ] **Step 1: Update the overview topology and model**

In `index.mdx`:

- Change the infrastructure card copy from global/project-only wording to production, stage, and project environments.
- Add an `Environment` row to the authorization model table.
- Change the grant definition to include global, environment, server-type, and server scopes.
- Explain that production owns platform policy, stage provides an isolated pre-production policy environment, and each project owns project-local policy.
- Replace the current precedence sentence with:

```mdx
Server scope wins over server-type scope, server-type scope wins over environment
scope, and environment scope wins over global scope. A deny wins between equally
specific candidates.
```

- Keep the distinction between Portal access and in-game permissions.

- [ ] **Step 2: Document administration access and environment grants**

In `administration.mdx`:

- Add an introductory `Info` callout explaining that project owners and editors can manage project permissions while project viewers have read-only access.
- State that production permission administration uses separate platform authorization.
- Add an `Environment` row to the role-grant table with `stage` and `prod` as examples.
- Update effective-access guidance so administrators verify environment, server type, and server context.

- [ ] **Step 3: Add the global-to-project import workflow**

Add a `## Import production policy into a project` section before Audit. It must explain:

1. Portal previews the current production snapshot against current project state.
2. The user resolves each conflict by keeping the project value, using the production value, skipping the change, or removing the project value when offered.
3. The import rejects a stale preview if project state changes before confirmation.
4. Roles, role grants, inheritance, and catalog entries are included.
5. Keycloak group mappings are optional and remain project-local when the checkbox is disabled.
6. Direct player role assignments and direct player permission grants are never imported.

Use a `Warning` callout for the exclusion of player-specific grants and a `Steps` component for the procedure.

- [ ] **Step 4: Validate and review the model pages**

Run:

```bash
mintlify validate
git diff --check
rg -n "global.*server-type|server-type.*global|project owners|player-specific" reference/plugins/in-game-permissions/{index,administration}.mdx
```

Expected: Mintlify validation passes, `git diff --check` prints nothing, and the search results show the new precedence, access, and import explanations.

- [ ] **Step 5: Commit the model and administration update**

```bash
git add reference/plugins/in-game-permissions/index.mdx reference/plugins/in-game-permissions/administration.mdx
git commit -m "docs: update permission administration model"
```

---

### Task 2: Replace gRPC runtime documentation with authenticated REST

**Files:**
- Modify: `reference/plugins/in-game-permissions/runtime-integration.mdx:6-108`

**Interfaces:**
- Consumes: Forge service declaration schema, plugin-permissions runtime configuration, and the terminology established in Task 1.
- Produces: The authoritative workload integration and security configuration linked from the catalog and infrastructure pages.

- [ ] **Step 1: Add the Forge least-privilege declaration**

Replace the gRPC warning with a prerequisites section that explains managed workloads declare their permissions access in the Forge manifest. Include this complete example:

```yaml
services:
  permissions:
    version: v1
    access:
      - capability: snapshot:read
      - capability: catalog:register
        resources:
          - example-lobby
```

Explain that `snapshot:read` allows player snapshot reads, `catalog:register` allows replacement of only the listed manifest sources, and omitting access grants no permission-service access.

- [ ] **Step 2: Replace the runtime configuration table**

Document these exact variables:

| Variable | Required | Managed behavior | Purpose |
| --- | --- | --- | --- |
| `PERMISSIONS_SERVICE_URL` | Yes when enabled | Injected | Base URL of the permission instance assigned to the workload. |
| `PERMISSIONS_TOKEN_FILE` | Yes when enabled | Injected | Path to the projected workload token. |
| `GROUNDS_PERMISSION_SERVER_TYPE` | No | Runtime-specific default | Resolves server-type-scoped grants. |
| `GROUNDS_PERMISSION_SERVER_ID` | No | Unset | Resolves grants scoped to one server. |
| `GROUNDS_PERMISSION_ENVIRONMENT` | No | Set by the platform where applicable | Resolves `stage` or `prod` environment grants. |
| `PERMISSIONS_REFRESH_INTERVAL_SECONDS` | No | `60` | Refresh interval for online-player snapshots. |

Add these constraints:

- Managed workloads must not override the injected URL or token path to call a central or foreign project instance.
- If only one of `PERMISSIONS_SERVICE_URL` and `PERMISSIONS_TOKEN_FILE` is set, startup fails.
- The client rereads the token file per request so token rotation does not require a restart.

- [ ] **Step 3: Retain and verify Velocity and Minestom examples**

Keep the existing `Permissions.hasPermission` examples. Update surrounding text to say the snapshot is fetched through REST and evaluated locally. State that the configured environment, server type, and server ID form the default local check scope.

- [ ] **Step 4: Document runtime failure behavior**

Keep the existing snapshot lifecycle but clarify:

- Login fetches a snapshot through the private runtime REST API.
- Refresh failure retains a still-valid snapshot.
- Expired or missing snapshots deny checks.
- Login fails closed when no valid snapshot can be obtained.
- Callers treat `false` as a local authorization decision and do not make their own remote retries.

Do not publish internal endpoint hostnames or token examples.

- [ ] **Step 5: Validate and scan for obsolete runtime claims**

Run:

```bash
mintlify validate
git diff --check
rg -n "PERMISSIONS_GRPC_TARGET|gRPC endpoint|local gRPC" reference/plugins/in-game-permissions/runtime-integration.mdx
rg -n "PERMISSIONS_SERVICE_URL|PERMISSIONS_TOKEN_FILE|snapshot:read|catalog:register" reference/plugins/in-game-permissions/runtime-integration.mdx
```

Expected: validation passes; the obsolete-term search returns no matches; the REST and capability search finds all four required terms.

- [ ] **Step 6: Commit the REST runtime update**

```bash
git add reference/plugins/in-game-permissions/runtime-integration.mdx
git commit -m "docs: document permissions REST runtime"
```

---

### Task 3: Align catalog registration with workload authorization

**Files:**
- Modify: `reference/plugins/in-game-permissions/permission-catalog.mdx:6-79`

**Interfaces:**
- Consumes: The Forge permissions declaration documented in Task 2 and plugin manifest validation rules.
- Produces: A complete, least-privilege catalog registration workflow.

- [ ] **Step 1: Link manifest sources to declared access**

After the manifest example, state that its `source` must exactly match one of the `resources` under the workload's `catalog:register` capability. Link to the runtime integration page for the complete Forge declaration.

Add this safe paired excerpt:

```yaml
- capability: catalog:register
  resources:
    - example-lobby
```

Explain that this grants registration for `example-lobby`, not arbitrary sources.

- [ ] **Step 2: Complete the manifest scope and validation guidance**

- Preserve the non-empty manifest, unique keys, non-empty key, label, description, and supported-scope requirements.
- Keep the manifest scope table limited to the currently supported `GLOBAL`, `SERVER_TYPE`, and `SERVER` values. Explain environment-scoped policy on the administration page, not as a plugin manifest scope.
- State that each entry must declare only scopes the consuming workload evaluates.

- [ ] **Step 3: Clarify asynchronous registration behavior**

Explain that discovered manifests register asynchronously after startup. Connection failures, rate limits, and server failures are retried with bounded backoff; invalid manifests and other client errors require a corrected workload deployment. Keep the existing statement that one invalid manifest does not prevent remaining valid manifests from registering.

- [ ] **Step 4: Validate and commit the catalog update**

Run:

```bash
mintlify validate
git diff --check
rg -n "example-lobby|catalog:register|GLOBAL|asynchronously" reference/plugins/in-game-permissions/permission-catalog.mdx
```

Expected: validation passes, whitespace validation is clean, and the source authorization, environment scope, and registration behavior are present.

Then commit:

```bash
git add reference/plugins/in-game-permissions/permission-catalog.mdx
git commit -m "docs: clarify permission catalog registration"
```

---

### Task 4: Document deployment topology and security boundaries

**Files:**
- Modify: `reference/plugins/in-game-permissions/infrastructure.mdx:6-89`

**Interfaces:**
- Consumes: The environment model from Task 1 and runtime security model from Task 2.
- Produces: The public architecture and security overview for all permission environments.

- [ ] **Step 1: Replace the topology diagram**

Create a Mermaid flowchart with these public components and flows:

- Keycloak to durable identity stream
- Stream to production instance
- Stream to Forge identity relay to project broker to project instance
- Portal to production administration API
- Portal through Forge project proxy to project instance
- Project instance to project runtimes
- Stage instance to stage runtimes
- Production instance to production runtimes

Distinguish browser-facing administration paths from private runtime paths in labels. Do not name clusters, service accounts, internal hosts, or Kubernetes resources.

- [ ] **Step 2: Document all three deployment shapes**

Add sections for:

- Production: platform policy and identity projection; its own private runtime endpoint for production workloads.
- Stage: isolated pre-production policy and private runtime endpoint; periodic identity reconciliation currently provides freshness.
- Projects: project-local policy and identity projection; Portal reaches it through Forge's authorized proxy; local runtimes use its private REST endpoint.

State that a runtime uses the permission instance assigned to its own environment and never calls another environment directly.

- [ ] **Step 3: Document event propagation and recovery**

Use a `Steps` component covering:

1. Keycloak publishes a minimal user-scoped invalidation after an identity change.
2. The durable central stream delivers it to the production consumer and Forge relay.
3. Forge relays project-relevant invalidations to project brokers.
4. Permission instances refresh identity projections and issue newer snapshots.
5. Periodic reconciliation recovers delayed, missed, or unsupported events.

Add an `Info` callout explaining that stage currently relies on periodic reconciliation for identity freshness.

- [ ] **Step 4: Document security boundaries**

Add a concise list stating:

- Administration APIs and private runtime APIs are separate surfaces.
- Runtime routes are not exposed as public browser endpoints.
- Managed workloads authenticate with short-lived projected tokens read from files.
- Authorization is limited to declared snapshot reads and exact catalog sources.
- Portal project requests pass through Forge authorization; project owner/editor access is writable and viewer access is read-only.
- Keycloak events carry identifiers and invalidation reasons, not credentials or full permission policy.
- Missing or expired snapshots fail closed.

- [ ] **Step 5: Validate the full permissions section**

Run:

```bash
mintlify validate
git diff --check
rg -n "PERMISSIONS_GRPC_TARGET|gRPC endpoint|local gRPC" reference/plugins/in-game-permissions
rg -n "Production|Stage|project|projected token|private runtime|reconciliation" reference/plugins/in-game-permissions/infrastructure.mdx
```

Expected: Mintlify validation passes, no whitespace errors or obsolete gRPC claims remain, and all three environments plus security and recovery concepts are present.

- [ ] **Step 6: Commit the infrastructure update**

```bash
git add reference/plugins/in-game-permissions/infrastructure.mdx
git commit -m "docs: update permissions infrastructure"
```

---

### Task 5: Perform final cross-page review

**Files:**
- Review: `reference/plugins/in-game-permissions/index.mdx`
- Review: `reference/plugins/in-game-permissions/administration.mdx`
- Review: `reference/plugins/in-game-permissions/runtime-integration.mdx`
- Review: `reference/plugins/in-game-permissions/permission-catalog.mdx`
- Review: `reference/plugins/in-game-permissions/infrastructure.mdx`

**Interfaces:**
- Consumes: All preceding documentation changes.
- Produces: A validated, internally consistent documentation section ready for pull-request review.

- [ ] **Step 1: Check terminology and links**

Confirm every page uses `In-Game Permissions`, `production`, `stage`, `project`, `runtime`, `manifest source`, and `environment scope` consistently. Confirm all relative Mintlify links target existing pages.

- [ ] **Step 2: Check examples against implementation sources**

Compare the final examples with current `main`:

- `grounds-forge`: permissions service declaration and capability rules
- `plugin-permissions`: environment variables, manifest model, and Kotlin API
- `service-permissions`: scope precedence, REST behavior, sync contents, and authentication boundaries
- `grounds-portal`: project role access and global import behavior
- Infrastructure repositories: production, stage, and project topology

Correct any statement that differs from deployed `main` behavior.

- [ ] **Step 3: Run final verification**

```bash
mintlify validate
git diff --check
git status -sb
```

Expected: Mintlify reports `success build validation passed`, `git diff --check` prints nothing, and the status contains only intentional plan or documentation changes.

- [ ] **Step 4: Commit any final corrections**

If the review changed content:

```bash
git add reference/plugins/in-game-permissions
git commit -m "docs: refine permissions documentation"
```

If no correction was needed, do not create an empty commit.
