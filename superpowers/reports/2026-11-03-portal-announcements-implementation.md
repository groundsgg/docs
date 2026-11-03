---
title: "Portal-Meldungen: Umsetzung und Prüfung"
description: "Implementierung, Prüfbelege, aktive Stage-Artefakte und direkter Portal-Worker-Rollout."
---

# Portal-Meldungen: Umsetzung und Prüfung

Stand: 3. November 2026, nach dem beauftragten Deployment. Die Funktion ist auf Stage und im Portal aktiv; der aktuelle Aktivierungsstand steht unten. Die Commit- und Prüftabellen dokumentieren den ursprünglichen lokalen Implementierungsstand vor dem Rollout.

## Ergebnis

- Portal-Tab **Announcements** in der bestehenden Netzwerkverwaltung: Erstellen, Bearbeiten, Aktivieren, Sortieren, Löschen und vier Sprachfassungen.
- Auswahl tatsächlicher Backend-Rollen **pro Meldung**; neue Entwürfe bleiben inaktiv und schlagen ausschließlich `lobby` vor. Aktuell deklarierte Stage-Rollen: `lobby`, `game`, `match`; einzelne Spielmodi sind keine eigenen Rollen.
- Eine gemeinsame HUD-Fläche für Text und optional goldene Adresse; höchstens zwei Meldungen pro Gruppe, zehn Sekunden pro Gruppe. Gemeinsame native Glyphenmetriken für JVM, API und Vorschau.
- Proxy-Anzeige mit expliziter HUD-Packbestätigung, Sprach-Fallback, Serverwechsel-Sperren, getrennten lokalen Spielern pro Proxy und zentralem, versioniertem Konfigurationsdokument.
- Autoritative Namespace-Snapshots und Polling reparieren verpasste NATS-Hinweise. Fehlerhafte Antworten erhalten den letzten gültigen Stand; ein wirklich fehlender oder leerer Inhalt entfernt die eigene Anzeige.
- Idempotenter Seed übernimmt die drei vorhandenen Hinweise und überschreibt weder leere noch bearbeitete Dokumente. Ownership-Guards und Runbook ermöglichen eine sequenzielle Ablösung der Lobby-Anzeige.

## Ursprüngliche Implementierungscommits und Arbeitskopien

| Bereich | Ursprünglicher Commit | Arbeitskopie |
| --- | --- | --- |
| hud | `c1a9294` | `/home/lukas/grounds/.worktrees/scoreboard-library-minigame-api` |
| proxy | `938c2d7` | `/home/lukas/grounds/.worktrees/scoreboard-plugin-proxy` |
| packs | `5b350a3` | `/home/lukas/grounds/.worktrees/scoreboard-plugin-resourcepacks` |
| forge | `e019168` | `/home/lukas/grounds/.worktrees/grounds-forge-motd-library` |
| portal | `2dcb0ea` | `/home/lukas/grounds/.worktrees/grounds-portal-contextual-motd` |
| lobby | `58c24f6` | `/home/lukas/grounds/.worktrees/queue-scene-plugin-lobby` |
| deploy | `fd1a2b1` | `/home/lukas/grounds/.worktrees/scoreboard-deploy` |
| pulumi | `9388d3a` | `/home/lukas/grounds/.worktrees/grounds-pulumi-forge-4.14.0` |

## Ursprüngliche lokale Verifikation

| Bereich | Tatsächlich ausgeführter Nachweis | Ergebnis |
| --- | --- | --- |
| HUD | Neue `HudPillsTest` (3) und vorhandene `GroundsBossBarTest` (7) | PASS |
| Proxy/API | Vollständige `:api:test :velocity:test` und `:velocity:shadowJar`, 229 Tests | PASS |
| Resourcepacks | 82 gezielte Capability-/Lifecycle-/Coordinator-Tests, einschließlich Wiederverbindungs-Race | PASS |
| Lobby | 6 Ownership-/Titelbar-Tests | PASS |
| Forge | 72 Netzwerk-, Konfigurations-, Seed- und Chart-Tests; `tsc --noEmit` | PASS |
| Portal | 33 Modell-/Hook-/Editor-/Netzwerk-Tests; TypeScript; ESLint auf den geänderten Produktdateien | PASS |
| Portal-Build | Optimierter Webpack-Build einschließlich TypeScript, statischer Seiten und `cf-postbuild` | PASS |
| Browser | Chromium: Erstellen → Übersetzung/Adresse → Speichern → Neuladen; zusätzlich gegen den Produktionsbuild | PASS |
| Deployment | Zwei Stage-Topologietests, Pulumi-Katalogregression und HUD-Release-Metadatentest | PASS |
| Helm | Tatsächlich gerenderte API-Deployment-Umgebungsvariablen behalten explizite leere Stage-/Production-Listen `[]` | PASS |
| Artefakt | Proxy-JAR enthält HUD-Metriken, keine Minestom-Klassen und genau eine `ProxyServiceRegistry` | PASS |
| Datenvertrag | Drei exportierte Contract-Kopien sind bytegleich zur HUD-Quelle und ihre SHA-256-Werte stimmen | PASS |

Die JVM-Prüfungen verwendeten lokale Gradle-Composite-Substitutionen für die noch unveröffentlichten HUD-/Proxy-API-Artefakte. Die Produktdateien enthalten keine lokalen absoluten Abhängigkeitspfade. Der Portal-Build nutzte echte bereits gecachte Geist-Dateien über Nexts Font-Testadapter, ohne Google-Abfrage und ohne Sentry-Upload. Der zusätzliche Produktions-Browserlauf verwendete das fertige Build-Ergebnis.

Belege dieser Sitzung: `/tmp/announcements-{hud,proxy,packs,lobby}-verified.log`, `/tmp/announcements-forge-reviewed-final.log`, `/tmp/announcements-portal-final.log`, `/tmp/announcements-portal-hermetic-build.log`, `/tmp/announcements-portal-production-e2e.log`, `/tmp/announcements-empty-catalog-render.yaml`. Temporäre Testserver-Konfigurationen wurden aus der Arbeitskopie entfernt. Screenshot: `/home/lukas/grounds/.task-evidence/portal-announcements/editor.png`.

## Unabhängiges Review und Korrekturen

Ein frischer, ausschließlich lesender Review fand sieben wesentliche Befunde. Alle wurden mit reproduzierenden Tests korrigiert:

1. Pre-Connect sperrt nun die Anzeige über den Verbindungsversuch hinweg; Ticks, Snapshots und andere Spieler können die alte Lobby-Bar nicht wieder erzeugen. Eine spätere Ablehnung eines weiteren Versuchs hebt eine bereits bestehende Sperre nicht auf. Fehlerfreigabe benötigt die bestätigte alte Verbindung und einen Notify-Fall.
2. Ein vorhandener `announcements/active`-Eintrag ohne erforderliches String-`contentJson` ist ein Fehler. Er wird nicht als Löschung übernommen und erhöht nicht die lokal angewendete Revision.
3. Der alte Disconnect entfernt HUD-Zustand innerhalb derselben geprüften Session-Sperre. Eine dazwischen neu verbundene Spielerinstanz verliert ihre Packbestätigung nicht.
4. Das 65.536-Byte-Dokumentlimit prüft die kanonische Darstellung. Zusätzliche PostgreSQL-JSONB-Leerzeichen machen ein gültiges gespeichertes Dokument nicht ungültig.
5. Der Proxy liest den bestehenden Namespace-Snapshot für `announcements`. Große fremde MOTD-/Wartungsdokumente können deshalb das Aktualisieren oder Löschen einer Meldung nicht blockieren.
6. `hud` gehört zu Paketliste, Linked-Versions-Gruppe, Release-Manifest und CI-Matrix. Ein regulärer Minestom-Release kann dadurch seine neue HUD-Abhängigkeit mit derselben Version erhalten.
7. Der Stage-Katalog ist an der vorhandenen Pulumi-Helm-Konfiguration hinterlegt. Helm setzt nur bei fehlendem Schlüssel Lobby voraus und respektiert explizite leere Listen.

Es wurden keine kleineren Review-Befunde zurückgestellt. Die Review-Ausnahmen für native Darstellung und echte Stage-Zustände bleiben Aktivierungsvoraussetzungen; der zunächst nicht abgeschlossene Portal-Build wurde anschließend erfolgreich separat verifiziert.

## Getroffene Entscheidungen und praktische Grenzen

| Entscheidung in Reihenfolge der Arbeit | Grund | Grenze oder Kosten, falls die Annahme nicht trägt |
| --- | --- | --- |
| Bestehende saubere Worktrees auf eigenen Feature-Branches wiederverwenden; später einen weiteren für Pulumi ergänzen | Bestehende Arbeit und ursprüngliche Branches erhalten | Acht getrennte Repositories müssen koordiniert integriert werden |
| Neue Meldungen mit tatsächlichen Zielclient-Metriken messen; Legacy-Segment-API kompatibel lassen | Bestehende Spielanzeigen nicht umformatieren | Native GUI-Prüfung bleibt erforderlich; Legacy-Fallback bleibt unverändert |
| Unveröffentlichte Java-Abhängigkeiten lokal über Composite-Build prüfen | Keine unautorisierte Paketveröffentlichung oder absolute Produktpfade | Reguläre Remote-Builds brauchen zuerst verfügbare Paketversionen |
| Verbindungsspezifischen Viewer-Adapter für die Controller-Logik nutzen | Deterministische Lebenszyklus- und Multi-Proxy-Tests | Simulation ersetzt keinen echten regionalen Client-/Proxy-Test |
| Koordinierte Branch-Koordinate `feat-portal-announcements-SNAPSHOT` verwenden | Entspricht dem vorhandenen Branch-Publisher | Vor Auslieferung durch tatsächlich geprüfte Release-Koordinaten ersetzen |
| Keine Image-Digests oder HUD-Pack-Hashes erfinden; Legacy bleibt vorbereitet aktiv | Nur nachgewiesene Assets freigeben | Neue Anzeige bleibt bis zum expliziten Rollout aus |
| Gradle-Verbraucher mit gemeinsamem API-Ausgabeverzeichnis nacheinander prüfen | Verhindert konkurrierend überschriebene Klassendateien | Etwas längere lokale Prüfzeit |
| Native und echte Stage-Proben als gesonderte Aktivierungsgates behandeln | Keine simulierten Tests als Live-Nachweis ausgeben | Derzeit kein Nachweis des tatsächlich deployten Systems |
| Optimierten Portal-Build mit realen gecachten Fonts prüfen | Externe Font-Abfrage und Upload von der Codeprüfung trennen | Erreichbarkeit von Google Fonts wurde damit nicht geprüft |
| Bei mehrdeutigen überlappenden fehlgeschlagenen Wechseln die Anzeige gesperrt lassen | Das öffentliche Failure-Event trägt keine eindeutige Attempt-ID; falsche Backend-Anzeige vermeiden | In diesem seltenen Fall können Hinweise bis zur positiven Auflösung oder erneuten Verbindung ausbleiben |

## Aktueller Aktivierungsstand am 3. November 2026

Die Angaben oben dokumentieren den ursprünglichen lokalen Prüfstand. Anschließend hat der Nutzer das Deployment und die Integration beauftragt.

- Das Portal nutzt die direkt als Live aktivierte Cloudflare-Worker-Version `6ba7f036-4de4-4748-8fc7-c06489dd2cb5` aus Commit `9e9affce8aa188a95df07c78dba9ec9b97799073`. Die bestehenden 34 Bindings und Runtime-Einstellungen wurden beibehalten. Es gab keinen Portal-Releaseprozess.
- Die zwei Java- und der Bedrock-Stage-Proxy laden denselben Proxy-JAR mit SHA-256 `98af26660f57d52f0d70dcbf5de0449ad023abd2f888b293447f703389946f0c`. Die Prüfung des tatsächlich geladenen Artefakts bestätigt die Meldungs-Runs auf allen drei Proxies. Der HUD bleibt ohne bestätigte Pack-Capability gesperrt; der Bedrock-Client erhält dadurch keine unbestätigte Java-HUD-Anzeige.
- Jede Meldung verwendet eine gemeinsame Fläche, warmweißen Text, den Sand-Punkt `•` und eine goldene, fette Adresse. Ohne Adresse entfällt der Punkt. Zwischen Text, Punkt und Adresse steht jeweils ein gewöhnliches Leerzeichen.
- Der Bitmap-Punkt wurde am Minecraft-26.3-Client gemessen: `•` hat 2×2 sichtbare Pixel und Advance 3, der vorherige `·` 1×1 Pixel und Advance 2. Portal, Forge, Proxy und Pack verwenden denselben Metrikvertrag.
- Das Stage-Dokument enthält drei Meldungen, jeweils auf `lobby` begrenzt. Der bestehende NPC-Lobby-Teststand bleibt erhalten.
- Der Nutzer hat Merge und bedingte Releases beauftragt. Wegen der gleichzeitig laufenden Arbeiten an Reporting, Punishment und Nachrichtenlöschung werden hier ausschließlich die Feature-PRs integriert. Es werden keine zusätzlichen Tags, GitHub-Releases oder Release-Workflows gestartet.

Die serverseitige Prüfung ersetzt keine vollständige native Client-Matrix für alle vier Sprachen, Fenstergrößen und GUI-Skalierungen. Eine Aktivierung des Proxy-HUD in der Spielproduktion ist separat zu behandeln.

Die Arbeitskopien bleiben erhalten, da andere Chats sie verwenden. Unabhängige Dokumentationsänderungen aus deren Branches gehören nicht zu dieser Integration.
