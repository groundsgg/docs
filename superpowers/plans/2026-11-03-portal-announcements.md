---
title: "Portal-Meldungen: Implementierungsplan"
description: "Sechs überprüfbare Arbeitspakete für zentrale Meldungen, gemeinsamen HUD-Renderer, Proxy-Anzeige und Portal-Verwaltung."
---

# Portal Announcements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Use superpowers:subagent-driven-development only if the user chooses delegation. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Pflege Meldungen im Portal und zeige Text samt Adresse auf einer gemeinsamen HUD-Fläche, mit expliziten Ziel-Servertypen pro Meldung und ausschließlich Lobby als Vorgabe.

**Architecture:** Forge speichert je Umgebung ein versioniertes Dokument in `service-config`; alle Velocity-Proxies lesen dieselbe Quelle. Ein gemeinsamer, Minestom-freier Renderer komponiert formatierte Textteile zu Flächen, während der jeweilige Proxy seine lokalen Spielerverbindungen anhand von Backend-Rolle und bestätigter HUD-Packfähigkeit verwaltet. Die alte Lobby-Anzeige wird vor Aktivierung des Proxy-Eigentümers abgeschaltet.

**Tech Stack:** Kotlin/JVM, Adventure 5.2.0 am Velocity-/Minestom-Adapter, Velocity 3.5.0-SNAPSHOT, Gradle; TypeScript, Fastify/Zod, Next.js 16.2.12/React/TanStack Query/Kumo; Postgres/service-config, NATS; Vitest/JUnit/Playwright und Python für die Deployment-Prüfung. Bestehende Toolchains erhalten, keine beiläufigen Versions-Upgrades.

**Spec:** [Portal-Meldungen: Entwurf und Umsetzungsplan](../specs/2026-11-03-portal-announcements-design.md). Proxy-Architektur und Auswahl pro Meldung sind im Chat bestätigt. Konkrete Limits und der deklarative Typenkatalog unten sind technische Festlegungen des Plan-Autors.

**Umsetzungsstand, aktualisiert am 3. November 2026:** Die Funktion und der größere Sand-Punkt (`•`, U+2022) sind auf allen drei Stage-Proxies aktiv. Die Portal-Worker-Version ist direkt Live aktiviert. Die Integration erfolgt ohne separate Releases, weil weitere Grounds-Chats an Portal und Moderation arbeiten. Der [Umsetzungsbericht](../reports/2026-11-03-portal-announcements-implementation.md) nennt die geprüften Artefakte und verbleibende Grenzen.

## Global Constraints

- Stage und Produktion behalten getrennte Dokumente und umgebungsbezogene Rechte. Kein Fallback zwischen Umgebungen.
- Jede Meldung besitzt eine explizite Liste stabiler Servertyp-Schlüssel. Neue Einträge: `enabled: false`, `serverTypes: ["lobby"]`.
- Verwende den tatsächlichen `grounds/server-type`-Wert aus der Discovery, keine Pod-Namen oder Präfix-Heuristiken.
- Text und Adresse teilen eine Fläche. Die Adresse ist ein lesbarer Hinweis, keine anklickbare Web-Aktion.
- Bestehende HUD-Font-Codepoints und die öffentliche `GroundsBossBar.Segment`-API bleiben kompatibel. Velocity darf Minestom nicht transitiv laden.
- Englisch ist die erforderliche Fallback-Sprache; Startsprachen: `en`, `de`, `fr`, `es`. Formatierung erfolgt strukturiert, niemals durch freies MiniMessage/HTML.
- Höchstens zwei vollständige Meldungen pro Zeile, Gruppenwechsel alle `10_000 ms` anhand UTC-Epochenzeit. Niemals eine Meldung zwischen Gruppen aufteilen.
- Keine Anzeige ohne bestätigte Backend-Rolle, passenden Zieltyp und bestätigte HUD-Capability des tatsächlich geladenen Packs.
- Autoritativer Snapshot plus periodischer Abgleich; NATS ist ausschließlich ein Refresh-Trigger. Leere Konfiguration ist kein Ausfall.
- Bestehende Arbeitskopien, uncommittete Änderungen und bereits vorhandene Worktrees nicht zurücksetzen. Produktänderungen je Repository separat committen.
- Dieser Plan autorisiert keine Live-Aktivierung. Stage- und Produktions-Rollouts werden erst im ausdrücklich beauftragten Deployment ausgeführt. Portal später direkt über die passende Cloudflare-Worker-Version Live aktivieren; keinen Portal-Releaseprozess starten.

## Review Focus

1. Verspätetes HTTP-/Pack-/Discovery-Ergebnis nach Wechsel oder Disconnect darf keine Meldung auf der neuen Verbindung wiederherstellen — Task 4.
2. Mischversionen dürfen nicht zwei Meldungsbars anzeigen; eine kurz leere Zeile beim Eigentümerwechsel ist zulässig — Task 6.
3. Ein leeres Dokument, eine erste nicht vorhandene Konfiguration und ein zeitweiliger Ausfall sind unterschiedliche Zustände — Tasks 2, 4, 6.
4. Entfernte Zieltypen und fehlende Übersetzungen dürfen vorhandene Entwürfe weder löschen noch auf andere Server ausweiten — Tasks 2, 5.
5. Fettung, mehrere Textteile, Umlaute/Akzente und unbekannte Glyphen dürfen Preview und tatsächliche Breitenprüfung nicht auseinanderlaufen lassen — Tasks 1, 2, 5.

## Ausgangsstände und Arbeitsverzeichnisse

Alle Repo-Pfade beziehen sich auf `/home/lukas/grounds`. Die folgenden Stände sind lokal gelesen, keine Aussage über Live-Versionen. Vor Codeänderungen geeignete vorhandene Worktrees und anwendbare `AGENTS.md` prüfen; keine neuen Worktrees für das Schreiben dieses Plans nötig.

| Repository | Untersuchte Arbeitskopie | Lokaler HEAD | Relevanter Bestand |
| --- | --- | --- | --- |
| `library-minigame-api` | `.worktrees/scoreboard-library-minigame-api` | `65fdf99` | `GroundsBossBar`, `DefaultFontAdvances`, Tests |
| `plugin-proxy` | `plugin-proxy` | `a5e78c1` | `ServerDisplayQuery`, `PlayerLocaleQuery`, NATS, Lifecycle |
| `plugin-agones` | `.worktrees/scoreboard-plugin-agones` | `dbd950d` | Registrierung von `DiscoveryServerDisplayQuery`; älterer Hauptcheckout enthält diese noch nicht |
| `plugin-resourcepacks` | `plugin-resourcepacks` | `0ce5b94` | Pack-ID-/Hash-Tracker, Chat-/Sidebar-Fähigkeiten |
| `plugin-lobby` | `.worktrees/queue-scene-plugin-lobby` | `b4cb08d` | Alte Titelbar und vier Übersetzungsdateien |
| `grounds-forge` | `grounds-forge` | `3b56a24` | Netzwerk-API, Config-Client, Umgebungsrechte/Audit |
| `grounds-portal` | `.worktrees/grounds-portal-contextual-motd` | `0f03593` | Netzwerkverwaltung, Chat-Panel, Dirty-State/Umgebungswechsel |
| `deploy` | `deploy` | Bei Ausführung festhalten | Stage-Komponenten, Python-Topologietests |

Abhängigkeiten: Task 1 definiert Geometrie/Fixtures; Task 2 nutzt sie für API-Validierung; Task 3 liefert Fähigkeiten und Serverrollen für Task 4; Task 5 nutzt Task 2 und die Geometrie; Task 6 integriert alle Ergebnisse. Kein Arbeitspaket darf allein durch Aktivieren seines Builds die Live-Anzeige umschalten.

## Gemeinsame Verträge und technische Festlegungen

**Dokument:** `apps/velocity/envs/{stage|prod}/namespaces/announcements/documents/active`. Schema aus dem Entwurf unverändert. Neue zentrale Datei `library-minigame-api/hud/contract/announcements-v1.json` enthält Schema-Metadaten, gemeinsame gültige/ungültige Beispiele, Seeds und Layout-Erwartungen; ein Exportskript erstellt Byte-identische Fixture-Kopien für Forge und Portal. Jede Kopie trägt Quellrevision und SHA-256 in einer Begleitdatei; Tests vergleichen den Fixture-Inhalt, keine Laufzeit-Symlinks zwischen Repositories.

**Limits:** maximal 32 Meldungen; Name 1–80 Zeichen; Text je unterstützter Sprache 1–256 Zeichen, `en` erforderlich; Adresse `null` oder 1–128 Zeichen; höchstens 32 eindeutige Zieltypen je Meldung und 64 Zeichen je Schlüssel. Zieltyp-Schlüssel: `[a-z0-9][a-z0-9._-]{0,63}`, kein `*`; UUIDs eindeutig; Objektfelder strikt; Array-Reihenfolge ist Anzeige-Reihenfolge. Aktive Einträge brauchen mindestens einen Zieltyp. Steuerzeichen, Zeilenumbrüche, Formatierungszeichen `§`, PUA-HUD-Glyphen und nicht unterstützte Fontzeichen werden mit Feldpfad abgelehnt; Klartext wie `<test>` wird niemals als Markup geparst. UTF-8-Dokument maximal 65_536 Bytes vor dem Speichern. HTTP-Wrapper behält zusätzlich die bestehende `262144`-Byte-Grenze.

**Geometrie:** versionierte `hud/contract/metrics-v1.json` enthält überprüfte Zeichen-Advances, unterstützte Zeichen, Farbwerte und Layoutkonstanten. Startbudget `400` logische GUI-Pixel, Pill-Innenabstand `3 px`, Kappen `2 px`, Abstand Text/Adresse `8 px`, Abstand zwischen Meldungen `8 px`; Text `#F3F0EA`, Adresse `#E7B84B` fett, Hintergrund `#181D20`. Diese Startwerte sind vor Aktivierung nativ zu prüfen. Ergibt die Client-Prüfung andere Werte, alle drei Verbraucher samt Fixtures in einem abgestimmten Update ändern. Die bisherigen Segment-Methoden behalten ihren Abstand `2 px` und bisheriges Verhalten.

**Metriken:** extrahiere die vorhandene Advance-Logik; ergänze die tatsächlichen Metriken der für die vier Sprachen benötigten Zeichen anhand des vorhandenen Zielclient-/Fontbestands. Neue Meldungen akzeptieren nur Zeichen mit verifizierten Metriken; kein geschätzter `6 px`-Fallback für neue Inhalte. Die Legacy-API behält ihren bisherigen Fallback. Setze Schriftart, Fettung und andere Dekorationen explizit, damit Client-Stilvererbung keine andere Breite erzeugt. Verwende dieselben JSON-Werte im JVM-Renderer, in Forge und Portal; keine Browser-Canvas-Schätzung für die Publikationsprüfung.

**Servertypen-Katalog:** Forge bekommt `networkServerTypes: Record<NetworkEnvironment, string[]>` über `NETWORK_SERVER_TYPES_STAGE` und `NETWORK_SERVER_TYPES_PROD` als JSON-Arrays, mit Default `["lobby"]`. Betreiber konfigurieren dort die tatsächlichen `grounds/server-type`-Rollen aus ihren Deployment-Werten. Der aktuelle Bestand verwendet beispielsweise `lobby`, `game`, `match`; einzelne BedWars-Flotten tragen `kind: game` und sind damit nicht separat auswählbar. Katalogantwort `{key, configured}` ist die Vereinigung deklarierter Rollen und bereits gespeicherter Ziele. `configured: false` bedeutet „nicht im aktuellen Katalog“, niemals „offline“. Bestehende nicht deklarierte Ziele dürfen beim Speichern erhalten bleiben; neue Zielzuweisungen benötigen einen Katalogeintrag. Validierung vergleicht gegen den Stand der `expectedVersion`; Rennbedingungen dürfen nicht zu unbedingtem Schreiben führen.

**Anzeigeplan:** `selectAnnouncementGroups(settings, serverType, locale)` wählt aktivierte/exakt passende Einträge in Reihenfolge, löst Übersetzungen mit `en`-Fallback auf und packt gierig höchstens zwei ganze Flächen in `400 px`. Zeilenindex: `floor(epochMillis / 10_000) mod groups.length`; leere Gruppenliste ergibt keine Bar. Alle vorhandenen Übersetzungen eines Eintrags werden beim Schreiben gegen das Einzel-Flächenbudget geprüft.

**Synchronisierung:** sofortiger Snapshot beim Start, danach spätestens alle `15 s`; NATS-Trigger zusammenfassen und höchstens einen Read gleichzeitig ausführen. HTTP-Deadline `5 s`; rotierende ServiceAccount-Tokens pro Anfrage lesen. `If-None-Match`/ETag nutzen, Revisionen als Kotlin `Long` bzw. JSON-Dezimalstrings im Portal behandeln. Keine Konfigurationsschreibrechte für den Proxy hinzufügen. Ein erster fehlender Eintrag bedeutet keine Anzeige; ein später fehlender Eintrag oder eine gültige leere Liste entfernt sie; Timeout/invalides Dokument behält den letzten gültigen Stand und meldet ihn als veraltet.

**Eigentümer:** Deployment-Variable `GROUNDS_ANNOUNCEMENTS_OWNER=legacy|disabled|proxy`, Default `legacy`; unbekannter Wert bedeutet bei beiden Implementierungen keine eigene Anzeige plus Konfigurationsfehler. Lobby rendert nur bei `legacy`, Proxy nur bei `proxy`. Umschaltung ist sequenziell, siehe Task 6; die Variable gehört nicht ins Portal-Dokument.

---

### Task 1: Gemeinsame HUD-Komposition und sprachübergreifende Geometrie

**Files — `library-minigame-api`:**
- Modify: `settings.gradle.kts`, `.github/workflows/ci.yml`, `minestom/build.gradle.kts`, `minestom/src/main/kotlin/gg/grounds/minigame/minestom/GroundsBossBar.kt`, `DefaultFontAdvances.kt` im selben Verzeichnis.
- Create: `hud/build.gradle.kts`, `hud/src/main/kotlin/gg/grounds/hud/HudPills.kt`, `HudMetrics.kt` im selben Verzeichnis, `hud/contract/metrics-v1.json`, `hud/contract/announcements-v1.json`, `hud/tools/export-contract.py`.
- Tests: `hud/src/test/kotlin/gg/grounds/hud/HudPillsTest.kt`, vorhandener `minestom/src/test/kotlin/gg/grounds/minigame/minestom/GroundsBossBarTest.kt`.

**Interfaces:** Produces `HudRun(text: String, rgb: Int, bold: Boolean = false)`, `HudPill(runs: List<HudRun>)`, `HudPills.width(pill: HudPill): Int`, `HudPills.render(pills: List<HudPill>, gapPx: Int = 8): Component`, `HudMetrics.advance(codePoint: Int, bold: Boolean): Int?`. Artifact `gg.grounds:library-minigame-api-hud`; public Adventure types compile-only gegen den Host, kein Minestom-/Paper-/Netty-Import. Export: `python3 hud/tools/export-contract.py --target <forge|portal> --root <absolute-target-repo>` schreibt nur die in Task 2/5 benannten Contract-Dateien und deren Manifest.

- [ ] **Step 1 — Failing tests:** `one_background_contains_white_caption_and_gold_bold_address` prüft eine linke/rechte Kappe und beide Textstile; `two_announcements_have_two_backgrounds` prüft zwei Kappenpaare und `8 px` Abstand. Assertions: `width(HudPill([run("abc")])) == 23` bei drei `6 px`-Advances; `advance('i', false) == 2`; unbekanntes Zeichen ergibt `null`. `legacy_segment_api_keeps_2px_gap` erhält vorhandene Erwartungen. Fixtures enthalten alle 12 Seed-Übersetzungen, Akzente und genau/am/über dem Pixelbudget liegende Beispiele.
- [ ] **Step 2 — Red:** `./gradlew :hud:test :minestom:test --tests '*HudPillsTest' --tests '*GroundsBossBarTest'`; erwarteter Fehler: Modul/Klassen fehlen, nicht Infrastruktur oder Credentials.
- [ ] **Step 3 — Implementation:** Baue das kleine `hud`-Modul und delegiere die alte API an gemeinsame Primitive, ohne deren Aufrufvertrag zu verändern. Messe Unicode-Codepoints statt UTF-16-Hälften; exportiere verifizierte Metriken/Fixtures. Nimm `metrics-v1.json` über `processResources` als Klassenpfad-Ressource in das HUD-JAR auf und prüfe das Lesen aus dem gebauten JAR, damit die Laufzeit nicht vom Quellcheckout abhängt. Bestätige, dass die existierenden HUD-Slices mehrere Textstile unter einer Fläche erlauben; keine neuen Pack-Codepoints.
- [ ] **Step 4 — Green:** gleicher Testbefehl plus `./gradlew :hud:dependencies --configuration runtimeClasspath`; PASS, Runtimegraph ohne Minestom/Paper. Prüfe das Ergebnis im Zielclient bei GUI-Skalen 2/3/4, 1280×720 und 1920×1080; protokolliere Abweichungen vor Freigabe der Geometrie. Ohne Client-Nachweis ist der Renderer baubar, aber nicht für Aktivierung freigegeben.
- [ ] **Step 5 — Commit:** ausschließlich Task-Dateien, `feat(hud): compose formatted text within shared announcement pills`.

### Task 2: Meldungsdokument, Rollen-Katalog und geschützte Netzwerk-API

**Files — `grounds-forge`:**
- Create: `src/network/announcements.ts`, `announcementLayout.ts`, `serverTypes.ts` im selben Verzeichnis; `src/network/contracts/{announcements-v1,metrics-v1,manifest}.json`.
- Modify: `src/network/settings.ts`, `src/network/configClient.ts`, `src/routes/network.ts`, `src/main.ts`, `src/config.ts`.
- Tests: `tests/networkAnnouncements.test.ts`, `tests/networkAnnouncementRoutes.test.ts`, `tests/networkAnnouncementLayout.test.ts`, vorhandene `tests/networkConfig.test.ts`, `tests/config.test.ts`.

**Interfaces:** Consumes Task-1-Vertrag. Produces `Announcement`, `AnnouncementSettings`, `parseAnnouncementSettings(input: unknown): AnnouncementSettings`, `defaultAnnouncementSettings(): AnnouncementSettings` (leere Liste), `seedAnnouncementSettings(): AnnouncementSettings` (3 Legacy-Einträge), `announcementGroups(settings, serverType, locale): Announcement[][]`, `getAnnouncementServerTypes(declared: string[], stored: AnnouncementSettings): {key: string; configured: boolean}[]`. Erweitert `NetworkDocumentKind`/`NetworkSettingsMap` um `announcements`; GET/PUT `/v1/admin/network/:environment/announcements`, GET `/v1/admin/network/:environment/announcement-server-types`. GET-Dokumentantwort bleibt `NetworkDocument<AnnouncementSettings>`, PUT `{expectedVersion, settings}`. Keine Änderung der bestehenden Overview-Antwort erforderlich.

- [ ] **Step 1 — Failing tests:** `rejects_invalid_and_overwide_announcements` prüft gemeinsame Fixtures, doppelte UUIDs, unbekannte Schema-Version, Wildcards, leere `en`-Texte und unzulässige Glyphen; `empty_document_is_not_seeded` erwartet `announcements: []`. Routenfälle erwarten 403 ohne umgebungsbezogenes Recht, 409 bei Versionskonflikt, getrennte Stage/Prod-Speicherung und Audit `network.announcements.update`. `removed_role_is_preserved_but_cannot_be_newly_assigned` erhält Alt-Ziele, lehnt neue Zuweisungen ab; GET ändert weder Daten noch Seed.
- [ ] **Step 2 — Red:** `npm test -- tests/networkAnnouncements.test.ts tests/networkAnnouncementRoutes.test.ts tests/networkAnnouncementLayout.test.ts tests/networkConfig.test.ts tests/config.test.ts`; FAIL wegen fehlender Funktion/Route, nicht ungeklärter Abhängigkeiten.
- [ ] **Step 3 — Implementation:** Exportiere Contract-Dateien mit Task 1, validiere Byte- und Pixelbudget vor Config-PUT. Erweitere bestehende Dispatches vollständig: insbesondere darf `parseStoredSettings` Meldungen nicht als MOTD/Editor-Dokument behandeln. Verwende `networkPermissionRequirement(environment, write)` für alle neuen Routen und das bestehende CAS-Verfahren für Katalogprüfung plus Update. Typenkatalog aus zwei neuen Env-Variablen strikt parsen; unbekannte aktuelle Targets beim Lesen erhalten.
- [ ] **Step 4 — Green:** gleicher Testbefehl plus `npm run typecheck`; PASS. Beim kompletten relevanten Netzwerk-Testlauf auch `tests/networkRoutes.test.ts`/`tests/networkPresenceRoutes.test.ts` einbeziehen, um vorhandene Funktionen abzusichern.
- [ ] **Step 5 — Commit:** `feat(network): manage versioned announcements and explicit server roles`.

### Task 3: Bestätigte HUD-Fähigkeit und vorhandene Discovery-Anbindung

**Files — `plugin-proxy`:** Create `api/src/main/kotlin/gg/grounds/proxy/api/PlayerHudAppearanceQuery.kt`; Test `api/src/test/kotlin/gg/grounds/proxy/api/PlayerHudAppearanceTest.kt`.

**Files — `plugin-resourcepacks`:** Create `velocity/src/main/kotlin/gg/grounds/resourcepacks/velocity/PlayerHudAppearance.kt`; Modify `GroundsResourcePacksPlugin.kt` im selben Verzeichnis, `velocity/build.gradle.kts`; Tests `PlayerHudAppearanceTest.kt`, `GroundsResourcePacksPluginTest.kt` im bestehenden Testpaket.

**Files — `plugin-agones`:** Falls der ausgewählte Zielstand sie noch nicht enthält, integriere die bereits vorhandenen `velocity/src/main/kotlin/gg/grounds/discovery/DiscoveryServerDisplayQuery.kt`, Registrierung/Abmeldung in `gg/grounds/GroundsPluginAgones.kt` und `DiscoveryServerDisplayQueryTest.kt` aus dem untersuchten neueren Stand. Keine zweite Registry oder Namensheuristik hinzufügen.

**Interfaces:** Produces `PlayerHudAppearanceQuery.appearanceOf(playerId: UUID): PlayerHudAppearance`, `PlayerHudAppearance(ready: Boolean = false)` über dieselbe vorhandene `ProxyServiceRegistry`-Klasse. Konsumiert `HUD_FONT_PACK_SHA1S` (explizite lowercase SHA-1-Allowlist), tatsächlich angebotene/geladene Pack-IDs, Protokoll- und Plattformnachweis. Beginne mit dem im Zielstand nativ verifizierten Java-Protokoll `777`; Bedrock/unknown bleibt `ready=false`. Liefert `ServerDisplayQuery.displayOf(serverName)?.kind` aus tatsächlichen Discovery-Metadaten.

- [ ] **Step 1 — Failing tests:** `chat_or_sidebar_readiness_does_not_imply_hud_readiness`, `wrong_hash_or_reused_pack_id_stays_false`, `accepted_but_not_loaded_stays_false`, `removal_disconnect_and_pack_replacement_revoke_hud`. Ein verspätetes SUCCESSFUL der alten Spielersession aktiviert die neue Session nicht. `discovery_kind_is_label_not_server_name` prüft fremde Servernamen, fehlende Rollen und exakte `game`/`lobby`-Werte.
- [ ] **Step 2 — Red:** `./gradlew :api:test --tests '*PlayerHudAppearanceTest'` in Proxy, `./gradlew :velocity:test --tests '*PlayerHudAppearanceTest' --tests '*GroundsResourcePacksPluginTest'` in Resourcepacks und bei Bedarf `./gradlew :velocity:test --tests '*DiscoveryServerDisplayQueryTest'` in Agones. Fehlende neue Klassen sind der erwartete Red-Grund.
- [ ] **Step 3 — Implementation:** Veröffentliche die API zunächst kompatibel; bump/pinne die tatsächlich erzeugte Version in Resourcepacks und den Plattform-Artefakten. Verwende die vorhandenen Tracker-Primitiven mit separater HUD-Allowlist und Sessionprüfung. Registriere die Query genau einmal, entferne sie beim Shutdown. Behandle Pack-Entfernung/Ersetzung zusätzlich zu initialen Ladeereignissen; die vorhandene Initial-Deadline-Logik allein ist kein vollständiger Lebenszyklus.
- [ ] **Step 4 — Green:** obige Tests plus vorhandene `PlayerChatAppearanceTest`, `PlayerSidebarAppearanceTest`, `ResourcePackCoordinatorTest`; PASS. Prüfe, dass API und Verbraucher dieselbe Klassenidentität aus dem Host verwenden und keine mitgeschattete zweite `ProxyServiceRegistry` erzeugen.
- [ ] **Step 5 — Commit:** je Repository fokussiert `feat(api): expose verified HUD appearance`, `feat(resourcepacks): track loaded HUD capabilities`; Agones nur bei notwendiger Integration separat committen.

### Task 4: Proxy-Controller mit Snapshot-Abgleich und Serverwechseln

**Files — `plugin-proxy`:**
- Create unter `velocity/src/main/kotlin/gg/grounds/proxy/velocity/announcements/`: `AnnouncementSettings.kt`, `AnnouncementLayout.kt`, `AnnouncementConfigStore.kt`, `AnnouncementSynchronizer.kt`, `AnnouncementController.kt`, `AnnouncementOwnership.kt`; Contract-Fixtures unter `velocity/src/test/resources/announcements/` aus Task 1.
- Modify: `velocity/src/main/kotlin/gg/grounds/proxy/velocity/GroundsProxyPlugin.kt`, `velocity/build.gradle.kts`.
- Tests im entsprechenden Testpaket: `AnnouncementSettingsTest.kt`, `AnnouncementLayoutTest.kt`, `AnnouncementConfigStoreTest.kt`, `AnnouncementControllerTest.kt`, `AnnouncementSynchronizerTest.kt`.

**Interfaces:** Consumes Tasks 1–3 sowie `PlayerLocaleQuery.localeOf(UUID)`, danach `Player.effectiveLocale`, danach `en`. Produces `AnnouncementController.refresh(player: Player)`, `refreshAll()`, `forget(player: Player)`, `close()`. Reiner Selektor `AnnouncementLayout.groups(settings, serverType: String, locale: Locale): List<List<HudPill>>`, `groupAt(groups, epochMillis: Long): List<HudPill>?`; Store `read(knownVersion: Long?): AnnouncementSnapshot`, Snapshot unterscheidet `Unchanged`, `Missing`, `Present(version, settings)`; Netzwerkfehler sind Exceptions. Synchronizer liefert nur autoritative validierte Werte, Controller betreibt kein HTTP.

- [ ] **Step 1 — Failing tests:** `lobby_game_lobby_switch_filters_immediately` erwartet Anzeige/keine Anzeige/Anzeige; `two_proxies_only_touch_their_local_players` erhält insgesamt genau eine Bar pro Verbindung. Fake-Clock: `0`, `9_999`, `10_000`, `20_000` wählen deterministische Gruppen; `de-DE` normalisiert auf `de`, fehlendes `fr` auf `en`. `late_snapshot_and_pack_callback_cannot_revive_disconnected_session`, `missed_nats_recovers_in_15_seconds`, `missing_or_empty_clears_but_timeout_preserves_last_good`, `unchanged_content_does_not_send_duplicate_bar_updates` sichern die genannten Failure-Modes.
- [ ] **Step 2 — Red:** `./gradlew :velocity:test --tests '*Announcement*Test'`; FAIL wegen fehlender Implementierung. Store-Tests verwenden lokalen HTTP-Fakeserver und kontrollierten Executor, keine Live-Dienste.
- [ ] **Step 3 — Implementation:** Store verwendet den autoritativen Consumer-Snapshot unter `/v1/config/apps/velocity/envs/{env}/snapshot` mit ETag (gegen bestehenden HTTP-Vertrag prüfen), bounded Response und aktuellem Token. Subscribe auf `config.velocity.{env}.changed`, NATS-Inhalt niemals direkt anwenden. Serialisiere Reads, prüfe Sessionidentität/Backend beim Anwenden; evaluiere vorhandene Queries bei jedem Refresh neu. Controller verwaltet eine eigene Bossbar-ID pro Verbindung, entfernt nur diese Bar und aktualisiert nur bei Änderung. Lifecycle: Post-Connect sofort, Backend-Wechsel zum Verbindungsbeginn ausblenden, danach neu prüfen, Pack/Locale und verspätete Discovery über einen lokalen `1 s`-Refresh, Disconnect/Shutdown bereinigen. Owner `legacy`/`disabled` erzeugt keine Proxy-Bar; ungültige Umgebung aktiviert nichts.
- [ ] **Step 4 — Green:** obiger Testbefehl plus `./gradlew :api:test :velocity:test :velocity:shadowJar`; PASS. Prüfe `:velocity:dependencies --configuration runtimeClasspath` ohne Minestom und das gebaute JAR auf doppelte API-/Registry-Klassen.
- [ ] **Step 5 — Commit:** `feat(proxy): display centrally configured announcements by backend role`.

### Task 5: Portal-Verwaltung und geometrisch übereinstimmende Vorschau

**Files — `grounds-portal`:**
- Create: `src/lib/network-announcements.ts`, `src/lib/announcement-layout.ts`, `src/lib/api/network-announcements.ts`, `src/lib/announcements-contract/{announcements-v1,metrics-v1,manifest}.json`.
- Create unter `src/components/network/`: `network-announcements-panel.tsx`, `announcement-editor.tsx`, `announcement-preview.tsx`.
- Modify: `src/components/network/network-maintenance-page.tsx`; vorhandene generische BFF-Route nur ändern, falls ein konkreter Allowlist-Eintrag nötig ist.
- Tests: neue gleichnamige `.test.ts`/`.test.tsx` neben Library-/Hook-/Komponentendateien, bestehender `network-maintenance-page.test.tsx`, `tests/e2e/network-announcements.spec.ts`.

**Interfaces:** Consumes Tasks 1–2: unveränderter Dokumentvertrag und Typenkatalog. Produces `useNetworkAnnouncements(environment)`, `useUpdateNetworkAnnouncements(environment)`, `useAnnouncementServerTypes(environment)`, `NetworkAnnouncementsPanel({environment, canManage, onDirtyChange})`; Querykeys `[...qk.network(environment), "announcements"]` und `[...qk.network(environment), "announcement-server-types"]` ohne Vermischen mit dem Overview. Vorschau nutzt `announcementGroups` nach identischen Fixture-Regeln; sichtbare Fläche je Meldung enthält beide Textteile.

- [ ] **Step 1 — Failing tests:** `new_draft_targets_only_lobby_and_is_disabled`, `editing_keeps_missing_catalog_roles`, `reordering_preserves_ids_and_translations`, `readonly_user_cannot_mutate`, `409_keeps_draft_and_never_auto_retries`, `failed_put_never_shows_saved`, `environment_switch_does_not_save_draft_to_other_environment`. Fixture-Tests prüfen gleiche Gruppen/Pixelbreiten wie JVM/Forge einschließlich aller Seed-Sprachen; Vorschauprüfung zählt eine Fläche pro Meldung statt pro Textteil.
- [ ] **Step 2 — Red:** `npm test -- src/lib/announcement-layout.test.ts src/lib/api/network-announcements.test.tsx src/components/network/network-announcements-panel.test.tsx src/components/network/announcement-preview.test.tsx src/components/network/network-maintenance-page.test.tsx`; FAIL wegen fehlender Dateien/Komponenten.
- [ ] **Step 3 — Implementation:** Vor Codeänderung die lokalen Next-Guides laut `AGENTS.md` lesen. Füge einen Tab `Announcements` in bestehende Netzwerkverwaltung ein, mit lazy geladenem Panel und erhaltenem Dirty-State beim Tabwechsel. Inputs: interner Name, vier Sprachen, optionale Adresse, explizite Rollen, Aktivierung. Für Reihenfolge reichen zugängliche Nach-oben/-unten-Buttons. Nutze Kumo-Controls und vorhandene Stile; keine neue UI-Library. Vorschau erlaubt Sprache/Rolle und manuelles Durchblättern der Gruppen; visuelle Browser-Schrift ist eine Annäherung, Geometrie und Validierung stammen aus Contract-Werten. Speichern ersetzt den Cache erst bei bestätigtem Erfolg und sperrt keine anderen Tabs dauerhaft.
- [ ] **Step 4 — Green:** gleicher Testbefehl, `npx tsc --noEmit`, ESLint auf geänderten TS/TSX-Dateien und `npx playwright test tests/e2e/network-announcements.spec.ts`; PASS. E2E mit gemockter API: erstellen → Übersetzung ergänzen → Rolle ändern → Reihenfolge ändern → speichern → neu laden → dieselben Werte. Zusätzlich Produktionsansicht mit nur Stage-Recht sperren; kein echter Produktions-PUT.
- [ ] **Step 5 — Commit:** `feat(portal): edit and preview announcements per server role`.

### Task 6: Idempotenter Seed, Legacy-Abschaltung und überprüfbarer Rollout

**Files — `plugin-lobby`:** Modify `minestom/src/main/kotlin/gg/grounds/lobby/LobbyTitleBar.kt`, `GroundsPluginLobby.kt` im selben Paket; Create `AnnouncementOwnership.kt`, Test `minestom/src/test/kotlin/gg/grounds/lobby/AnnouncementOwnershipTest.kt`, bestehender `LobbyTitleBarTest.kt`. Beim Shutdown alle eigenen Bars schließen, nicht nur den Scheduler abbrechen.

**Files — `grounds-forge`:** Create `scripts/seed-network-announcements.ts`, Test `tests/networkAnnouncementSeed.test.ts`. Seed verwendet den vorhandenen Config-Client mit `expectedVersion: null`, schreibt ausschließlich bei wirklich fehlendem Dokument und behandelt konkurrierenden Create-Konflikt durch erneutes Lesen ohne Overwrite. Ausführung explizit mit `--environment stage`; kein Default auf Produktion.

**Files — `deploy`:** Create `tests/test_stage_announcements.py`, `docs/operations/announcements-rollout.md`; vorbereitete Änderungen in `environments/stage/components/{velocity,velocity-bedrock,lobby}/values.yaml`, gepinnte Artefaktstände in `environments/stage/components/{plugin-proxy,plugin-resourcepacks,plugin-agones}/values.yaml` nach vorhandenen Komponentennamen. Erweitere die tatsächliche Forge-Deployment-Konfiguration um den Stage-Typenkatalog, sobald deren bestehender Konfigurationsort identifiziert ist; keine neue parallele Deployment-Quelle.

**Interfaces:** Consumes alle vorigen Tasks. Seeds mit festen IDs `7c354d9c-6b16-4f06-9f15-000000000001` bis `...0003` für Discord/Shop/Turniere, alle `enabled: true`, nur `lobby`; bestehende vier Texte bytegenau aus den geprüften `messages*.properties` übernehmen, Adressen aus `LobbyTitleBar.Entry`. Verwendet `GROUNDS_ANNOUNCEMENTS_OWNER` und ausschließlich nachweislich geeignete `HUD_FONT_PACK_SHA1S`; kein Kopieren beliebiger Chat-Hashes.

- [ ] **Step 1 — Failing tests:** `seed_only_missing_document` unterscheidet fehlend/leere Liste/vorhandenen Inhalt/409-Rennen; `ownership_matrix` erwartet nur Legacy bei `legacy`, keine bei `disabled`, nur Proxy bei `proxy`, keine bei ungültigem Wert. Topologietest prüft beide Proxyvarianten, exakt dieselbe Stage-Konfiguration, gesonderte HUD-Allowlist und keinen automatischen Production-Patch. Tests sichern, dass Legacy nur ihre eigene Bar entfernt und vorhandene Spiel-Bossbars unangetastet bleiben.
- [ ] **Step 2 — Red:** `npm test -- tests/networkAnnouncementSeed.test.ts` in Forge; `./gradlew :minestom:test --tests '*AnnouncementOwnershipTest' --tests '*LobbyTitleBarTest'` in Lobby; `python3 -m unittest discover -s tests -p test_stage_announcements.py` in Deploy. FAIL wegen fehlender Funktion/Schalterverdrahtung.
- [ ] **Step 3 — Implementation:** Baue Seed und Ownership-Guards; bereite immutable Artefaktversionen, alle benötigten Abhängigkeits-Pins und den Stage-Runbook vor. Prüfe Registry-Provider und HUD-Allowlist in beiden Proxyvarianten. Breiten-/Schema-Fixtures mit SHA-256 über alle drei Verbraucher vergleichen. Commits/Builds allein dürfen den Eigentümer nicht aktivieren.
- [ ] **Step 4 — Green / lokale Freigabe:** obige Befehle plus alle relevanten Tests der vorherigen Tasks PASS; Paketgraphen zeigen verfügbare, kompatible veröffentlichbare Artefakte. Rollout-Konfiguration bleibt zunächst ohne aktive Proxy-Anzeige. Native Client-Probe und Stage-Schritte bleiben im Runbook als separate, tatsächlich nachzuweisende Aktivierungsbedingungen markiert, falls noch nicht ausgeführt.
- [ ] **Step 5 — Commit:** pro Repository nur Task-Dateien: `feat(lobby): make announcement ownership explicit`, `feat(network): seed legacy announcements without overwriting edits`, `docs(deploy): prepare staged announcement migration`.

**Runbook bei ausdrücklich beauftragter Stage-Aktivierung:**

1. API/Portal und neue Bibliotheken/Plugins bereitstellen, `legacy` beibehalten. Mindestens zwei Proxies und zwei Lobbys nachweisen; Rollen-Query, Pack-Capability und Konfigurationszugriff an jeder Instanz prüfen.
2. Seed einmal für Stage ausführen. Bestehender leerer/edierter Stand bleibt unverändert. Gespeichertes Dokument samt Version lesen und sichern.
3. Alle Lobbys mit neuer Ownership-Unterstützung auf `disabled` ausrollen. Alte Pods vollständig beenden; bestehende Clients müssen die Legacy-Bar entfernt haben. Noch kein Proxy darf anzeigen. Ein Backend in anderer Region zählt ebenfalls.
4. Erst nach diesem Nachweis beide Proxyvarianten auf `proxy` umstellen. Neue Verbindungen sowie Spieler, die auf bereits laufenden Lobbys bleiben, müssen die richtige Anzeige erhalten. Der Lobby-Wert kann für den Betrieb `disabled` bleiben; es gibt keine zentrale Live-Schalter-Abhängigkeit.
5. Zwei kurze Meldungen nebeneinander, drei Meldungen mit Rotation, alle vier Sprachen, schmale Fenster und GUI-Skalierung prüfen. Lobby → `game` entfernt die Standardmeldungen; testweise für `game` aktivierte Meldung erscheint nur dort. Beim Proxywechsel darf weder eine zweite Meldungsbar noch ein alter Text stehen bleiben.
6. Einen Proxy neu starten und ein NATS-Ereignis auslassen: nach spätestens `15 s` plus HTTP-Deadline stimmt der Stand wieder. API-Ausfall erhält den letzten gültigen Stand; explizit leeres Dokument entfernt alle eigenen Anzeigen. Pack ersetzen/ablehnen: keine unbestätigten HUD-Zeichen.
7. Rollback sequenziell: erst alle Proxy-Anzeigen ausblenden/Proxies auf `disabled` ausrollen, anschließend Lobbys auf `legacy`. Die Dokumentdaten bleiben erhalten. Niemals Legacy aktivieren, solange ein Proxy weiterhin Eigentümer ist.
8. Nachweise mit Revisionen, Pack-Hashes, sichtbaren Ergebnissen und offenen Grenzen ablegen. Eine spätere Produktionsaktivierung benötigt einen eigenen Auftrag; für Portal den gewünschten Cloudflare-Worker direkt Live aktivieren.

## Selbstprüfung und Abschlussbedingungen

- Design-Ziele sind Tasks zugeordnet: gemeinsame Fläche/Geometrie → 1; zentrale Pflege/Rechte/CAS/Katalog → 2; Pack-/Servertypnachweis → 3; Multi-Proxy/Rotation/Lifecycle/Reconciliation → 4; Editor/Übersetzungen/Vorschau → 5; Migration/Seeds/Integration/Rollback → 6.
- Alle fünf Review-Focus-Punkte besitzen oben konkrete Tests. API-Namen, Dokumentnamen, Owner-Werte und Layoutkonstanten bleiben über Aufgaben hinweg identisch.
- Keine Tests oder nativen Nachweise wurden durch das Schreiben dieses Plans ausgeführt. Vor einer späteren Erfolgsmeldung sind die tatsächlichen Ergebnisse getrennt von den hier beschriebenen erwarteten Ergebnissen anzugeben.
- Für Ausführung empfiehlt der Autor die Umsetzung durch den primären Agenten in Abhängigkeitsreihenfolge: Der Umfang ist über mehrere Repositories eng gekoppelt; Architektur, Integrationsentscheidungen und abschließende Korrektheitsprüfung bleiben beim primären Agenten. Delegation erfolgt nur nach gewählter Ausführungsmethode; die gewünschte Implementierungs-Subagent-Vorgabe `gpt-5.6-terra` ist in dieser Sitzung nicht als Modell verfügbar und wird nicht stillschweigend ersetzt.
