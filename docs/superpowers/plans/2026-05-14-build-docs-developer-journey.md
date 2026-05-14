# Build Docs Developer Journey Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restructure the Build docs around the internal developer journey while preserving all current Build information.

**Architecture:** The Build section becomes journey-first in navigation, with short workflow pages for developer tasks and existing detailed pages moved under Reference. Existing URLs are preserved where practical; new pages compose and link to existing reference pages instead of duplicating flag and field tables.

**Tech Stack:** Mintlify MDX docs, `docs.json` navigation, shell validation with `mintlify validate`, `git diff --check`, and `rg`.

---

## Current Branch Context

This plan assumes work continues in `/home/lukas/grounds/docs` on branch:

```bash
feat/document-local-plugin-overrides
```

The branch already contains:

- committed design spec: `docs/superpowers/specs/2026-05-14-build-docs-developer-journey-design.md`
- uncommitted local override docs changes:
  - `build/cli/workspace.mdx`
  - `build/cli/configuration.mdx`
  - `build/cli/push.mdx`
  - `build/gradle-plugin/groundsPush.mdx`
  - `build/gradle-plugin/setup.mdx`
  - `build/manifest.mdx`
  - `docs.json`

Treat those uncommitted changes as in-scope. Do not revert them.

---

## Target File Structure

### Create

- `build/local-plugin-workspaces.mdx`  
  Journey page for local workspace overrides with the real internal Velocity sample.

- `build/multi-plugin-stacks.mdx`  
  Journey page for multi-plugin Paper/Velocity/gamemode bundles.

- `build/share-staging-previews.mdx`  
  Journey page for staging previews and sharing builds.

- `build/debug-pushes.mdx`  
  Journey page for diagnosing failed pushes and local override issues.

### Modify

- `docs.json`  
  Reorder Build navigation into Overview, Quickstart, Workflows, Concepts, Reference, Troubleshooting, Internal test environment.

- `build/index.mdx`  
  Keep current overview/value proposition, add internal developer journey entry points.

- `build/quickstart.mdx`  
  Rewrite as internal developer quickstart from fresh workspace to first local override push.

- `build/local-development.mdx`  
  Keep existing local `groundsTestLocal` information and add links to workspace/multi-plugin workflows.

- `build/cli/workspace.mdx`  
  Keep as CLI reference for exact workspace commands. Ensure it links back to the journey page.

- `build/cli/push.mdx`  
  Keep exact push flag reference. Ensure it links to workflow pages.

- `build/cli/preview.mdx`  
  Keep exact preview command reference. Ensure it links to staging preview workflow.

- `build/manifest.mdx`  
  Keep exact manifest schema/reference. Ensure structured `plugins:` and 100 MB cap are documented.

- `build/gradle-plugin/groundsPush.mdx`  
  Keep exact Gradle task reference. Ensure multi-plugin bundle behavior links to workflow page.

- `build/troubleshooting.mdx`  
  Keep detailed troubleshooting information and add links to debug workflow.

---

## Task 1: Confirm Baseline And Preserve Current Work

**Files:**
- Inspect: `docs.json`
- Inspect: `build/cli/workspace.mdx`
- Inspect: `build/quickstart.mdx`
- Inspect: `build/index.mdx`

- [ ] **Step 1: Check branch and dirty state**

Run:

```bash
cd /home/lukas/grounds/docs
git status --short --branch
```

Expected:

```text
## feat/document-local-plugin-overrides
 M build/cli/configuration.mdx
 M build/cli/push.mdx
 M build/gradle-plugin/groundsPush.mdx
 M build/gradle-plugin/setup.mdx
 M build/manifest.mdx
 M docs.json
untracked build/cli/workspace.mdx
untracked docs/superpowers/plans/2026-05-14-build-docs-developer-journey.md
```

Extra files are acceptable only if they are docs work from this branch. If unrelated files appear, stop and ask before staging or editing them.

- [ ] **Step 2: Review current navigation**

Run:

```bash
sed -n '1,180p' docs.json
```

Confirm the current Build navigation still contains all existing Build pages before restructuring.

- [ ] **Step 3: Review the current workspace reference page**

Run:

```bash
sed -n '1,260p' build/cli/workspace.mdx
```

Confirm the page has frontmatter and covers `workspace scan`, `add`, `list`, `enable`, `doctor`, `--local`, and `--with-local`.

---

## Task 2: Rewrite Build Navigation

**Files:**
- Modify: `docs.json`

- [ ] **Step 1: Replace only the Build tab `pages` array**

Edit the Build tab in `docs.json` so its `"pages"` array is exactly:

```json
[
  "build/index",
  "build/quickstart",
  {
    "group": "Workflows",
    "pages": [
      "build/local-development",
      "build/local-plugin-workspaces",
      "build/multi-plugin-stacks",
      "build/share-staging-previews",
      "build/debug-pushes"
    ]
  },
  {
    "group": "Concepts",
    "pages": [
      "build/concepts/projects-and-teams",
      "build/concepts/targets",
      "build/concepts/pushes",
      "build/concepts/base-images",
      "build/concepts/preview-environments"
    ]
  },
  {
    "group": "Reference",
    "pages": [
      "build/manifest",
      {
        "group": "CLI",
        "pages": [
          "build/cli/installation",
          "build/cli/auth",
          "build/cli/push",
          "build/cli/workspace",
          "build/cli/preview",
          "build/cli/configuration"
        ]
      },
      {
        "group": "Gradle plugin",
        "pages": [
          "build/gradle-plugin/setup",
          "build/gradle-plugin/groundsPush",
          "build/gradle-plugin/groundsPushRetry",
          "build/gradle-plugin/groundsTestLocal"
        ]
      },
      "build/ci-tokens",
      {
        "group": "Portal",
        "pages": [
          "build/portal/index",
          "build/portal/control-center-access",
          "build/portal/control-center-step-up"
        ]
      },
      "build/observability"
    ]
  },
  "build/troubleshooting",
  {
    "group": "Internal test environment",
    "pages": [
      "build/test-environment/index",
      "build/test-environment/bundle-reference"
    ]
  }
]
```

Keep all other `docs.json` tabs unchanged.

- [ ] **Step 2: Validate all Build pages are still referenced**

Run:

```bash
find build -maxdepth 3 -type f -name '*.mdx' | sort
```

Compare every current Build page against `docs.json`. The only Build MDX pages that may be absent from navigation are pages that were already intentionally absent before this task. If a previously visible Build page disappeared from navigation, add it to the appropriate group.

---

## Task 3: Rewrite `build/quickstart.mdx` As Internal Developer Quickstart

**Files:**
- Modify: `build/quickstart.mdx`

- [ ] **Step 1: Replace frontmatter**

Use:

```mdx
---
title: "Internal developer quickstart"
description: "Set up an internal Grounds workspace, push a plugin, and test local plugin overrides."
---
```

- [ ] **Step 2: Replace body with the internal journey**

Use this structure:

```mdx
This guide starts from a fresh internal development workspace. By the end,
you'll have the CLI authenticated, Gradle ready to resolve `grounds-push`, and
a Velocity stack pushed with local `plugin-agones` and `plugin-player` builds.

<Note>
Examples use `~/grounds` as the parent directory. Use any directory you prefer,
but keep the app repo and plugin repos under one parent so workspace scans are
predictable.
</Note>

## Prerequisites

- A Grounds Account.
- Java 21+.
- A Gradle wrapper in each plugin repository.
- Git access to the internal repositories you work on.
- A GitHub token with `read:packages` for resolving `gg.grounds.push` from GitHub Packages.
```

Then add sections with these exact headings:

```mdx
## 1. Create your workspace directory
## 2. Install and sign in with the CLI
## 3. Configure GitHub Packages for Gradle
## 4. Check the app manifest
## 5. Push with release sources
## 6. Add local workspace mappings
## 7. Push with local plugin overrides
## 8. Inspect the result
## Next steps
```

Include these commands and examples in the appropriate sections:

```bash
mkdir -p ~/grounds
cd ~/grounds
git clone git@github.com:groundsgg/plugin-agones.git
git clone git@github.com:groundsgg/plugin-player.git
grounds login
grounds doctor
```

```bash
export GITHUB_ACTOR=<github-user>
export GITHUB_TOKEN=<token-with-read-packages>
```

```yaml grounds.yaml
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

```bash
cd ~/grounds/sample-plugin-workspace/app
grounds push --target=dev
```

```bash
grounds workspace add plugin-agones ~/grounds/plugin-agones \
  --variant velocity \
  --artifact velocity/build/libs/plugin-agones-velocity.jar \
  --build './gradlew :velocity:shadowJar'

grounds workspace add plugin-player ~/grounds/plugin-player \
  --variant velocity \
  --artifact velocity/build/libs/plugin-player-velocity.jar \
  --build './gradlew :velocity:shadowJar'

grounds workspace list
grounds workspace doctor
grounds push --target=dev --with-local
```

Add a final `CardGroup` linking to:

- `/build/local-plugin-workspaces`
- `/build/multi-plugin-stacks`
- `/build/debug-pushes`
- `/build/manifest`

- [ ] **Step 3: Preserve public first-push information**

Confirm the rewritten quickstart still contains:

- CLI install commands for Homebrew, curl, and Scoop.
- `grounds login`.
- `grounds doctor`.
- first `grounds push --target=dev`.
- staging path linked through `build/share-staging-previews.mdx`.

---

## Task 4: Create `build/local-plugin-workspaces.mdx`

**Files:**
- Create: `build/local-plugin-workspaces.mdx`
- Modify: `build/cli/workspace.mdx`

- [ ] **Step 1: Create journey page**

Create `build/local-plugin-workspaces.mdx` with sections:

```mdx
---
title: "Work with local plugin workspaces"
description: "Override release plugin sources with local builds from independent repositories."
---

## Repository layout
## Scan for repositories
## Pin exact Velocity artifacts
## Inspect mappings
## Push one local override
## Push every enabled local override
## Disable a mapping temporarily
## Config location
## Reference
```

Use the canonical sample:

```text
~/grounds/
  sample-plugin-workspace/
    app/
      grounds.yaml
  plugin-agones/
  plugin-player/
```

Include these commands:

```bash
grounds workspace scan ~/grounds
grounds workspace scan ~/grounds --yes
grounds workspace add plugin-agones ~/grounds/plugin-agones \
  --variant velocity \
  --artifact velocity/build/libs/plugin-agones-velocity.jar \
  --build './gradlew :velocity:shadowJar'
grounds workspace add plugin-player ~/grounds/plugin-player \
  --variant velocity \
  --artifact velocity/build/libs/plugin-player-velocity.jar \
  --build './gradlew :velocity:shadowJar'
grounds workspace list
grounds workspace doctor
grounds push --target=dev --local plugin-agones
grounds push --target=dev --local plugin-agones,plugin-player
grounds push --target=dev --local plugin-agones --local plugin-player
grounds push --target=dev --with-local
grounds workspace enable plugin-player velocity --disable
grounds workspace enable plugin-player velocity
```

Add reference links to:

- `/build/cli/workspace`
- `/build/cli/push`
- `/build/cli/configuration`
- `/build/multi-plugin-stacks`

- [ ] **Step 2: Link reference page back to journey**

In `build/cli/workspace.mdx`, after the opening paragraph, add:

```mdx
For the internal step-by-step workflow, start with
[Work with local plugin workspaces](/build/local-plugin-workspaces).
```

---

## Task 5: Create `build/multi-plugin-stacks.mdx`

**Files:**
- Create: `build/multi-plugin-stacks.mdx`
- Modify: `build/manifest.mdx`
- Modify: `build/gradle-plugin/groundsPush.mdx`

- [ ] **Step 1: Create workflow page**

Create `build/multi-plugin-stacks.mdx` with sections:

```mdx
---
title: "Test multi-plugin stacks"
description: "Bundle multiple Paper, Velocity, or gamemode plugins into one Grounds push."
---

## When to use `plugins:`
## Structured entries
## Release defaults, local overrides
## Supported source forms
## Upload shape
## Limits
## Next steps
```

Include the canonical `sample-velocity-plugins` manifest and explain:

- `id` is the stable key for workspace overrides and source reporting.
- `variant` identifies `paper`, `velocity`, or `minestom` variants.
- `source` is the portable default for plain `grounds push`.
- `grounds push --with-local` replaces enabled workspace entries for the push.
- `plugins:` and `jar:` are mutually exclusive.
- `plugins:` supports 2 to 10 entries.
- total upload size is capped at 100 MB.

- [ ] **Step 2: Add cross-link from manifest**

In `build/manifest.mdx`, after the first paragraph under `### plugins`, add:

```mdx
For the internal workflow that uses these fields with local overrides, see
[Test multi-plugin stacks](/build/multi-plugin-stacks).
```

- [ ] **Step 3: Add cross-link from Gradle task reference**

In `build/gradle-plugin/groundsPush.mdx`, at the end of the `Multi-plugin bundles` section, add:

```mdx
For the developer workflow around this feature, see
[Test multi-plugin stacks](/build/multi-plugin-stacks).
```

---

## Task 6: Create `build/share-staging-previews.mdx`

**Files:**
- Create: `build/share-staging-previews.mdx`
- Modify: `build/cli/preview.mdx`
- Modify: `build/concepts/preview-environments.mdx`

- [ ] **Step 1: Create workflow page**

Create `build/share-staging-previews.mdx` with sections:

```mdx
---
title: "Share staging previews"
description: "Push a build to staging, share it with teammates, and manage preview lifetime."
---

## Push to staging
## When to use staging
## List previews
## Pin a preview
## Inspect in Portal
## Reference
```

Include these commands:

```bash
grounds push --target=staging
grounds preview list
grounds preview pin <preview-id>
grounds preview unpin <preview-id>
```

Link to:

- `/build/cli/preview`
- `/build/concepts/preview-environments`
- `/build/cli/push`

- [ ] **Step 2: Link CLI preview reference back to workflow**

Near the top of `build/cli/preview.mdx`, add:

```mdx
For the developer workflow, see [Share staging previews](/build/share-staging-previews).
```

- [ ] **Step 3: Link concept page back to workflow**

Near the top of `build/concepts/preview-environments.mdx`, add:

```mdx
For the step-by-step staging workflow, see
[Share staging previews](/build/share-staging-previews).
```

---

## Task 7: Create `build/debug-pushes.mdx`

**Files:**
- Create: `build/debug-pushes.mdx`
- Modify: `build/troubleshooting.mdx`
- Modify: `build/cli/push.mdx`

- [ ] **Step 1: Create workflow page**

Create `build/debug-pushes.mdx` with sections:

```mdx
---
title: "Debug pushes and deployments"
description: "Diagnose failed local builds, uploads, Forge builds, and deployments."
---

## 1. Read the CLI output
## 2. Check local workspace mappings
## 3. Check the source table
## 4. Inspect Gradle directly
## 5. Inspect Portal
## 6. Tail deployment logs
## 7. Retry transient failures
## Reference
```

Include the diagnostic table:

```mdx
| Layer | Typical signal |
| --- | --- |
| Workspace resolution | `local workspace entry ... not found` |
| Local build | Gradle failure from the dependency repo build command |
| Upload | HTTP, auth, file size, or network error |
| Forge build | `build_failed` |
| Deploy | `deploy_failed`, pod scheduling, readiness, or crash loop |
```

Include these commands:

```bash
grounds workspace list
grounds workspace doctor
cd ~/grounds/plugin-agones
./gradlew :velocity:shadowJar
./gradlew groundsPush --target=dev --stacktrace
grounds logs deployment <app-name>
grounds push retry <push-id>
```

- [ ] **Step 2: Link troubleshooting page back to debug workflow**

Near the top of `build/troubleshooting.mdx`, add:

```mdx
For a layer-by-layer debugging workflow, start with
[Debug pushes and deployments](/build/debug-pushes).
```

- [ ] **Step 3: Link push reference back to debug workflow**

In `build/cli/push.mdx`, near the `Exit codes` section, add:

```mdx
For a practical failure investigation flow, see
[Debug pushes and deployments](/build/debug-pushes).
```

---

## Task 8: Update Build Overview And Local Development Cross-links

**Files:**
- Modify: `build/index.mdx`
- Modify: `build/local-development.mdx`

- [ ] **Step 1: Add journey entry points to Build overview**

In `build/index.mdx`, after the `Who this is for` section and before `Get started in five minutes`, add:

```mdx
## Internal developer path

If you work on Grounds plugins or platform-adjacent repos, start here:

<CardGroup cols={2}>
<Card title="Internal quickstart" icon="rocket" href="/build/quickstart">
  Set up a local workspace, authenticate the CLI, and push with local plugin overrides.
</Card>

<Card title="Local plugin workspaces" icon="folder-tree" href="/build/local-plugin-workspaces">
  Map independent plugin repos into `grounds push --local` and `--with-local`.
</Card>

<Card title="Multi-plugin stacks" icon="layer-group" href="/build/multi-plugin-stacks">
  Test Paper, Velocity, or gamemode stacks with multiple plugins in one push.
</Card>

<Card title="Debug pushes" icon="bug" href="/build/debug-pushes">
  Work through local build, upload, Forge build, and deployment failures.
</Card>
</CardGroup>
```

- [ ] **Step 2: Keep existing overview content**

Confirm `build/index.mdx` still contains:

- `What you get`
- `Who this is for`
- install/login/push quick start steps or equivalent link
- `What's under the hood`

- [ ] **Step 3: Link local development to workspace workflow**

In `build/local-development.mdx`, after `When local is not enough`, add:

```mdx
<Tip>
When you need multiple independent plugin repositories on the same server, use
[Local plugin workspaces](/build/local-plugin-workspaces) and
[Multi-plugin stacks](/build/multi-plugin-stacks). `groundsTestLocal` is best
for one plugin in one local Paper or Velocity server.
</Tip>
```

---

## Task 9: Review Reference Pages For Cross-links And Stale Claims

**Files:**
- Review/modify: `build/cli/configuration.mdx`
- Review/modify: `build/cli/push.mdx`
- Review/modify: `build/cli/workspace.mdx`
- Review/modify: `build/gradle-plugin/setup.mdx`
- Review/modify: `build/gradle-plugin/groundsPush.mdx`
- Review/modify: `build/manifest.mdx`

- [ ] **Step 1: Search for stale upload limits**

Run:

```bash
rg -n "50 MB|50MB|100 MB|100MB" build
```

Expected:

- No `50 MB` or `50MB` references for current push upload limits.
- `100 MB` references are allowed in `build/manifest.mdx`, `build/gradle-plugin/groundsPush.mdx`, and workflow pages.

- [ ] **Step 2: Search for stale plugin versions**

Run:

```bash
rg -n 'gg\.grounds\.push"\) version "0\.5\.0|gg\.grounds\.push"\) version "0\.1\.0|version "0\.x"' build
```

Expected: no matches.

- [ ] **Step 3: Search for broken old category links**

Run:

```bash
rg -n 'href="/build/cli"|href="/build/concepts"|href="/build/gradle-plugin"' build
```

For any card/link pointing to a group path that does not exist as a page, replace it with the most specific existing page:

- `/build/cli` -> `/build/cli/push` or `/build/cli/installation`
- `/build/concepts` -> `/build/concepts/projects-and-teams`
- `/build/gradle-plugin` -> `/build/gradle-plugin/setup`

---

## Task 10: Validate Mintlify And Navigation

**Files:**
- Validate: all docs

- [ ] **Step 1: Run Mintlify validation**

Run:

```bash
cd /home/lukas/grounds/docs
mintlify validate
```

Expected:

```text
success build validation passed
```

- [ ] **Step 2: Run whitespace/conflict validation**

Run:

```bash
git diff --check
```

Expected: no output and exit code `0`.

- [ ] **Step 3: Verify every new page is in navigation**

Run:

```bash
rg -n "build/local-plugin-workspaces|build/multi-plugin-stacks|build/share-staging-previews|build/debug-pushes|build/cli/workspace" docs.json
```

Expected: all five paths appear.

---

## Task 11: Commit The Documentation Restructure

**Files:**
- Stage all intended docs changes from this plan.

- [ ] **Step 1: Review final status**

Run:

```bash
git status --short --branch
git diff --stat
```

Expected changed paths include only docs files touched by this plan.

- [ ] **Step 2: Stage intended docs changes**

Run:

```bash
git add docs.json build docs/superpowers/plans/2026-05-14-build-docs-developer-journey.md
```

- [ ] **Step 3: Commit**

Run:

```bash
git commit -m "docs: restructure build developer journey"
```

Expected: commit succeeds.

---

## Task 12: Optional PR

Only run this task if the user asks for push/PR.

**Files:**
- No file edits.

- [ ] **Step 1: Push branch**

Run:

```bash
git push -u origin feat/document-local-plugin-overrides
```

- [ ] **Step 2: Create PR**

Run:

```bash
gh pr create \
  --base main \
  --head feat/document-local-plugin-overrides \
  --title "docs: restructure build developer journey" \
  --body "## Summary
- reorganize Build docs around the internal developer journey
- add workflow pages for local plugin workspaces, multi-plugin stacks, staging previews, and debugging pushes
- keep detailed CLI, Gradle, manifest, config, portal, and concept references available under Reference

## Validation
- mintlify validate
- git diff --check
- stale text/link searches for upload limits, plugin versions, and group links"
```

Expected: GitHub returns the PR URL.
