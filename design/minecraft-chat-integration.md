---
title: "Minecraft Chat: Plugin-Integration"
description: "Gemeinsame i18n-Layouts, geprüfte Plugin-Migrationen und Veröffentlichungsreihenfolge."
---

## Zuständigkeiten

`library-i18n` besitzt Systemlayouts, Kontext- und Levelbezeichnungen sowie das Spielerlayout. Jedes Plugin besitzt seine fachlichen Übersetzungskeys. `plugin-chat/common` erzeugt sichere Absender-, Kopf- und Emoji-Komponenten. `plugin-resourcepacks` meldet tatsächliche Clientfähigkeiten über die gemeinsame Proxy-API. Die Mini-CD legt die [Gestaltungsregeln](/design/minecraft-mini-cd-and-chat) fest.

Layout und Wortlaut werden in `messages*.properties` gepflegt. Code darf keine sprachlichen Präfixe oder feste Reihenfolge sprachlicher Komponenten herstellen. Farben und Clientprüfung bleiben im Renderer. Richtext-Platzhalter erhalten explizite Komponenten; freie Strings bleiben unparsed.

## Kontext und Level

`SystemCategory` beschreibt ausschließlich den Kontext: `GENERAL`, `FRIENDS`, `PARTY`, `MATCH`, `STORY`. `SystemLevel` beschreibt unabhängig davon `INFO`, `SUCCESS`, `WARNING`, `ERROR`. `SystemMessageKey` bietet beide Felder mit den Standards `GENERAL` und `INFO`; bestehende einfache `MessageKey`-Implementierungen bleiben gültig.

Eine Freundeslisten-Ablehnung verwendet beispielsweise `FRIENDS` mit `ERROR`, ein erfolgreicher Partybeitritt `PARTY` mit `SUCCESS`. Das Layout zeigt weiterhin den Kontext. Das Level bestimmt dessen Farbe; Textinhalt, Klickaktionen und explizite Komponentenstile bleiben erhalten. `<category>` und das optionale `<level>` werden unabhängig lokalisiert und können in den i18n-Layouts platziert werden. Der Ausgabeweg entscheidet weiterhin, ob die Nachricht vom System oder einem Spieler stammt.

Alle Plugin-Keys und direkte `SystemMessages.format`-Aufrufe werden mit diesen zwei unabhängigen Feldern gepflegt. Fachliche Zuordnungen sind explizit; es gibt keine Laufzeitheuristik anhand von Keynamen oder Farben.

## Repository-Abgleich

GitHub-Abgleich vom 14. Oktober 2026: 104 sichtbare Repositories, davon 60 unterschiedliche lokale Checkouts. Alle `plugin-*`-Repositories sind lokal vorhanden. Ergänzt wurden fehlende Plugin-Checkouts für Coins, Match, Analytics und Platform Router. Nicht jeder lokale Hauptcheckout war aktuell; die Umstellung arbeitet deshalb in isolierten Worktrees von frisch abgeglichenem `origin/main`.

44 weitere Repositories sind lokal nicht vorhanden. Eine vollständige Kopie sämtlicher Services, Beispiele, Games und Infrastruktur ist für diese Plugin-Migration nicht erforderlich.

<Accordion title="Fehlende lokale Repository-Checkouts">

```text
1vs1-demo
claude-rp-test
cloudflare-prometheus-exporter
disgit-hook
duel
external-dns-bunny-webhook
game-bedwars
grnds-link
grounds-anticheat
grounds-clickhouse-writer
grounds-infra-lib
grounds-instatus-sync
grounds-landing
grounds-pulumi-archive
grounds-recorder
grpc-client
grpc-server
headlamp-upcloud-clusters
keycloak-web
library-data-access
library-jvm-modules
library-minigame-api
mc-stress
menu-demo
minestom
multi-plugin-sample
nats-consumer
nats-producer
nats-recorder
platform-vcluster-template
prism
pvp-demo
sample-leaderboard-plugin
scoop-bucket
service-coins
service-leaderboard
service-match
service-proxy-registry
service-social
valkey-consumer
valkey-publisher
valkey-viewer
valkey-writer
website
```

</Accordion>

## Auditumfang

| Plugin | Angepasste Ausgabe |
| --- | --- |
| Chat | Systemfeedback, Report-Beleg, Global-/Privat-/Stafflayout, Emoji-Hilfe, Reporting-Menütexte |
| Proxy | Online-, Regions- und MOTD-Befehlsantworten |
| Player | Link/Unlink, Sprache, Login-Standardtexte, vertrauenswürdige Edition-Markierung |
| Social | Freunde, Party, Blocklisten und zusammengesetzte Party-Listen |
| Coins | Kontostand und Fehlerantworten |
| Match | Queue, Rejoin, Leaderboard, Assignment und Lobbybefehle |
| Notifications | Feste Texte, dynamische Systemausgabe und lokalisierte Aktionslayouts |
| Permissions | Feste menschliche Antworten und Login-Denial |
| Agones | Befehle, Lobby-Move, Drain-/Restart- und Logintexte |
| Scene Editor | Befehle, Lifecycle und menschliche Fehlerbausteine |
| Grounds Runtime | Menschliche Diagnoseüberschriften für Spieler |
| Resourcepacks | Java-Fontassets, bestätigte Darstellungsfähigkeiten, Lifecycle und lokalisierter Pack-Disconnect |
| Config, Grounds Platform, Analytics, Platform Router | Keine eigene feste Chat-Ausgabe im Audit gefunden |
| Server Discovery | Archivierter Repositorybestand; nicht als aktive zweite Implementierung geändert |

EN, DE, FR und ES bleiben die vorhandenen Sprachvarianten der meisten Plugins. Der Scene Editor besitzt EN und DE; andere Sprachen fallen bewusst auf die Quellsprache zurück. Vor dem Client-Locale-Austausch verwenden Login-Denials die Quellsprache. Explizite Operator-Overrides in `messages.yml` bleiben wörtlich erhalten; neue leere Overrides verwenden die Übersetzungskeys.

Actionbars, Inventartitel und Disconnect-Dialoge verwenden `render` und erhalten keinen Chatpräfix. Maschinenlesbare Diagnosen, beliebige Komponenten aus Transport-APIs, externe Fehlertexte und Konsolenausgaben behalten ihren jeweiligen Vertrag. Dynamische Texte werden nicht nachträglich als MiniMessage geparst.

## Clientzustand

Native Java-Köpfe und die Fontbereitschaft sind unabhängig. Die Proxy-API liefert standardmäßig zwei falsche Flags; eine fehlende oder fehlerhafte Abfrage bleibt lesbarer Text. Floodgate wird anhand seiner API abgefragt, damit verknüpfte Bedrock-Spieler mit einer Mojang-UUID korrekt erkannt werden.

`CHAT_FONT_PACK_SHA1S` ist eine explizite, standardmäßig leere Allowlist der tatsächlich freigegebenen Plattformpack-Hashes. Ein gesendetes oder akzeptiertes Pack schaltet keine Grafiken frei; erst die erfolgreiche Ladebestätigung eines tatsächlich angebotenen erlaubten Packs zählt. Mehrdeutige wiederverwendete UUIDs mit unterschiedlichen Hashes bleiben Text. Identische bereits geladene Clientpacks bleiben beim Backendwechsel gültig. Die bestehende Pack-Deduplizierung bleibt erhalten; allein das Chatdesign löst keinen erneuten Download oder Ressourcen-Reload aus. Ein tatsächlich neues Angebot setzt die Bereitschaft bis zur erfolgreichen Ladebestätigung zurück.

Die Minestom- und Paper-Lokalausgaben werden je Empfängersprache gerendert. Minestom erkennt Java anhand der vertrauenswürdig weitergeleiteten Edition-Markierung und des Protokolls. Der Proxy überträgt den bestätigten Fontstatus über `grounds:chat_style` an die konkrete aktuelle Backendverbindung. Die Minestom-Spielerinstanz speichert ihn unter `Tag.Boolean("grounds:chat-font-ready")`; neue Instanzen beginnen mit `false`, Disconnect und Modulstopp löschen den Zustand. Lokale Chatzeilen, Lobby-Systemmeldungen und Benachrichtigungen lesen denselben Zustand.

Der binäre Vertrag umfasst exakt 26 Bytes in Big Endian: Version `1` als Byte, Ziel-UUID als zwei Longs, eine positive monotone Revision als Long und Bereitschaft `0` oder `1` als Byte. UUID und tatsächlicher Ereignisspieler müssen übereinstimmen. Ältere oder doppelte Revisionen überschreiben keinen neueren Zustand. Ungültige Pakete bleiben ohne Grafik; aufgezeichnete Events einer beendeten Spieler- oder Modulinstanz können keine neue Instanz aktivieren. Der Proxy behandelt eingehende Clientpakete auf diesem Kanal selbst und leitet sie nicht ungeprüft weiter.

Remote-Social-Ausgaben verwenden den empfängerbezogenen `PlayerMessageRenderer` der gemeinsamen Proxy-API. Die Transportdaten bleiben semantische Übersetzungskeys mit Argumenten. Erst die zustellende Instanz entscheidet über Sprache und Logo. Unbekannte oder dekorierte Komponenten bleiben unverändert im bestehenden Ausgabeweg; fehlende oder inkompatible Renderer liefern die ursprüngliche Komponente weiter. Der Provider wird atomar registriert und nur von seinem tatsächlichen Eigentümer entfernt.

## Verifizierter lokaler Stand

Am 14. Oktober 2026 wurden in den betroffenen Modulen insgesamt 1550 Testfälle erfasst: 1544 erfolgreich ausgeführt, 6 bestehende Scene-Editor-Tests übersprungen, keine Fehler. Die relevanten Build- und Formatierungsprüfungen sind erfolgreich. Ein unabhängiger Code-Review prüfte insbesondere Registry-Eigentümerschaft, konkrete Spielersitzungen, Shutdown und konkurrierende Packereignisse.

| Repository / Testumfang | Testfälle | Übersprungen |
| --- | ---: | ---: |
| library-i18n | 42 | 0 |
| plugin-chat | 351 | 0 |
| plugin-proxy | 138 | 0 |
| plugin-player | 49 | 0 |
| plugin-social | 27 | 0 |
| plugin-coins | 13 | 0 |
| plugin-match | 20 | 0 |
| plugin-notifications | 46 | 0 |
| plugin-permissions | 197 | 0 |
| plugin-agones | 105 | 0 |
| plugin-scene-editor | 177 | 6 |
| plugin-lobby | 86 | 0 |
| plugin-grounds-runtime | 6 | 0 |
| plugin-resourcepacks | 114 | 0 |
| resourcepacks: product + catalog | 179 | 0 |

## Veröffentlichung und Abnahme

`library-i18n:0.4.0` und `plugin-proxy-api:2.6.0` sind veröffentlicht. Alle 13 betroffenen Plugin-Builds wurden anschließend eigenständig mit diesen veröffentlichten Artefakten geprüft: ohne Composite-Build oder lokale API-Ersetzung. Dabei wurden fehlende Maven-Quellen ergänzt und die Abhängigkeitssperren des Resourcepack-Providers aktualisiert.

Die Feature- und Release-PRs werden nach erfolgreichen GitHub-Prüfungen zusammengeführt. Vorgeschriebene Reviews bleiben Voraussetzung. Der genaue Veröffentlichungsstand wird über die verlinkten PRs und deren Release-Workflows nachgewiesen; eine grüne lokale Prüfung allein bestätigt keine Veröffentlichung.

1. Bibliothek und Proxy-API veröffentlichen, dann Consumer mit den tatsächlichen Versionen prüfen (erledigt).
2. Grüne Consumer- und Release-PRs integrieren und die veröffentlichten Module im Minestom-Lobby-Host festschreiben.
3. Java 26.3 im echten Client prüfen: Köpfe, Logo, Zeilenumbruch, GUI-/Chatskalierung, Report-Klicks und Packwechsel.
4. Erst nach dieser Abnahme das Java-Pack veröffentlichen und seinen tatsächlichen SHA-1 in `CHAT_FONT_PACK_SHA1S` freigeben.
5. Bedrock-Grafiken und optionale Shader separat umsetzen und abnehmen.

Das Packprodukt bleibt bis zur Clientabnahme offen: Schon sein Merge würde automatisch den Edge-Kanal aktualisieren. Ein isoliertes Vorschau-Pack wurde aus Commit `a8e253686148e6fb27232bde55a6ffeca2de80e9` gebaut. Sein Plattform-SHA-1 `8c1858645d1eda6ddecd9a1021e8a631715bfe74` ist ausschließlich für die lokale Sichtprüfung bestimmt und keine Produktionsfreigabe. Das originale Logo bleibt als 128×128-RGBA-Textur erhalten und wird über den Fontprovider auf acht Pixel Höhe dargestellt.

## Review- und Release-Nachweise

| Repository | Feature-PR |
| --- | --- |
| library-i18n | [Review](https://github.com/groundsgg/library-i18n/pull/10) |
| plugin-proxy | [Review](https://github.com/groundsgg/plugin-proxy/pull/77) |
| resourcepacks | [Review](https://github.com/groundsgg/resourcepacks/pull/45) |
| plugin-agones | [Review](https://github.com/groundsgg/plugin-agones/pull/87) |
| plugin-chat | [Review](https://github.com/groundsgg/plugin-chat/pull/46) |
| plugin-coins | [Review](https://github.com/groundsgg/plugin-coins/pull/5) |
| plugin-grounds-runtime | [Review](https://github.com/groundsgg/plugin-grounds-runtime/pull/6) |
| plugin-lobby | [Review](https://github.com/groundsgg/plugin-lobby/pull/89) |
| plugin-match | [Review](https://github.com/groundsgg/plugin-match/pull/19) |
| plugin-notifications | [Review](https://github.com/groundsgg/plugin-notifications/pull/18) |
| plugin-permissions | [Review](https://github.com/groundsgg/plugin-permissions/pull/36) |
| plugin-player | [Review](https://github.com/groundsgg/plugin-player/pull/49) |
| plugin-resourcepacks | [Review](https://github.com/groundsgg/plugin-resourcepacks/pull/31) |
| plugin-scene-editor | [Review](https://github.com/groundsgg/plugin-scene-editor/pull/30) |
| plugin-social | [Review](https://github.com/groundsgg/plugin-social/pull/23) |

Der Chat-Branch enthält zusätzlich den aktuellen Mute-Enforcement-Stand von `origin/main`. Mute-Prüfungen, Server-Routing und die Reihenfolge von Privatnachricht-Zustellung und Echo bleiben erhalten; neu hinzugekommene Mute-Rückmeldungen sind ebenfalls lokalisiert. Nach dieser Integration wurden alle Chatmodule erneut gebaut und geprüft (351 Testfälle).

Die freigegebene Kontext-/Level-Trennung ist in den verlinkten Feature-PRs umgesetzt und erneut geprüft: 42 Bibliothekstests, alle 13 betroffenen Plugin-Builds erfolgreich; insgesamt 1550 erfasste Testfälle, 1544 erfolgreich und sechs bestehende Scene-Editor-Skips. Der unabhängige Review fand keine verlorenen Metadaten. Veröffentlichungs- und Clientabnahmegrenzen bleiben bestehen.
