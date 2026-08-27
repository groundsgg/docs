# Paper Scene Editor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task by task. Use `superpowers:test-driven-development` for every behavior change and `superpowers:verification-before-completion` before each merge or release. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship the first Paper scene-editor slice so builders can author canonical, catalog-valid props and NPCs in a build world, save root `scene.json` safely, and receive a dirty-session guard before `/map push`.

**Architecture:** First publish a non-empty resourcepacks 0.6.0 catalog. Then create the standalone `plugin-scene-editor` repository with a Bukkit-free `common` module and a Paper adapter. Keep documents immutable, persist through `scene-format` only, and integrate GroundsMaps through the editor's small `SceneEditStatus` service API.

**Tech Stack:** Kotlin, Java 25, Gradle, Grounds conventions, `scene-format` 0.1.0, `scene-testkit` 0.1.0, `resourcepacks-catalog` 0.6.0, Paper 26.1.2, Adventure, MockBukkit, JUnit 5

## Global constraints

- Use isolated worktrees for `resourcepacks`, `plugin-scene-editor`, `buildsystem`, and later deployment changes. Preserve every unrelated dirty checkout.
- Do not wait for `arc-linux`, Kubernetes, service-maps Stage acceptance, or cluster recovery. Record those as external release/deployment gates.
- Never create a second scene JSON model or catalog JSON codec. Use released public APIs from `scene-format` and `GroundsAssetCatalog`.
- Write a failing focused test before each production change. Confirm that it fails for the expected reason before implementing.
- Keep all common state free of Bukkit types. Paper objects enter only through adapter interfaces.
- Do not claim a saved scene is published. `/scene save` reports local file state only.
- Do not merge a consumer pin until its exact producer artifact is published, except for locally verified branches explicitly held behind that gate.
- Use `GRADLE_USER_HOME=/tmp/<task-specific-name>` when the default Gradle cache is unavailable.
- Update the Confluence masterplan only after repository evidence exists; never mark deployment or Stage acceptance complete from local tests.

---

### Task 1: Add declarative bootstrap assets to resourcepacks

**Repository:** `/home/lukas/grounds/resourcepacks`

**Files:**
- Modify: `resourcepacks-catalog/build.gradle.kts`
- Modify: `resourcepacks-catalog/src/main/kotlin/gg/grounds/resourcepacks/catalog/GroundsAssets.kt`
- Modify: `resourcepacks-catalog/src/test/kotlin/gg/grounds/resourcepacks/catalog/CatalogApiTest.kt`
- Modify: `resourcepacks-catalog/src/test/java/gg/grounds/resourcepacks/catalog/CatalogJavaApiTest.java`
- Modify: `resourcepacks-product/src/test/kotlin/gg/grounds/resourcepacks/product/CatalogParityValidatorTest.kt`

**Contract:**
- `grounds:editor/marker` is a `PROP` with `LocalBounds(center=(0,0.5,0), size=(1,1,1))`.
- `grounds:editor/guide` is an `NPC_BODY` with `LocalBounds(center=(0,0.9,0), size=(0.6,1.8,0.6))`.
- Both have no animations and empty editor metadata.
- `GroundsAssetCatalog.catalog.assets` is immutable and deterministically ordered.

- [x] **Step 1: Create an isolated feature worktree**

```bash
git fetch origin
git worktree add ../.worktrees/resourcepacks-scene-bootstrap -b feat/scene-bootstrap-assets origin/main
```

- [x] **Step 2: Write failing catalog API tests**

Replace the empty-catalog assertions with exact key, kind, bounds, ordering, version-reference, Java-access, and mutation-rejection assertions. Extend the parity fixture to require both projected model paths.

Run:

```bash
GRADLE_USER_HOME=/tmp/scene-resourcepacks-gradle ./gradlew --no-build-cache :resourcepacks-catalog:test :resourcepacks-product:test --tests '*CatalogParityValidatorTest'
```

Expected: failure because the generated catalog is still empty.

- [x] **Step 3: Generate the catalog from one declarative source**

Add one ordered private definition map in `GroundsAssets.kt` and retain the public Java-facing `GroundsAssets.all: Set<AssetKey>` as its immutable key set. Keep the existing public owners, types, and Java ABI unchanged. The selected Gradle build version must reach the runtime catalog for both releases and exact edge builds.

- [x] **Step 4: Run the focused tests**

```bash
GRADLE_USER_HOME=/tmp/scene-resourcepacks-gradle ./gradlew --no-build-cache :resourcepacks-catalog:check :resourcepacks-product:test --tests '*CatalogParityValidatorTest'
git diff --check
```

Expected: catalog API tests and the parity fixture pass against the two exact projected model paths. Full pack composition remains red until Task 2 supplies the source models.

- [x] **Step 5: Commit the catalog declaration**

```bash
git add resourcepacks-catalog
git commit -m "feat(catalog): declare scene editor bootstrap assets"
```

---

### Task 2: Package matching bootstrap models

**Repository:** resourcepacks feature worktree from Task 1

**Files:**
- Create: `art/content/models/editor/marker.json`
- Create: `art/content/models/npc_bodies/editor/guide.json`
- Modify: `art/content/LICENSE`
- Modify: `resourcepacks-product/src/main/kotlin/gg/grounds/resourcepacks/product/ContentContribution.kt`
- Modify: `resourcepacks-product/src/main/kotlin/gg/grounds/resourcepacks/product/ProductGraph.kt`
- Modify: `resourcepacks-product/src/test/kotlin/gg/grounds/resourcepacks/product/PackComposerTest.kt`
- Modify: `resourcepacks-catalog/src/test/kotlin/gg/grounds/resourcepacks/catalog/PackArtworkLicenseContractTest.kt`

- [x] **Step 1: Write failing composition and license tests**

Require these exact content-pack entries:

```text
assets/grounds/models/editor/marker.json
assets/grounds/models/npc_bodies/editor/guide.json
```

Require both source JSON files to be covered by the repository's artwork/content provenance contract.

- [x] **Step 2: Add deliberately authored minimal models**

Add deterministic JSON models using Minecraft parent/texture references and no copied binary artwork. Record them as Grounds-authored source in `art/content/LICENSE` while preserving the existing vendor/MCModels attribution text and tests. The marker is a one-block editor marker; the guide is a simple bootstrap NPC-body model. Do not add image files or vendor assets.

- [x] **Step 3: Capture and compose the files safely**

Extend `ProductGraph` to capture both sources with `HeldSourceFile`, bounded size, and pinned bytes. Feed byte-backed entries through `ContentContribution`; do not pass mutable paths into pack composition. Preserve no-follow behavior and exact release-input capture.

- [x] **Step 4: Verify model/catalog parity and the built pack**

```bash
GRADLE_USER_HOME=/tmp/scene-resourcepacks-gradle ./gradlew --no-build-cache :resourcepacks-catalog:check :resourcepacks-product:test
GRADLE_USER_HOME=/tmp/scene-resourcepacks-gradle ./gradlew --no-build-cache clean check -PpackSetVersion="$(tr -d '\n' < version.txt)"
git diff --check
```

Build the current branch's release-shaped PackSet into a fresh absent output path and inspect the ZIP. The feature branch must use the exact value still present in `version.txt`; Release Please changes it to 0.6.0 later:

```bash
scene_pack_version="$(tr -d '\n' < version.txt)"
scene_pack_commit="$(git rev-parse HEAD)"
GRADLE_USER_HOME=/tmp/scene-resourcepacks-gradle ./gradlew :resourcepacks-product:buildPackSet -PpackSetVersion="$scene_pack_version" -PprovenanceCommit="$scene_pack_commit" -PpublicationType=release -PpublicationId="v$scene_pack_version" -PreleaseOutput=/tmp/scene-packset-bootstrap
unzip -l "/tmp/scene-packset-bootstrap/grounds-content-pack-v${scene_pack_version}.zip"
```

Expected: both exact model paths exist and parity validation passes.

- [x] **Step 5: Commit the pack content**

```bash
git add art/content resourcepacks-product resourcepacks-catalog/src/test
git commit -m "feat(pack): add scene editor bootstrap models"
```

---

### Task 3: Merge and release resourcepacks 0.6.0

**Repository:** resourcepacks feature worktree

- [x] **Step 1: Run full verification and request review**

```bash
GRADLE_USER_HOME=/tmp/scene-resourcepacks-gradle ./gradlew --no-build-cache clean check -PpackSetVersion="$(tr -d '\n' < version.txt)"
git diff --check origin/main...HEAD
git status --short
```

Use `superpowers:requesting-code-review`; resolve every blocking finding and rerun the commands.

- [x] **Step 2: Push, create the PR, and merge after green checks**

Do not wait on `arc-linux` indefinitely. If required hosted checks cannot start, keep the verified branch and record the release gate rather than weakening branch protection.

Delivered by resourcepacks PR #26. CI run `33076082670` passed and the PR was squash-merged as `bc69e647c6111edfa8e6efaf9c2f79715f6d51d6`.

- [ ] **Step 3: Complete Release Please for 0.6.0**

Let Release Please update `version.txt`, changelog, and release metadata. On that release branch, rerun `clean check` with the exact new `version.txt` value. Merge its release PR, verify tag `v0.6.0`, Maven package `gg.grounds:resourcepacks-catalog:0.6.0`, and immutable PackSet artifacts. Do not activate a cluster channel while the cluster is down.

Release PR #24 and its clean verification run `33077019256` were merged as `a1ccb3d4981c268253744f88142d2c849082006c`; tag `v0.6.0` exists. Immutable release run `33077618746` built and locally verified all four artifacts, then failed at the catalog Maven upload with GitHub Packages `402 Payment Required`. Keep this step open until Maven, CDN/release assets, and Stable advancement are complete.

- [ ] **Step 4: Record exact producer evidence**

Capture merge SHA, tag URL, Maven coordinate, PackSet checksums, and any queued workflow IDs. These values gate Task 4 production dependency resolution.

---

### Task 4: Create and scaffold `plugin-scene-editor`

**Repository:** new `/home/lukas/grounds/plugin-scene-editor`

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`
- Create: `common/build.gradle.kts`, `paper/build.gradle.kts`
- Create: `paper/src/main/resources/plugin.yml`
- Create: `README.md`, `CHANGELOG.md`, `LICENSE`
- Create: `release-please-config.json`, `.release-please-manifest.json`
- Create: `.github/dependabot.yml`
- Create: `.github/workflows/ci.yml`, `.github/workflows/release-please.yml`, `.github/workflows/release.yml`
- Create: Gradle wrapper files
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/SceneEditStatus.kt`
- Create: packaging and Java-consumer tests under `common/src/test` and `paper/src/test`

- [ ] **Step 1: Create the repository locally and on GitHub**

Create the GitHub repository with the same visibility and merge settings as sibling Grounds plugin repositories. Initialize `main`, then create an isolated `feat/scene-editor-mvp` worktree. Do not reuse the dirty docs or plugin template checkouts.

- [x] **Step 2: Copy only conventions, not product code**

Use `plugin-grounds-platform` and `plugin-permissions` as structure exemplars. Configure exact released inputs:

```text
root project: plugin-scene-editor
modules: common, paper
base conventions: 0.8.0
Kotlin/JDK: Kotlin 2.2.20 conventions and Java 25
Paper: paper-conventions / Paper 26.1.2
scene-format: 0.1.0
scene-testkit: 0.1.0 (test only)
resourcepacks-catalog: 0.6.0
paper compile-only API: de.eintosti:buildsystem-api:4.0.0
```

Set plugin name `GroundsSceneEditor`, main class `gg.grounds.scene.editor.paper.GroundsSceneEditorPlugin`, API version matching the current Paper convention, hard `depend: [BuildSystem]`, root `/scene`, and the permissions from the approved design. Expand `${VERSION}` explicitly with `ProcessResources`; `paper-conventions` does not do this itself.

- [x] **Step 3: Add the public service boundary first**

Create the Java-friendly contract:

```kotlin
fun interface SceneEditStatus {
    fun hasUnsavedChanges(worldId: UUID): Boolean
}
```

Add a Java compilation test proving callers see `boolean hasUnsavedChanges(UUID)` without Kotlin-specific types.

- [x] **Step 4: Add packaging tests**

Prove the deployable Paper shadow JAR contains `SceneEditStatus`, `scene-format`, and the pinned catalog exactly once, leaves `SceneEditStatus` unrelocated, expands `${VERSION}`, and excludes test libraries. Configure Paper's Maven publication to replace the disabled/empty standard JAR with `shadowJar`, following `plugin-grounds-runtime/paper/build.gradle.kts`; publish both `common` and the runnable Paper artifact. Use the default Grounds artifact naming unless deployment tooling proves a fixed versioned filename is required.

- [x] **Step 5: Verify and commit the scaffold**

```bash
GRADLE_USER_HOME=/tmp/scene-editor-gradle ./gradlew --no-build-cache clean check
git diff --check
git add .
git commit -m "build: scaffold Paper scene editor"
```

Local commit `30ee898` passed a serial clean build and both Maven publications against an isolated Maven repository populated from the exact `v0.6.0` tag. The public GitHub repository remains intentionally uncreated until repository visibility is explicitly approved; the remote package outage is not represented as a successful release.

---

### Task 5: Implement catalog binding, scene creation, and typed mutations

**Repository:** plugin-scene-editor feature worktree

**Files:**
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/catalog/SceneCatalogBinding.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/catalog/CatalogStatus.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/mutation/SceneMutation.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/mutation/SceneMutationResult.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/mutation/SceneMutations.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/validation/SceneValidationState.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/validation/SaveEligibility.kt`
- Create matching tests under `common/src/test/kotlin/...`

- [x] **Step 1: Write failing catalog and creation-default tests**

Cover exact catalog references, wrong asset kind, missing NPC bounds, duplicate IDs, player-position/yaw placement, zero pitch/roll, unit scale, null group/animation/label, `(0,2.25,0)` label offset, fixed look, null proximity, empty bindings, visible state, and automatic activation.

- [x] **Step 2: Implement catalog binding and scene creation**

Production binding reads `GroundsAssetCatalog.catalog` and constructs immutable `grounds:actions@1` with no actions. Tests inject catalogs. New documents pin these exact references.

- [x] **Step 3: Write failing mutation tests**

Cover create, select-independent replace, position set/here/add, rotation set/add with canonical angles, uniform positive scale, clone, label set, and remove for props/NPCs. Rejections must preserve the exact original document.

Also cover a decoded document containing `ApplicationAction`: unrelated prop/NPC mutations preserve it byte-semantically, the empty action catalog makes it read-only and catalog-unverified, and no common mutation can create, replace, or silently remove it without an exact catalog definition.

- [x] **Step 4: Implement typed immutable mutations**

Use scene DTO constructors/copies only. Return structured rejection values instead of throwing user-input failures. Run intrinsic validation after every candidate mutation and catalog validation for save eligibility.

- [x] **Step 5: Verify and commit**

```bash
GRADLE_USER_HOME=/tmp/scene-editor-gradle ./gradlew --no-build-cache :common:test
git diff --check
git add common
git commit -m "feat(common): add scene mutations and validation"
```

Delivered locally as `d9c3770`. TDD and review covered exact catalog pins/defaults, every first-slice prop/NPC mutation family, canonical transforms, exact-original rejection identity, unsupported composite rejection, non-finite input, pure intrinsic/catalog status separation, SaveEligibility, and read-only preservation of unverified application actions. Final forced `:common:spotlessCheck :common:test` rerun passed against the exact locally staged v0.6.0 catalog.

---

### Task 6: Implement shared sessions, history, selections, and leases

**Files:**
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/session/EditorSession.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/session/EditorSessionService.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/session/SessionState.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/history/SceneHistory.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/lease/ElementLease.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/lease/ElementLeaseRegistry.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/session/EditorSelection.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/SceneEditorEvents.kt`
- Create matching tests

- [x] **Step 1: Write failing state-machine tests**

Cover one session per world, shared document/per-player selection, serialized mutation order, one event per success, no event/history change on rejection, canonical-byte dirty comparison, and `SceneEditStatus` across absent/clean/dirty sessions.

- [x] **Step 2: Implement bounded history**

Store complete immutable snapshots, cap at 100, clear redo after a new edit, keep history after save, and make multi-step undo/redo atomic session operations.

- [x] **Step 3: Implement leases with an injected clock**

Cover acquire, refusal with owner, renewal, 120-second expiry, explicit override, disconnect/world-change/deselect/delete release, and document-level operations without global element leases.

- [x] **Step 4: Verify and commit**

```bash
GRADLE_USER_HOME=/tmp/scene-editor-gradle ./gradlew --no-build-cache :common:test
git diff --check
git add common
git commit -m "feat(common): add editor sessions and leases"
```

Delivered locally as `a40f2ff`. TDD and two review rounds covered one shared session per
world, independent player selections, serialized mutations, FIFO post-commit events with
listener-failure isolation, canonical-byte dirty state, generation-bound save snapshots,
100-entry immutable history, atomic multi-step undo/redo, 120-second element leases,
renewal/expiry/override/release behavior, and controlled no-session/input edge cases. The final
forced `:common:spotlessCheck :common:test --rerun-tasks` run passed with all 11 Gradle tasks
executed, and the final read-only review reported no P1/P2 findings.

---

### Task 7: Implement atomic world-root persistence and recovery

**Files:**
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/repository/SceneRepository.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/repository/WorldSceneRepository.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/repository/AtomicSceneFileStore.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/repository/SceneFingerprint.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/repository/SceneLoadResult.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/repository/SceneSaveResult.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/recovery/RecoveryService.kt`
- Create matching tests

- [x] **Step 1: Write failing load and path-security tests**

Test absent, valid, and invalid root `scene.json`; exact-byte fingerprinting; normalized world root; symlink escape rejection; size bounds; and preservation of invalid bytes.

- [x] **Step 2: Write failing atomic-save tests**

Inject file operations and prove sibling temp creation, flush, `ATOMIC_MOVE`, per-world serialization, concurrent fingerprint rejection, stale async generation rejection, and retention of the old file when atomic replacement is unsupported.

- [x] **Step 3: Implement canonical persistence only**

Accept/return scene-format results. Never expose a second serializer. Update the session base fingerprint and dirty comparison point only after successful replacement.

- [x] **Step 4: Implement explicit recovery**

Cover byte-identical timestamped sibling backup-and-create, generated diagnostic exports below plugin data, clean reload, dirty reload refusal, and literal-confirm discard-and-reload with audit details.

- [x] **Step 5: Verify and commit**

```bash
GRADLE_USER_HOME=/tmp/scene-editor-gradle ./gradlew --no-build-cache :common:test
git diff --check
git add common
git commit -m "feat(common): persist scenes atomically"
```

Delivered locally as `7ce3a1e`. TDD and three independent review rounds covered bounded
no-follow reads, exact-byte SHA-256 fingerprints with distinct absence, canonical `SceneJson`
encoding, eligibility-bound save reservations, coordinated conditional `ATOMIC_MOVE`, concurrent
disk conflicts, atomic-only failure retention, defensive byte boundaries, invalid-file backup and
create, diagnostic exports, clean/dirty reload rules, literal-confirm discard audit, JVM capability
forgery, fatal-error reservation cleanup, and best-effort temp cleanup. The final forced
`:common:spotlessCheck :common:test --rerun-tasks` run passed with all 11 Gradle tasks executed,
and the final read-only review reported no P1/P2 findings under the documented cooperative-writer
coordination contract.

---

### Task 8: Compose the Paper plugin and command surface

**Files:**
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/GroundsSceneEditorPlugin.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/PaperSceneEditorRuntime.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/PaperSessionResolver.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/command/SceneCommand.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/command/SceneTabCompleter.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/command/SceneCommandAuthorizer.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/AdventureSceneFeedback.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/PaperScheduler.kt`
- Create matching MockBukkit tests

- [x] **Step 1: Write failing lifecycle/service tests**

Prove enable composition, catalog initialization failure, command registration, `SceneEditStatus` registration, reverse-order close/unregister, and no mutable global singleton.

- [x] **Step 2: Implement lifecycle composition**

Keep `JavaPlugin` limited to dependency construction and registration. Run Bukkit work on the server thread and file work asynchronously with session-generation checks.

- [x] **Step 3: Write failing command/permission/completion tests**

Cover every first-slice path from the approved design, exact permissions, console-only catalog status exception, finite-number parsing, asset-kind completion, pagination, partial-path completion, and read-only display of preserved application actions.

`PaperSessionResolver` must query the published BuildSystem API for the player's current `BuildWorld`; ordinary loaded Bukkit worlds are rejected. MockBukkit tests cover a missing BuildWorld, a valid build world, world change, and BuildSystem service loss. The hard plugin dependency makes a completely absent BuildSystem an enable-time failure rather than an ambiguous editor mode.

- [x] **Step 4: Implement commands as adapters over common operations**

Commands must not reconstruct DTOs independently. Use Adventure components and deterministic problem ordering. `/scene save` says saved, never published.

- [x] **Step 5: Verify and commit**

```bash
GRADLE_USER_HOME=/tmp/scene-editor-gradle ./gradlew --no-build-cache :paper:test
git diff --check
git add paper
git commit -m "feat(paper): expose scene editor commands"
```

Delivered locally as `2cf36d6`, with supporting Common hardening in `e66a808`, `c80ebae`,
`880193a`, and `db4a59f`. TDD covers transactional plugin lifecycle, BuildSystem service and exact
world resolution, scheduler shutdown races, lazy valid/absent/invalid scene bootstrap, atomic
first-command recovery, generation-bound reload/discard, all first-slice prop/NPC mutations,
owner-bound leases and administrative takeover, exact leaf permissions, kind- and
session-aware completion, pagination, finite-number rejection, preserved read-only application
actions, deterministic diagnostics, and local-only save wording. The final forced
`:common:spotlessCheck :common:test :paper:spotlessCheck :paper:test --rerun-tasks` verification
passed, and three independent review rounds ended with no P1/P2 findings.

---

### Task 9: Add previews and lifecycle cleanup

**Files:**
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/preview/PaperPreviewAdapter.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/preview/PreviewRegistry.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/PaperWorldLifecycleListener.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/PaperPlayerLifecycleListener.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/tool/RaySelection.kt`
- Create matching tests

- [ ] **Step 1: Write failing pure selection and adapter tests**

Cover deterministic nearest-hit selection, generic prop/NPC placeholders, labels, per-viewer selected outline/axes/ID, non-persistent entities, plugin PDC keys, and a visible fallback when rendering fails.

- [ ] **Step 2: Implement server-thread preview reconciliation**

Reconcile from immutable document snapshots. Never mutate documents from preview state. Discard stale async results by generation.

- [ ] **Step 3: Implement cleanup listeners**

Quit/world change releases player selection and leases. World unload/plugin disable removes all preview entities, cancels tasks, releases leases, logs dirty state, and notifies online editors.

- [ ] **Step 4: Verify and commit**

```bash
GRADLE_USER_HOME=/tmp/scene-editor-gradle ./gradlew --no-build-cache :common:test :paper:test
git diff --check
git add common paper
git commit -m "feat(paper): preview scene documents safely"
```

---

### Task 10: Add the editor tool

**Files:**
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/tool/TransformComponent.kt`
- Create: `common/src/main/kotlin/gg/grounds/scene/editor/tool/TransformMath.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/tool/EditorTool.kt`
- Create: `paper/src/main/kotlin/gg/grounds/scene/editor/paper/tool/EditorToolListener.kt`
- Create matching tests

- [ ] **Step 1: Write failing transform-math tests**

Cover X/Y/Z, yaw/pitch/roll, uniform scale, signed fine/normal/coarse steps, canonical rotation, positive scale rejection, and exact mutation equivalence with commands.

- [ ] **Step 2: Write failing listener tests**

Cover exact PDC-tagged item identity, permissions/session/selection/lease gates, ray-select left click, component cycling, accepted wheel cancellation, and normal click/hotbar behavior outside active tool state.

- [ ] **Step 3: Implement the tool and action bar**

Show element ID, component, value, step, dirty state, and lease time. Cancel `PlayerItemHeldEvent` only after a valid editor mutation is accepted.

- [ ] **Step 4: Verify and commit**

```bash
GRADLE_USER_HOME=/tmp/scene-editor-gradle ./gradlew --no-build-cache :common:test :paper:test
git diff --check
git add common paper
git commit -m "feat(paper): add scene editor tool"
```

---

### Task 11: Verify, merge, and release the editor API and Paper plugin

**Repository:** plugin-scene-editor feature worktree

- [ ] **Step 1: Add the canonical end-to-end fixture**

Create a scene through common mutations using both bootstrap assets, save it through the atomic repository, decode it with released `scene-format` 0.1.0, reload it into a fresh session, and assert byte-canonical equality. Package the Paper shadow JAR and rerun its contents contract.

- [ ] **Step 2: Run clean verification and review**

```bash
GRADLE_USER_HOME=/tmp/scene-editor-release ./gradlew --no-build-cache clean check
git diff --check origin/main...HEAD
git status --short
```

Use `superpowers:requesting-code-review`; resolve all blocking findings and rerun verification.

- [ ] **Step 3: Push, create the PR, and merge after green checks**

Keep any runner outage visible as an external gate. Do not replace missing protected checks with an unverifiable manual claim.

- [ ] **Step 4: Release the first editor version**

Merge the Release Please PR and verify the release tag, the published `common` Maven coordinate, and the runnable Paper shadow artifact. Download both from the release/package service and rerun the Java API and JAR-contents smoke checks against the published files.

- [ ] **Step 5: Record exact consumer pins**

Capture editor version, merge SHA, release URL, common coordinate/checksum, Paper artifact URL/checksum, and workflow IDs. Task 12 must use these exact released values.

---

### Task 12: Guard dirty scenes before GroundsMaps push

**Repository:** `/home/lukas/grounds/buildsystem` using a new worktree based on the branch containing async map publication

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `buildsystem-grounds/build.gradle.kts`
- Modify: `buildsystem-grounds/src/main/java/gg/grounds/buildsystem/command/MapCommand.java`
- Create: `buildsystem-grounds/src/main/java/gg/grounds/buildsystem/command/SceneEditorPushGuard.java`
- Create: `buildsystem-grounds/src/test/java/gg/grounds/buildsystem/command/SceneEditorPushGuardTest.java`
- Modify: archive contract test that owns `WorldArchive`

- [ ] **Step 1: Add dependency and descriptor contract tests**

Add the Grounds GitHub Packages repository to `dependencyResolutionManagement`, using `github.user`/`github.token` Gradle properties with `GITHUB_ACTOR`/`GITHUB_TOKEN` CI fallback and never logging either value. Prove the exact released common POM/JAR resolves before changing production code.

Pin that released common API as `compileOnly` and `testImplementation`. Add `GroundsSceneEditor` to the generated GroundsMaps `softDepend`. Test that the shadow JAR does not bundle `SceneEditStatus` and that `plugin.yml` contains the soft dependency.

- [ ] **Step 2: Write failing guard tests**

Cover absent editor plugin/API, absent service, clean session, dirty session, and a provider failure. A dirty session must stop before Bukkit world save, archive creation, link creation, or registry calls. A provider failure must log and fail closed, because silently publishing potentially dirty work is unsafe.

- [ ] **Step 3: Implement a linkage-safe optional adapter**

Keep typed `SceneEditStatus` access in `SceneEditorPushGuard`, and load/invoke that adapter only after `PluginManager` confirms `GroundsSceneEditor` is enabled. The no-editor test must prove `MapCommand` loads and pushes without `NoClassDefFoundError` when the compile-only API is absent. Query on the server thread.

- [ ] **Step 4: Wire the guard at the start of push**

Call it at the beginning of the private push path, before all side effects. Send: `You have unsaved scene edits. Run /scene save before /map push.` for dirty state.

- [ ] **Step 5: Retain the archive-root contract**

Extend the existing archive test to prove `scene.json` remains a direct archive-root entry and its bytes are unchanged.

- [ ] **Step 6: Verify and commit**

```bash
GRADLE_USER_HOME=/tmp/scene-buildsystem-gradle ./gradlew --no-build-cache :buildsystem-grounds:test :buildsystem-grounds:shadowJar
unzip -p build/libs/GroundsMaps-*.jar plugin.yml | rg 'softdepend|GroundsSceneEditor'
jar tf build/libs/GroundsMaps-*.jar | rg 'SceneEditStatus' && exit 1 || true
git diff --check
git add buildsystem-grounds
git commit -m "feat(maps): block pushes with dirty scenes"
```

---

### Task 13: Run final cross-repository acceptance and prepare the buildserver

**Repositories:** plugin-scene-editor, resourcepacks, buildsystem, and the service-maps feature worktree when its derivation implementation exists

**Buildserver files after acceptance:**
- Modify: `containers/buildserver/Dockerfile`
- Modify: `containers/buildserver/README.md`
- Create: `containers/scripts/test-buildserver-scene-editor-pin.sh`
- Modify: `containers/.github/workflows/ci.yml`
- Modify, only when deployment resumes: `deploy/environments/stage/components/buildserver/values.yaml`

- [ ] **Step 1: Verify archive and published artifact contracts together**

Run the canonical editor fixture against the published 0.6.0 catalog and released editor artifacts. Archive its world with BuildSystem and assert that root `scene.json` is byte-identical and still decodes with released `scene-format` 0.1.0.

- [ ] **Step 2: Exercise downstream contracts locally**

If the independent service-maps scene-derivation branch has landed production `SceneDeriver`, feed the bytes through it using the exact 0.6.0 asset catalog and empty `grounds:actions@1`, then assert representative derived output without contacting the cluster. If that implementation has not landed, record this as a non-blocking downstream check; released scene-format decoding and BuildSystem archive-root verification remain mandatory.

- [ ] **Step 3: Run full clean builds**

```bash
GRADLE_USER_HOME=/tmp/scene-resourcepacks-final ./gradlew --no-build-cache clean check
GRADLE_USER_HOME=/tmp/scene-editor-final ./gradlew --no-build-cache clean check
GRADLE_USER_HOME=/tmp/scene-buildsystem-final ./gradlew --no-build-cache clean check
```

Run each command in its repository. Also run `git diff --check`, inspect artifact contents, and request final code review.

- [ ] **Step 4: Merge and publish the remaining consumers in dependency order**

1. Confirm the already released resourcepacks 0.6.0 and editor artifacts.
2. Merge and publish BuildSystem/GroundsMaps with the exact editor-common pin.
3. In a fresh containers worktree, pin the new BuildSystem base image by immutable digest and add exact `SCENE_EDITOR_URL`/`SCENE_EDITOR_SHA256` build arguments. Download `GroundsSceneEditor.jar` in the existing verified plugin stage and copy it to `/app/plugins`.
4. Add and wire `scripts/test-buildserver-scene-editor-pin.sh`; assert the Dockerfile/README pins agree and the built image contains exactly one editor JAR alongside BuildSystem and GroundsMaps.
5. Merge containers and publish the derived `ghcr.io/groundsgg/buildserver` image by immutable digest.
6. Only after cluster work resumes, update `deploy/environments/stage/components/buildserver/values.yaml` to that exact derived image tag/digest and retain its parser/Helm tests.

If pipelines remain unavailable, merge only changes whose protected checks can complete, retain verified local branches for downstream pins, and do not fabricate artifact coordinates.

- [ ] **Step 5: Update the masterplan with evidence**

Mark Phase 6 items complete only for merged code, published artifacts, and verified local cross-repo contracts. Leave cluster deployment and Stage acceptance explicitly blocked by the external cluster/runner outage. Include exact PRs, SHAs, tags, artifact digests, and queued workflow IDs.
