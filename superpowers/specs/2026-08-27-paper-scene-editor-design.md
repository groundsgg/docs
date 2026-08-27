---
title: "Paper scene editor design"
description: "Architecture and behavioral contract for authoring validated scene.json files on Grounds build servers."
---

# Paper scene editor design

## Status

Approved for implementation planning on 2026-08-27.

This specification defines Phase 6 of the Scene, NPC, and Resource Pack platform. It introduces a Paper-only `plugin-scene-editor` product that lets builders create, preview, validate, and save the optional `scene.json` belonging to a build world. Cluster deployment and Phase 5 Stage acceptance are independent and may remain unavailable while this plugin is developed and tested locally.

## Outcome

A permitted builder can enter a loaded build world, create or open its scene, add props and NPCs, select and transform them, inspect validation problems, undo or redo changes, and atomically save canonical `scene.json` at the world root. The existing `/map push` flow later archives that file and submits it to service-maps for authoritative derivation.

The first vertical slice ends when a scene containing props and NPCs can be created, transformed, saved, decoded again, and proven byte-canonical and catalog-compatible. Trigger-chain authoring and composite-rig authoring build on the same architecture in later slices.

## Repository and modules

Create a dedicated `groundsgg/plugin-scene-editor` repository with two modules:

| Module | Responsibility |
| --- | --- |
| `common` | Immutable editor sessions, mutations, bounded history, leases, repository abstraction, catalog binding, validation, and save eligibility. It has no Bukkit dependency. |
| `paper` | JavaPlugin lifecycle, world/session discovery, commands, permissions, tab completion, Paper previews, editor-tool input, Adventure feedback, and main-thread scheduling. |

The repository uses `gg.grounds.base-conventions` at the root and `gg.grounds.paper-conventions` for the Paper artifact. It targets JDK 25, Kotlin, Paper 26.1.2, `library-scene` 0.1.0, and the exact catalog artifact prepared below. The initial product is Paper-only; no Velocity or Minestom adapter is created.

The Paper module produces the deployable shadow JAR. It uses `plugin.yml`, `${VERSION}` resource expansion, and the existing Grounds GitHub Packages/release conventions.

## Dependencies and ownership

`library-scene:scene-format` is the only scene schema, codec, and validation authority. The editor never creates a second JSON model and never parses or writes scene JSON through another serializer.

The editor consumes these public operations:

- `SceneJson.decode(ByteArray)`
- `SceneJson.encode(SceneDocument)`
- `SceneValidation.validateIntrinsic(SceneDocument)`
- `SceneValidation.validateCatalogs(SceneDocument, AssetCatalog, ActionCatalog)`

The plugin treats all scene-format DTOs as immutable. Each edit replaces the affected element and constructs a new `SceneDocument`. It does not mutate collection instances retained by existing documents.

The editor owns authoring state and Paper previews. BuildSystem owns build worlds and `/map push`. The Paper module consumes published `de.eintosti:buildsystem-api:4.0.0`, declares a hard `BuildSystem` plugin dependency, and opens sessions only for worlds resolved as `BuildWorld`. service-maps owns authoritative derivation, publication, pins, and rollback. The editor never calls R2, Kubernetes, or service-maps directly in the first release.

## Scene file contract

The file path for a loaded world is exactly:

```text
<World#getWorldFolder()>/scene.json
```

The editor never stores the file below `grounds/` or a plugin-data directory. The existing `WorldArchive` packs the world folder and therefore preserves `scene.json` at archive root, which is the only location the derive worker recognizes.

The repository applies these load rules:

1. If `scene.json` is absent, the session starts without a document. The builder must explicitly create a scene before element commands become available.
2. If decoding succeeds, the canonical document becomes the session base snapshot.
3. If decoding fails, the original bytes remain untouched. The session exposes ordered `SceneProblem` diagnostics and enters read-only recovery state.
4. Recovery requires an explicit command that moves the invalid file to a timestamped sibling backup before creating a new document. The plugin never silently overwrites invalid authored content.

Saving uses `SceneJson.encode`. A successful save writes the returned canonical bytes to a sibling temporary file, flushes the file, and replaces `scene.json` atomically where the filesystem supports atomic moves. If an atomic move is unsupported, the operation fails and retains the previous file; it does not fall back to a non-atomic overwrite.

The file repository serializes saves per world. It refuses a save if the on-disk file changed since the session loaded or last saved it. The comparison uses the SHA-256 of the exact loaded/saved bytes, with absence represented separately from an empty file.

The editor repository is the sole supported writer while an editor save or recovery operation is
running. Repository instances in the same server process coordinate through one normalized-path
commit lock and recheck the exact fingerprint inside the conditional atomic-move boundary. Java
NIO does not provide a portable compare-and-swap primitive for file contents: a non-cooperating
external process can still race in the final interval between that recheck and `ATOMIC_MOVE`.
Manual or third-party writes during an active editor operation are therefore outside the supported
coordination contract; changes present before the conditional commit are rejected and preserved.

## Catalog model

The first release uses versioned catalog snapshots from its own classpath. It does not invent a catalog-file codec or add a catalog-discovery API.

The currently released `gg.grounds:resourcepacks-catalog:0.5.1` contains no assets and cannot satisfy the vertical slice. Phase 6 therefore begins with a resourcepacks prerequisite: make catalog entries declarative, add matching pack models for one bootstrap `PROP` (`grounds:editor/marker`) and one bootstrap `NPC_BODY` (`grounds:editor/guide`), and release them together as resourcepacks 0.6.0. Both entries have explicit default bounds, and product validation proves that each catalog key has the model path required by its asset kind.

The deployable plugin then pins `gg.grounds:resourcepacks-catalog:0.6.0` and obtains the asset snapshot from `GroundsAssetCatalog.catalog`. Its exact catalog reference becomes the asset-catalog reference of every new `SceneDocument`. Updating the asset catalog requires updating this dependency and releasing a new plugin build, so the editor and resource-pack declarations cannot drift at runtime. Unit tests may inject richer in-memory catalogs, but production commands never expose test-only assets.

Until an action-catalog artifact or discovery contract exists, the plugin constructs one local immutable `ActionCatalog(CatalogId("grounds:actions"), "1", emptyMap())`. Its reference becomes the action-catalog reference of every new document. The plugin exposes catalog status but has no runtime catalog reload command in this release.

The current Grounds action catalog is `grounds:actions@1` and contains no application actions. The editor therefore:

- preserves decodable application actions already present in a document;
- displays them read-only;
- rejects creation or mutation of an application action unless the active action catalog defines its ID and argument contract;
- never silently removes an unknown action.

Catalog compatibility controls save eligibility. A scene remains editable when its pins do not match the classpath catalogs, but the editor labels it `catalog-unverified` and refuses normal save. A recovery export may write canonical intrinsic-valid bytes below the plugin's diagnostic-export directory, never to the live world `scene.json`. If the pinned asset catalog cannot initialize, plugin enable fails instead of starting without its required validation authority.

The first release has no catalog-pin migration command. A mismatched document is inspectable, editable in memory, and exportable, but remains unsavable until an editor build carrying its exact catalog is installed. Explicit catalog migration is deferred because it needs asset-by-asset compatibility rules, not a blind reference rewrite.

## Session model

There is at most one `EditorSession` per loaded Bukkit world. A session contains:

- world identity and exact world-root path;
- base document and current document;
- base file fingerprint;
- selected element per participating player;
- active transform component per participating player;
- bounded undo and redo history;
- element edit leases;
- validation state;
- dirty state and last successful save time.

Selections are per player. Documents, history, validation, and leases are shared per world.

The session service creates sessions lazily when a permitted player runs a scene command in a loaded build world. It closes a session when the world unloads or the plugin disables. Closing removes every preview entity, cancels tasks, releases leases, and discards unsaved memory only after logging and notifying online editors that unsaved changes existed.

## Mutations and history

Every authoring operation is a typed `SceneMutation` with:

- a stable mutation name for audit and feedback;
- the acting player UUID;
- the target element ID when applicable;
- an `apply(SceneDocument)` operation returning either a replacement document or structured rejection;
- no Bukkit or filesystem dependency.

The session performs a mutation in this order:

1. Verify permission, session state, selection, and lease.
2. Apply the mutation to the current immutable document.
3. Run intrinsic validation.
4. Record the previous document in history only when the mutation succeeds.
5. Clear redo history.
6. Recompute catalog validation and save eligibility.
7. Publish one document-changed event for the Paper preview adapter.

History stores complete immutable `SceneDocument` snapshots. It is capped at 100 entries per world. Undo and redo are themselves serialized session operations and do not create additional history entries. A save does not clear history; it updates the base fingerprint and dirty comparison point.

## Edit leases

A lease protects one scene element from concurrent edits. It contains the element ID, owning player UUID, acquisition time, and last activity time.

- Selecting an unleased element acquires its lease.
- Selecting an element already leased by another player shows the owner and refuses mutation.
- Every successful mutation renews the lease.
- A lease expires after 120 seconds without activity.
- Deselect, world change, disconnect, plugin disable, or element deletion releases the lease.
- Administrative permission `grounds.scene.lease.override` allows an explicit lease takeover command; normal selection never steals a lease.

Document-level operations such as save, undo, redo, scene creation, and recovery are serialized by the session and do not require leasing every element.

## Commands

The root command is `/scene`. Commands use explicit hierarchical paths, declare matching Bukkit permissions, and provide context-aware tab completion. Partial paths list valid next subcommands rather than returning an empty response.

The first vertical slice provides:

```text
/scene create <scene-id> <display-name>
/scene info
/scene validate
/scene save
/scene reload
/scene history
/scene undo [steps]
/scene redo [steps]
/scene recovery backup-and-create <scene-id> <display-name>
/scene recovery export
/scene recovery discard-and-reload confirm
/scene tool give

/scene prop list
/scene prop <prop-id> create <asset-key>
/scene prop <prop-id> select
/scene prop <prop-id> position set <x> <y> <z>
/scene prop <prop-id> position here
/scene prop <prop-id> position add <x> <y> <z>
/scene prop <prop-id> rotation set <yaw> <pitch> <roll>
/scene prop <prop-id> rotation add <yaw> <pitch> <roll>
/scene prop <prop-id> scale set <value>
/scene prop <prop-id> clone <new-id>
/scene prop <prop-id> remove

/scene npc list
/scene npc <npc-id> create <body-asset-key>
/scene npc <npc-id> select
/scene npc <npc-id> position set <x> <y> <z>
/scene npc <npc-id> position here
/scene npc <npc-id> position add <x> <y> <z>
/scene npc <npc-id> rotation set <yaw> <pitch> <roll>
/scene npc <npc-id> rotation add <yaw> <pitch> <roll>
/scene npc <npc-id> scale set <value>
/scene npc <npc-id> label set <text>
/scene npc <npc-id> clone <new-id>
/scene npc <npc-id> remove

/scene lease status <element-id>
/scene lease release <element-id>
/scene catalogs status
```

Commands execute only for players in loaded build worlds except catalog status, which authorized console senders may use. Coordinates and transforms reject non-finite values before constructing scene DTOs. Asset completion lists only catalog entries of the required kind.

`/scene reload` refuses to discard a dirty document. After a concurrent disk change, the builder may first preserve the current intrinsic-valid bytes with `/scene recovery export`, then explicitly replace the in-memory document from disk with `/scene recovery discard-and-reload confirm`. The literal `confirm` argument is required and the command reports the discarded scene ID and fingerprint. Diagnostic exports use generated collision-resistant names; commands never accept arbitrary output paths.

Create mutations use deterministic defaults. Both prop and NPC creation place the element at the executing player's current position with the player's yaw, zero pitch and roll, unit scale, no group, visible state, automatic activation, and no initial animation. A prop needs no additional defaults. An NPC starts with no label, label offset `(0, 2.25, 0)`, fixed look behavior, no proximity sensor, and no trigger bindings. Its interaction bounds come from the selected `NPC_BODY` catalog entry; NPC creation fails if that entry has no default bounds.

Permissions mirror paths beneath `grounds.scene`, with broader convenience nodes such as `grounds.scene.edit`, `grounds.scene.save`, `grounds.scene.recovery`, `grounds.scene.catalogs.status`, and `grounds.scene.lease.override`. Server-side permission checks remain authoritative even when tab completion hides inaccessible paths.

Trigger-chain and CompositeProp-rig commands are excluded from the first vertical slice. Their future command roots are reserved as `/scene npc <id> trigger ...` and `/scene composite ...`.

## Paper preview

The Paper adapter renders a non-authoritative preview from the current document. Preview entities are marked non-persistent, tagged with plugin-owned persistent-data keys for identification while alive, and never saved into the world.

The first slice renders:

- props and NPC bodies as deterministic generic display-entity or bounding-box placeholders keyed by asset kind;
- labels as text displays;
- the selected element with an outline, axis markers, and an ID label visible only to the selecting player where Paper permits per-viewer visibility.

Preview rendering never changes the document. Asset IDs remain visible so a placeholder can be correlated with its catalog entry. Faithful model previews wait for a standardized `editorMetadata` key contract; the first release does not interpret unspecified metadata keys.

All Bukkit entity creation, mutation, and removal runs on the server thread. File and catalog I/O runs asynchronously, returning immutable results that the server thread applies only if the session generation still matches.

## Editor tool

The plugin gives an authorized builder a named editor tool through an explicit command. Tool behavior is active only when all of these are true:

- the player holds that exact plugin-tagged item;
- the player has an active session and selection;
- the selected element's lease belongs to the player;
- the player has edit permission.

Input mapping:

| Input | Result |
| --- | --- |
| Left click | Ray-select the nearest preview element. |
| Right click | Select the next transform component. |
| Sneak + right click | Select the previous transform component. |
| Mouse wheel | Apply one signed step and cancel the held-slot change. |
| Sneak + mouse wheel | Apply a fine step. |
| Sprint + mouse wheel | Apply a coarse step. |

Step sizes:

| Value | Fine | Normal | Coarse |
| --- | ---: | ---: | ---: |
| Position | 0.01 | 0.1 | 1 block |
| Rotation | 1 degree | 5 degrees | 15 degrees |
| Scale | 0.01 | 0.05 | 0.25 |

Position cycles through X, Y, and Z. Rotation cycles through yaw, pitch, and roll. Scale is uniform in the first slice. The action bar shows element ID, property, current value, step size, dirty state, and lease time remaining.

Outside active tool state, clicks and hotbar selection behave normally. The listener cancels `PlayerItemHeldEvent` only after it has accepted a valid editor step.

## Validation and save eligibility

The session maintains separate intrinsic and catalog validation results.

`save` succeeds only when:

- a document exists;
- the session is not in invalid-file recovery state;
- intrinsic validation has no problems;
- active catalogs exactly match the document's references;
- catalog validation has no problems;
- the disk fingerprint still matches the session base;
- no save is already running.

Validation output groups problems by element and field path, preserves deterministic ordering, and paginates long output. `validate` never mutates the document.

Dirty state means the current document differs from the last successfully saved document. Canonical encoded bytes are the comparison authority; object identity is not.

## BuildSystem publish integration

The editor does not replace or wrap `/map push` in the first release. Builders save first and then use the existing command.

The editor's `common` module publishes a Java-friendly read-only integration interface:

```java
public interface SceneEditStatus {
    boolean hasUnsavedChanges(UUID worldId);
}
```

The Paper plugin is named `GroundsSceneEditor`, embeds `common` in its deployable JAR without relocating its public API, and registers one implementation through Bukkit's `ServicesManager`. The `buildsystem-grounds` module adds the published `plugin-scene-editor:common` artifact as `compileOnly`, adds `GroundsSceneEditor` to its generated `plugin.yml` `softdepend`, and queries the service in `MapCommand` before `/map push` starts world-save, archive, or upload work. It does not bundle its compile-only copy, so Paper resolves the shared API class from the declared plugin dependency.

If the service reports a dirty session, `MapCommand` rejects the push with an actionable message naming `/scene save`. An absent plugin or service preserves today's push behavior. The interface exposes no mutation, save, or publication operation, and service-maps remains uninvolved.

After save, `/map push` continues to:

1. save the Bukkit world;
2. archive the exact world folder;
3. include root `scene.json`;
4. upload the source archive;
5. commit a version with `derive=true`;
6. poll the exact version status.

The editor reports only local save state. It never displays “published” based on a successful file write.

## Failure behavior

| Failure | Required behavior |
| --- | --- |
| Invalid existing JSON | Keep original bytes, enter recovery state, show ordered decode problems. |
| Document pins mismatch classpath catalogs | Allow editing, mark catalog-unverified, reject normal save. |
| Required asset catalog fails to initialize | Fail plugin enable; do not expose an unvalidated editor session. |
| Invalid mutation | Leave document, history, preview, and dirty state unchanged. |
| Preview failure | Keep document, show placeholder/problem, permit later refresh. |
| Concurrent disk change | Reject save; require optional export followed by confirmed discard-and-reload. |
| Atomic replace unsupported | Reject save and retain the previous file. |
| Async result from stale session | Discard it without touching the current session or preview. |
| Player disconnect | Release selection and leases; keep shared document session alive. |
| World unload or plugin disable | Remove previews, cancel tasks, release leases, report unsaved state. |

Failures use Adventure components with concise summaries and expandable command-driven detail. Logs include world, scene, element, and problem code where available, but never dump entire scene files or catalog payloads.

## Security boundaries

- The plugin represents declarative scene data only; it cannot encode arbitrary commands or scripts.
- Application-action authoring requires an exact active action-catalog definition.
- Scene paths are derived from operator-owned loaded worlds and normalized before access. Diagnostic exports remain below the plugin data directory.
- The repository never follows a symlink that would place `scene.json` outside the exact world root.
- Recovery backups remain siblings of `scene.json` and use collision-resistant timestamps.
- Commands recheck permissions at execution time.
- Preview entities carry no executable behavior and never survive plugin shutdown.
- The editor does not receive service credentials, R2 credentials, Kubernetes tokens, or presigned URLs.

## Lifecycle

`onEnable` performs only composition and registration:

1. Construct catalog providers from the pinned classpath asset catalog and local empty action catalog.
2. Construct common services.
3. Register `SceneEditStatus` through Bukkit's ServicesManager.
4. Register command executor, tab completer, and listeners.
5. Schedule lease expiry and preview maintenance through one shared task.

`onDisable` closes registrations and services in reverse order. Runtime registrations return `AutoCloseable` handles where practical. No global singleton exposes mutable editor state.

## Testing strategy

### Common unit tests

- Create, replace, clone, and remove prop/NPC mutations.
- Reject duplicate IDs, non-finite transforms, invalid scale, and wrong asset kind.
- Apply the exact prop/NPC creation defaults and reject an NPC body without default bounds.
- Preserve unchanged documents after rejected mutations.
- Bound history at 100 snapshots and prove deterministic undo/redo.
- Acquire, renew, expire, release, and administratively take over leases.
- Separate per-player selection from shared document state.
- Compute dirty state from canonical bytes.
- Preserve invalid source bytes and require explicit recovery.

### File tests

- Load absent, valid, and invalid `scene.json` from temporary world roots.
- Prove canonical encode/decode round trips with `scene-testkit`.
- Prove save uses the exact root path.
- Prove atomic replacement retains the old file on injected failure.
- Reject symlink escape and concurrent file modification.
- Prove recovery creates a byte-identical backup before a new document.

### Paper tests

Use MockBukkit where it supports the required Paper behavior:

- plugin lifecycle and service registration;
- command permissions and hierarchical completion;
- player/world session resolution;
- selection and lease cleanup on quit/world change;
- editor-tool identity and inactive hotbar behavior;
- accepted wheel input cancellation and correct step sizes;
- main-thread preview application and stale async-result rejection.

Use focused adapter tests with mocked Paper interfaces when MockBukkit does not implement display entities or `PlayerItemHeldEvent` precisely enough. Pure ray selection and transform math stay in `common`.

### Cross-repository contracts

- Build a world archive and assert `scene.json` remains at archive root.
- Build resourcepacks 0.6.0 and prove both bootstrap catalog entries have their required pack model paths.
- Decode the editor output through released `scene-format` 0.1.0.
- Run representative output through service-maps `SceneDeriver` with fake catalog resolution.
- Prove the editor does not claim publish success and blocks dirty-session push through the optional integration hook.

Cluster, Keycloak, presigned upload, R2 promotion, Kubernetes Job, and asynchronous service acceptance remain Phase 5 Stage tests and are not prerequisites for merging local editor logic.

## Acceptance criteria

- A builder can create a scene in a loaded build world without editing JSON.
- A builder can create, select, move, rotate, scale, clone, and delete one prop and one NPC.
- Command and editor-tool edits produce the same canonical document mutations.
- Two builders can work in one scene without editing the same leased element concurrently.
- Undo and redo restore exact earlier canonical documents.
- Invalid existing files and concurrent external edits cannot be overwritten silently.
- Normal save succeeds only with intrinsic-valid, catalog-compatible content.
- Saved bytes decode canonically through `scene-format` 0.1.0.
- Saved `scene.json` resides at the exact world root and enters the existing map archive unchanged.
- Plugin shutdown removes all previews, tasks, selections, and leases.
- The product requires no cluster, R2, or service credential to run its local editor functions.

## Deferred slices

- Trigger-chain authoring for safe scene actions and catalog-defined application actions.
- CompositeProp creation, part hierarchy, local transforms, and rig preview.
- Animation timeline editing.
- A centralized asset/action catalog discovery API.
- Explicit catalog-pin migration backed by asset compatibility rules.
- Standardized editor-preview metadata and faithful resource-pack model previews.
- Automatic invocation or orchestration of `/map push`.
- Displaying authoritative service-maps derive and publication status in the editor.
- Phase 7 Minestom runtime behavior and Phase 8 portal status UI.
