# Runtime Feature Docs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish public Mintlify docs for the new JVM service registry, Minestom runtime modules, Minestom distribution pushes, local Minestom module substitution, and Agones provider-based Minestom integration.

**Architecture:** Keep reusable API documentation under `Reference > Development libraries`, keep push/deploy workflow documentation under `Build`, and keep Agones-specific behavior under `Reference > Plugins SDK > Agones Integration`. The docs repo is the canonical public site; implementation repositories remain source-of-truth inputs.

**Tech Stack:** Mintlify MDX, `docs.json` navigation, Kotlin/Gradle examples, Grounds CLI/Forge manifest examples.

---

## File Structure

- Create `reference/development/jvm-modules/service-registry.mdx`: standalone `library-jvm-modules` service registry reference.
- Create `reference/development/minestom-runtime/index.mdx`: Minestom runtime overview.
- Create `reference/development/minestom-runtime/modules.mdx`: module/provider/service contract API reference.
- Create `reference/development/minestom-runtime/composition.mdx`: runtime composition and provider selection guide.
- Create `reference/development/minestom-runtime/local-modules.mdx`: local Minestom module substitution workflow.
- Modify `docs.json`: add a `Development libraries` group under the Reference tab.
- Modify `build/manifest.mdx`: document `minestom-server`, `build`, and `modules`.
- Modify `build/cli/push.mdx`: document Minestom push behavior.
- Modify `build/local-plugin-workspaces.mdx`: generalize plugin wording and add Minestom module examples.
- Modify `build/troubleshooting.mdx`: add Minestom troubleshooting entries.
- Modify `reference/plugins/agones/minestom.mdx`: make provider-based integration the primary path.

## Task 1: Add Development Library Navigation And Service Registry Docs

**Files:**
- Modify: `docs.json`
- Create: `reference/development/jvm-modules/service-registry.mdx`

- [ ] **Step 1: Add navigation group**

Update `docs.json` in the Reference tab after `Convention plugins` or before `Conventions`:

```json
{
  "group": "Development libraries",
  "pages": [
    "reference/development/jvm-modules/service-registry",
    {
      "group": "Minestom Runtime",
      "pages": [
        "reference/development/minestom-runtime/index",
        "reference/development/minestom-runtime/modules",
        "reference/development/minestom-runtime/composition",
        "reference/development/minestom-runtime/local-modules"
      ]
    }
  ]
}
```

- [ ] **Step 2: Write service registry page**

Create `reference/development/jvm-modules/service-registry.mdx` with:

- frontmatter title `Service Registry`;
- overview of type-first service access;
- `DefaultServiceRegistry` example;
- `serviceKey<T>()` and class-based overload examples;
- `qualifiedServiceKey<T>()` guidance for multiple implementations;
- descriptor example with `requires`, `provides`, and `dependsOn`;
- failure behavior for duplicate and missing services.

Use these concrete API examples:

```kotlin
val registry = DefaultServiceRegistry()

registry.register<MyService>(DefaultMyService())

val service = registry.require<MyService>()
```

```kotlin
registry.register(MyService::class, DefaultMyService())

val service = registry.require(MyService::class)
```

```kotlin
object NatsServiceKeys {
    val Public = qualifiedServiceKey<NatsClient>("grounds.nats.public")
    val Internal = qualifiedServiceKey<NatsClient>("grounds.nats.internal")
}
```

```kotlin
override val descriptor =
    ModuleDescriptor(
        id = "grounds.matchmaking",
        version = "1.0.0",
        requires = setOf(NatsServiceKeys.Internal),
        provides = setOf(serviceKey<MatchmakingService>()),
        dependsOn = setOf("grounds.config"),
    )
```

- [ ] **Step 3: Verify task 1 paths**

Run:

```bash
test -f reference/development/jvm-modules/service-registry.mdx
rg -n "reference/development/jvm-modules/service-registry|Development libraries" docs.json
```

Expected: both commands exit `0`.

## Task 2: Add Minestom Runtime Reference Pages

**Files:**
- Create: `reference/development/minestom-runtime/index.mdx`
- Create: `reference/development/minestom-runtime/modules.mdx`
- Create: `reference/development/minestom-runtime/composition.mdx`
- Create: `reference/development/minestom-runtime/local-modules.mdx`

- [ ] **Step 1: Write overview page**

Create `reference/development/minestom-runtime/index.mdx` with:

- title `Minestom Runtime`;
- explanation that Minestom has no hot plugin folder;
- build-time composition and immutable distributions;
- cards linking to modules, composition, local modules, manifest, and Agones Minestom.

- [ ] **Step 2: Write module API page**

Create `reference/development/minestom-runtime/modules.mdx` with:

- `GroundsModule` lifecycle code;
- `GroundsModuleProvider` code;
- `ModuleDescriptor` use;
- `serverTypes` compatibility;
- service contract declarations.

Use this API shape:

```kotlin
interface GroundsModule {
    val id: String

    fun install(ctx: GroundsServerContext)

    fun start() {}

    fun stop() {}
}
```

```kotlin
class MatchmakingModuleProvider : GroundsModuleProvider {
    override val id = "grounds.matchmaking"
    override val version = "1.0.0"
    override val serverTypes = setOf(ServerType.MINIGAME)
    override val descriptor =
        ModuleDescriptor(
            id = id,
            version = version,
            requires = setOf(serviceKey<PlayerService>()),
            provides = setOf(serviceKey<MatchmakingService>()),
        )

    override fun create(): GroundsModule = MatchmakingModule()
}
```

- [ ] **Step 3: Write composition page**

Create `reference/development/minestom-runtime/composition.mdx` with:

- `GroundsServer.builder()`;
- direct `use(provider)`;
- `discoverProviders()`;
- `useProvider("grounds.agones")`;
- selected provider validation failure text;
- startup lifecycle order.

Use this example:

```kotlin
fun main() {
    GroundsServer.builder()
        .discoverProviders()
        .useProvider("grounds.agones")
        .use(ExampleMinigameModuleProvider())
        .start()
}
```

- [ ] **Step 4: Write local modules page**

Create `reference/development/minestom-runtime/local-modules.mdx` with:

- `grounds.yaml` `modules` entries;
- workspace mappings with `variant: minestom`;
- `--local` and `--with-local`;
- Gradle composite build behavior;
- when `module` and `project` metadata are needed.

Use this manifest example:

```yaml grounds.yaml
name: minigame-agones
type: minestom-server
baseImage: minestom
build:
  task: :examples:minigame-agones:distTar
  artifact: examples/minigame-agones/build/distributions/*.tar
modules:
  - id: plugin-agones
    variant: minestom
    source: github:groundsgg/plugin-agones@v0.6.0:plugin-agones-minestom.jar
```

- [ ] **Step 5: Verify task 2 paths**

Run:

```bash
test -f reference/development/minestom-runtime/index.mdx
test -f reference/development/minestom-runtime/modules.mdx
test -f reference/development/minestom-runtime/composition.mdx
test -f reference/development/minestom-runtime/local-modules.mdx
```

Expected: command exits `0`.

## Task 3: Update Build Workflow Docs For Minestom Distributions

**Files:**
- Modify: `build/manifest.mdx`
- Modify: `build/cli/push.mdx`
- Modify: `build/local-plugin-workspaces.mdx`
- Modify: `build/troubleshooting.mdx`

- [ ] **Step 1: Update manifest reference**

Modify `build/manifest.mdx`:

- add `minestom-server` to the `type` table;
- add `minestom` to the `baseImage` table;
- add sections for `build` and `modules`;
- explain that `minestom-server` requires a distribution tar, not `jar`;
- clarify that `modules` is CLI composition metadata for release defaults and local substitution, while Gradle dependencies still define the classpath.

- [ ] **Step 2: Update push CLI reference**

Modify `build/cli/push.mdx`:

- add `--flavor=<key>` to synopsis examples;
- explain Minestom detection from `grounds.yaml`;
- document that CLI runs `build.task`, normalizes `build.artifact`, uploads it to Forge, and prints the effective source table when local module mappings apply.

- [ ] **Step 3: Update local workspace docs**

Modify `build/local-plugin-workspaces.mdx`:

- change the intro from only plugin workspaces to plugin/module workspaces;
- keep existing Paper/Velocity examples;
- add a Minestom module example:

```bash
grounds workspace add plugin-agones ~/grounds/plugin-agones \
  --variant minestom \
  --build './gradlew :minestom:build'
```

- describe optional `workspace.yaml` fields:

```yaml
repos:
  plugin-agones:
    path: /home/alice/grounds/plugin-agones
    variants:
      minestom:
        enabled: true
        module: gg.grounds:plugin-agones-minestom
        project: :minestom
```

- [ ] **Step 4: Update troubleshooting**

Modify `build/troubleshooting.mdx` with Minestom sections:

- `type 'minestom-server' requires a distribution bundle`;
- `expected at least one distribution tar`;
- `Grounds module provider grounds.agones was requested but not discovered`;
- `local module requires both module and project for dependency substitution`.

- [ ] **Step 5: Verify task 3 coverage**

Run:

```bash
rg -n "minestom-server|distribution bundle|build\\.task|build\\.artifact|grounds\\.agones|plugin-agones-minestom" build
```

Expected: command exits `0` and shows hits in all four modified Build pages.

## Task 4: Update Agones Minestom Docs

**Files:**
- Modify: `reference/plugins/agones/minestom.mdx`

- [ ] **Step 1: Make provider-based integration primary**

Rewrite the top of `reference/plugins/agones/minestom.mdx` so it:

- says `plugin-agones-minestom` `0.6.0` or newer exposes `grounds.agones`;
- uses `runtimeOnly("gg.grounds:plugin-agones-minestom:<version>")`;
- shows runtime composition with `discoverProviders().useProvider("grounds.agones")`;
- explains that the runtime calls `install`, `start`, and `stop`.

- [ ] **Step 2: Keep direct lifecycle as low-level only**

Move direct `GroundsPluginAgones().enable()` / `disable()` instructions into a later section named `Low-level direct usage`.

Open that section with:

```mdx
<Warning>
Use direct lifecycle calls only when you are embedding the Agones module without
`grounds-minestom-runtime`. Runtime-based servers should select the provider
instead.
</Warning>
```

- [ ] **Step 3: Verify Agones page no longer starts with direct lifecycle**

Run:

```bash
sed -n '1,120p' reference/plugins/agones/minestom.mdx
```

Expected: top section describes provider-based runtime integration, not `enable()`.

## Task 5: Validate, Commit, Push, And Open PR

**Files:**
- All files changed in Tasks 1-4.

- [ ] **Step 1: Run static checks**

Run:

```bash
git diff --check
rg -n "TODO|TBD|FIXME|placeholder" reference build docs/superpowers/plans/2026-06-17-runtime-feature-docs.md
```

Expected: `git diff --check` exits `0`. The placeholder scan exits non-zero or only reports intentional historical text outside changed files.

- [ ] **Step 2: Validate navigation references**

Run:

```bash
python3 - <<'PY'
import json
from pathlib import Path

cfg = json.loads(Path("docs.json").read_text())
missing = []

def walk(node):
    if isinstance(node, str):
        p = Path(node + ".mdx")
        if not p.exists():
            missing.append(str(p))
    elif isinstance(node, list):
        for item in node:
            walk(item)
    elif isinstance(node, dict):
        if "pages" in node:
            walk(node["pages"])

walk(cfg["navigation"]["tabs"])
if missing:
    raise SystemExit("missing docs.json pages: " + ", ".join(missing))
PY
```

Expected: command exits `0`.

- [ ] **Step 3: Run Mintlify validation if available**

Run:

```bash
if command -v mint >/dev/null 2>&1; then mint broken-links; else echo "mint CLI not installed; relying on docs CI"; fi
```

Expected: either Mintlify validation passes or the command clearly reports that local Mintlify is unavailable.

- [ ] **Step 4: Commit docs changes**

Run:

```bash
git add docs.json reference/development build reference/plugins/agones/minestom.mdx docs/superpowers/plans/2026-06-17-runtime-feature-docs.md
git commit -S -m "docs: document runtime module workflows"
```

Expected: signed commit succeeds.

- [ ] **Step 5: Push and open PR**

Run:

```bash
git push -u origin docs/runtime-feature-docs-plan
gh pr create --draft --title "docs: document runtime module workflows" --body-file /tmp/grounds-docs-runtime-feature-pr.md
```

Use the repository PR template if present. If there is no PR template, include:

```md
## Summary
- Document JVM service registry usage as a standalone development library reference
- Add Minestom runtime module, composition, and local module workflow docs
- Update Forge/CLI build workflow docs and Agones Minestom provider integration

## Testing
- [ ] git diff --check
- [ ] docs.json navigation reference check
- [ ] Mintlify validation or local CLI availability note
```

Expected: draft PR exists on `groundsgg/docs`.
