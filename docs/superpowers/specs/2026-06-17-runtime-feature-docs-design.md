# Runtime Feature Docs Design

## Context

Grounds now has several related runtime features that are implemented across different repositories:

- `library-jvm-modules` provides typed service keys, service registry access, module descriptors, graph validation, and `ServiceLoader` discovery primitives.
- `grounds-minestom-runtime` provides the Grounds-specific Minestom runtime API, module lifecycle, explicit provider selection, server type filtering, and example runtime composition.
- `plugin-agones` exposes a Minestom `GroundsModuleProvider` for `grounds.agones`.
- `grounds-cli` and `grounds-forge` support `minestom-server` pushes as distribution tar uploads, including local Minestom module substitution from workspace mappings.

The canonical public documentation lives in `groundsgg/docs`, a Mintlify site. The new docs should be added there, not only in the individual implementation repositories.

## Goals

- Document the service registry as its own reusable JVM module concept.
- Document the Minestom runtime as a development API, not as a single plugin.
- Document Forge and CLI behavior for Minestom distribution pushes.
- Update Agones Minestom docs to the provider-based runtime model.
- Keep workflow pages under `Build` and API/reference pages under `Reference`.
- Reuse the existing Mintlify style and navigation structure.

## Non-Goals

- Do not document unreleased future runtime services as shipped features.
- Do not replace Javadoc or Dokka output.
- Do not move implementation docs out of their repos in this change.
- Do not document a runtime plugin folder model. Minestom servers are built as immutable distributions.

## Recommended Documentation Structure

### Service Registry

Add `reference/development/jvm-modules/service-registry.mdx`.

This page documents `library-jvm-modules` independently from Minestom:

- when to use type-first service access;
- `serviceKey<T>()`;
- `qualifiedServiceKey<T>(...)`;
- `ServiceRegistry.register`, `get`, `require`, and `contains`;
- non-reified class-based access for Java-compatible or reflection-heavy call sites;
- module descriptors with `requires`, `provides`, and `dependsOn`;
- guidance that qualified keys should be defined once by the service owner and not created inline at call sites.

Navigation should add a `JVM modules` group under `Reference > Convention plugins` or a new adjacent `Development libraries` group. The preferred placement is a new `Development libraries` group because the service registry is not a Gradle convention plugin.

### Minestom Runtime

Add a new group under `Reference > Development libraries`:

```text
reference/development/minestom-runtime/index.mdx
reference/development/minestom-runtime/modules.mdx
reference/development/minestom-runtime/composition.mdx
reference/development/minestom-runtime/local-modules.mdx
```

Suggested page responsibilities:

- `index.mdx`: concept overview, build-time composition, immutable server distributions, relation to Minestom, and when to use this runtime.
- `modules.mdx`: `GroundsModule`, `GroundsModuleProvider`, `ModuleDescriptor`, lifecycle hooks, server type compatibility, service contracts, and provider metadata.
- `composition.mdx`: `GroundsServer.builder()`, `discoverProviders()`, `useProvider("grounds.agones")`, direct `use(...)`, startup logs, validation failures, and example runtime composition.
- `local-modules.mdx`: how `grounds.yaml` module entries and CLI workspace mappings let developers replace release Minestom modules with local builds.

This keeps API concepts separate from push workflow details while still linking to the Build pages for end-to-end pushing.

### Forge And CLI Build Workflow

Update existing Build pages instead of creating a parallel Minestom section:

- `build/manifest.mdx`: add `type: minestom-server`, `baseImage: minestom`, `build.task`, `build.artifact`, and `modules` fields.
- `build/cli/push.mdx`: document that `grounds push` handles `minestom-server` manifests directly, builds the configured distribution task, normalizes/uploads the tar artifact, and supports `--local` and `--with-local` module substitutions.
- `build/local-plugin-workspaces.mdx`: generalize wording from "plugin overrides" to "plugin and module workspace overrides" where needed, and add a Minestom example mapping for `plugin-agones` with variant `minestom`.
- `build/troubleshooting.mdx`: add Minestom-specific failures such as uploading a JAR instead of a tar distribution, missing distribution artifact, and requesting a module provider that is not on the runtime classpath.

### Agones Minestom

Update `reference/plugins/agones/minestom.mdx`.

The page currently describes direct manual `GroundsPluginAgones.enable()` and `disable()` usage. It should move the provider-based runtime model to the top:

- require `plugin-agones-minestom` `0.6.0` or newer for `GroundsModuleProvider` discovery;
- show `runtimeOnly("gg.grounds:plugin-agones-minestom:<version>")`;
- show `GroundsServer.builder().discoverProviders().useProvider("grounds.agones")`;
- explain that the runtime owns lifecycle calls;
- keep direct manual `GroundsPluginAgones` usage as a low-level/legacy section if still supported by the API.

## Navigation Design

Update `docs.json`:

- Under `Reference`, add a new group named `Development libraries`.
- Place `reference/development/jvm-modules/service-registry` in that group.
- Place the Minestom runtime pages in the same group.
- Keep `reference/plugins/agones/minestom` under `Plugins SDK > Agones Integration`.
- Keep Forge/CLI pages in the existing `Build` tab.

This avoids mixing workflow instructions with library reference material.

## Source Of Truth

Use current implementation code as source of truth:

- `library-jvm-modules` for service registry APIs and naming.
- `grounds-minestom-runtime` for runtime API, builder methods, environment parsing, examples, and module composition behavior.
- `plugin-agones` for the Minestom provider ID and version requirements.
- `grounds-cli` and `grounds-forge` for `grounds.yaml`, upload artifact shape, workspace mapping, and push behavior.

When examples mention versions, use released versions known to contain the documented feature. For Agones Minestom provider discovery, use `0.6.0` or newer.

## Testing And Validation

Validate the documentation change with:

- Mintlify navigation validation if a local command is available.
- Link/path checks by inspecting `docs.json` references and created files.
- Repository search for stale Minestom wording, especially old `enable()`/`disable()` first-path instructions.
- A final `git diff --check`.

If Mintlify CLI is not installed locally, document that limitation in the PR and rely on docs repository CI.

## Open Decisions

- Whether to keep direct `GroundsPluginAgones.enable()` documentation as a legacy section or remove it entirely if the public API should now be runtime-only.
- Whether to add a short `build/minestom-distributions.mdx` workflow page later if `build/manifest.mdx` and `build/cli/push.mdx` become too large.

## Acceptance Criteria

- Developers can find service registry docs without reading Minestom runtime docs.
- Developers can understand how to write a Minestom runtime module and expose services.
- Developers can understand how a Minestom server selects provider-backed modules.
- Developers can push a Minestom server distribution through Grounds.
- Agones Minestom docs no longer recommend the old direct lifecycle path as the primary integration.
