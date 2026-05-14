# Build Docs Developer Journey Design

## Goal

Restructure the Build docs around the internal developer journey while
preserving all existing information.

The Build section should answer the questions internal developers ask while
working in real Grounds repositories:

1. How do I set up my machine and repos?
2. How do I push one plugin?
3. How do I work across independent plugin repositories?
4. How do I test multiple plugins together?
5. How do I share a staging preview?
6. How do I debug a failed push?
7. Where is the exact reference for fields, flags, tasks, and config files?

## Audience

Primary audience: internal Grounds developers working across multiple
independent plugin repositories.

Secondary audience: external or future developers who need the same reference
material. Their path remains supported through the reference pages, but the
Build navigation prioritizes internal workflows.

## Non-goals

- Do not remove existing Build information.
- Do not rewrite the Deploy or Reference tabs.
- Do not change product behavior or code.
- Do not make the Build docs a single long tutorial.
- Do not duplicate flag tables, manifest field tables, or config precedence
  tables across workflow pages.

## Content Preservation Rule

All current Build information must remain available after the restructure.

Existing reference pages should keep their detailed content:

- `build/cli/*`
- `build/gradle-plugin/*`
- `build/manifest`
- `build/concepts/*`
- `build/portal/*`
- `build/ci-tokens`
- `build/observability`
- `build/troubleshooting`
- `build/test-environment/*`

Workflow pages may summarize and link to those references, but the reference
pages remain authoritative for exact flags, fields, Gradle properties, and
configuration precedence.

If content moves, the source page must keep either the detailed reference
content or a clear link to the new workflow page.

## Canonical Internal Sample

Use one recurring real sample for workflow pages:

```text
~/grounds/
  sample-plugin-workspace/
    app/
      grounds.yaml
  plugin-agones/
  plugin-player/
```

The sample app is a Velocity plugin stack:

```yaml
name: sample-velocity-plugins
type: plugin-velocity
baseImage: velocity
plugins:
  - id: plugin-agones
    variant: velocity
    source: github:groundsgg/plugin-agones@v0.5.0
  - id: plugin-player
    variant: velocity
    source: github:groundsgg/plugin-player@v0.1.0
```

The workspace maps local sibling repositories:

```bash
grounds workspace add plugin-agones ~/grounds/plugin-agones \
  --variant velocity \
  --artifact velocity/build/libs/plugin-agones-velocity.jar \
  --build './gradlew :velocity:shadowJar'

grounds workspace add plugin-player ~/grounds/plugin-player \
  --variant velocity \
  --artifact velocity/build/libs/plugin-player-velocity.jar \
  --build './gradlew :velocity:shadowJar'
```

Use fictional names such as `plugin-chat` and `plugin-permissions` only in
short reference snippets where concrete repository behavior is irrelevant.

## Navigation Design

The Build tab should become journey-first:

```text
Build
├─ Overview                         build/index
├─ Quickstart                       build/quickstart
├─ Workflows
│  ├─ Local development             build/local-development
│  ├─ Local plugin workspaces       build/local-plugin-workspaces
│  ├─ Multi-plugin stacks           build/multi-plugin-stacks
│  ├─ Staging previews              build/share-staging-previews
│  └─ Debug pushes                  build/debug-pushes
├─ Concepts
│  ├─ Projects and teams
│  ├─ Targets
│  ├─ Pushes
│  ├─ Base images
│  └─ Preview environments
├─ Reference
│  ├─ Manifest reference            build/manifest
│  ├─ CLI
│  │  ├─ Installation
│  │  ├─ Auth
│  │  ├─ Push
│  │  ├─ Workspace
│  │  ├─ Preview
│  │  └─ Configuration
│  ├─ Gradle plugin
│  │  ├─ Setup
│  │  ├─ groundsPush
│  │  ├─ groundsPushRetry
│  │  └─ groundsTestLocal
│  ├─ CI tokens
│  ├─ Portal
│  │  ├─ Overview
│  │  ├─ Control center access
│  │  └─ Control center step-up
│  └─ Observability
├─ Troubleshooting                  build/troubleshooting
└─ Internal test environment
   ├─ Overview
   └─ Bundle reference
```

Keep existing URLs stable where possible. Prefer navigation moves over file
renames.

## Page Responsibilities

### `build/index`

Role: Build section landing page.

It should state that Build docs are now optimized for internal developer
workflows and point readers to the quickstart, workflows, and reference areas.
It should keep the existing high-level value proposition and under-the-hood
summary.

### `build/quickstart`

Role: internal developer quickstart from a fresh workspace to a first useful
push.

It should include:

1. Prerequisites: account, CLI, Java/Gradle, GitHub Packages token.
2. Clone or arrange repositories under one parent directory.
3. Log in with `grounds login`.
4. Configure `grounds-push` credentials for GitHub Packages.
5. Push a single plugin or app with release/default sources.
6. Add workspace mappings for `plugin-agones` and `plugin-player`.
7. Run `grounds push --with-local`.
8. Check the push in Portal.
9. Link to workflow and reference pages.

The current public first-push information should be preserved in this page or
linked from it.

### `build/local-development`

Role: local-only server loop with `groundsTestLocal`.

Keep existing content. Move it into the Workflows group and add links to the
workspace and multi-plugin workflow pages for cases where local-only testing is
not enough.

### `build/local-plugin-workspaces`

Role: narrative workflow for local overrides across independent repositories.

It should use the canonical `plugin-agones` + `plugin-player` Velocity example
and show:

- why release sources remain in `grounds.yaml`
- where `workspace.yaml` lives
- `workspace scan`
- explicit `workspace add`
- `workspace list`
- `workspace doctor`
- `grounds push --local`
- `grounds push --with-local`
- source table interpretation

It should link to `build/cli/workspace` for exact command reference.

### `build/multi-plugin-stacks`

Role: explain how to test multiple plugins together.

It should cover:

- `plugins:` vs `jar:`
- structured entries with `id`, `variant`, and `source`
- release defaults
- local overrides
- Paper/Velocity/gamemode support
- bundle upload shape
- 100 MB cap
- what goes to Portal/Forge

It should link to `build/manifest` for exact field rules.

### `build/share-staging-previews`

Role: workflow for sharing builds through staging previews.

It should preserve and compose information from the current quickstart and
preview docs:

- `grounds push --target=staging`
- preview TTL
- portal inspection
- pinning previews
- when to use staging instead of dev

It should link to `build/cli/preview` and
`build/concepts/preview-environments`.

### `build/debug-pushes`

Role: developer workflow for failed pushes.

It should cover:

- reading CLI output
- interpreting local override source tables
- checking `workspace doctor`
- checking Gradle task failures
- checking Portal push/app views
- using `grounds logs`
- using `grounds push retry`
- common failure categories

It should link to `build/troubleshooting`, `build/cli/push`,
`build/gradle-plugin/groundsPush`, and `build/portal/index`.

### Reference pages

Role: detailed authoritative references.

Keep existing content and update cross-links only where needed:

- CLI pages contain exact flags and config behavior.
- Gradle pages contain exact task inputs and behavior.
- Manifest contains exact schema and constraints.
- Concepts explain platform concepts.
- Portal pages explain Portal behavior.
- Troubleshooting remains the deeper error catalog.

## Validation

Run:

```bash
mintlify validate
git diff --check
rg -n "50 MB|0\\.5\\.0|/build/cli\\)|/build/concepts\\)" build docs.json
```

The `rg` command is a stale-content/link smoke check. Any match should be
reviewed rather than blindly removed.

Manual review:

- `docs.json` navigation follows the journey-first map.
- Every existing Build page is still reachable.
- New workflow pages link to reference pages instead of copying large tables.
- The quickstart includes repository setup.
- The canonical internal sample is used consistently.

