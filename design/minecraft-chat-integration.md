---
title: "Minecraft Chat: Plugin-Integration"
description: "Gemeinsame i18n-Layouts, geprüfte Plugin-Migrationen und Veröffentlichungsreihenfolge."
---

## Zuständigkeiten

`library-i18n` besitzt Systemlayouts, Kontext- und Levelbezeichnungen sowie das Spielerlayout. Jedes Plugin besitzt seine fachlichen Übersetzungskeys. `plugin-chat/common` erzeugt sichere Absender-, Kopf- und Emoji-Komponenten. `plugin-resourcepacks` meldet tatsächliche Clientfähigkeiten über die gemeinsame Proxy-API. Die Mini-CD legt die [Gestaltungsregeln](/design/minecraft-mini-cd-and-chat) fest.

Layout und Wortlaut werden in `messages*.properties` gepflegt. Code darf keine sprachlichen Präfixe oder feste Reihenfolge sprachlicher Komponenten herstellen. Farben und Clientprüfung bleiben im Renderer. Richtext-Platzhalter erhalten explizite Komponenten; freie Strings bleiben unparsed.

## Kontext und Level

`SystemCategory` beschreibt ausschließlich den Kontext: `GENERAL`, `FRIENDS`, `PARTY`, `MATCH`, `STORY`. `SystemLevel` beschreibt unabhängig davon `INFO`, `SUCCESS`, `WARNING`, `ERROR`. `SystemMessageKey` bietet beide Felder mit den Standards `GENERAL` und `INFO`; bestehende einfache `MessageKey`-Implementierungen bleiben gültig.

Eine Freundeslisten-Ablehnung verwendet beispielsweise `FRIENDS` mit `ERROR`, ein erfolgreicher Partybeitritt `PARTY` mit `SUCCESS`. Der Kontextpräfix behält seine feste Modulfarbe. `SystemMessageColors` bestimmt die Grundfarbe des Nachrichtentextes nach Level: INFO `#F3F0EA`, SUCCESS `#A4CE87`, WARNING `#F0C36A`, ERROR `#F28B82`. Alte Wurzelfarben werden dabei ersetzt; explizite untergeordnete Hervorhebungen, Schriften, Klick- und Hoveraktionen bleiben erhalten. Logo und Köpfe bleiben weiß, Trenner sekundär. `<category>` und das optionale `<level>` werden unabhängig lokalisiert und können in den i18n-Layouts platziert werden. Die vollständige Kontextpalette steht im [Mini-CD](/design/minecraft-mini-cd-and-chat). Der Ausgabeweg entscheidet weiterhin, ob die Nachricht vom System oder einem Spieler stammt; `render` und unpräfixierte Unterhaltung behalten ihre eigenen Farben.

Alle Plugin-Keys und direkte `SystemMessages.format`-Aufrufe werden mit diesen zwei unabhängigen Feldern gepflegt. Fachliche Zuordnungen sind explizit; es gibt keine Laufzeitheuristik anhand von Keynamen oder Farben.

## Repository-Abgleich

GitHub-Abgleich vom 17. Oktober 2026: 107 sichtbare Repositories. Die Hauptstände von 47 für Minecraft-Ausgaben relevanten Repositories wurden frisch geladen und geprüft, einschließlich neuer Spielmodi, Bibliotheken und Beispiele. Die dafür fehlenden Checkouts sind jetzt lokal vorhanden. Bestehende Änderungen in den Hauptcheckouts bleiben erhalten; Anpassungen erfolgen in isolierten Worktrees auf den geprüften aktuellen Commits.

Zusätzlich umfasst der Audit GetDown, Keystone, BedWars, SurvivalGames und Duel sowie die Minigame-API, Recorder, Anti-Cheat und Demo-Repositories. Die beiden leeren Abstandzeilen von SurvivalGames sind bewusst keine Systemmeldungen. Ergänzend wurden der Fabric-Client Grounds Connect sowie `library-resource-pack` und `library-jvm-modules` geprüft; dort gibt es keine eigene Chat-Sendeausgabe.

## Gemeinsame Kategorie-Darstellung

Verbindlich bleibt `[Logo] · Kategorie | Nachricht`; ohne bestätigtes Chatpack erscheint `[Grounds] Kategorie | Nachricht`. Das gilt auch für benannte Kontexte wie `GetDown`, `Chat`, `Lobby` und `Settings`. Eine Kategorie allein mit `›` ist kein alternatives Systemdesign. Spieler-, Privat- und Staffnachrichten behalten ihren eigenen Gesprächsaufbau.

[Der gemeinsame Kategorie-Renderer](https://github.com/groundsgg/library-i18n/pull/18) ist als `library-i18n:0.4.3` [erfolgreich veröffentlicht](https://github.com/groundsgg/library-i18n/actions/runs/39027248865). `Prefix.category` verwendet damit dieselbe zentrale Systemvorlage. Kontext, Schweregrad und vorhandene Inhaltsstile bleiben getrennt; bekannte Kategorien werden lokalisiert, eigene Spielnamen bleiben erhalten. Die Prüfung umfasst 49 erfolgreiche Bibliothekstests und einen unabhängigen Code-Review ohne wesentliche Befunde.

Empfängerabhängige Ausgabe bindet `Translations.forBundleWithSystemRenderer` ein. Minestom liest ausschließlich den bestätigten Spieler-Tag `grounds:chat-font-ready`; der Proxy fragt die aktuelle `PlayerChatAppearanceQuery` ab. Ein Locale allein genügt nicht zur Logo-Freigabe. Deshalb bleiben Social-Nachrichten beim Transport semantische Komponenten und werden erst bei lokaler oder entfernter Zustellung für den tatsächlichen Leser gerendert. `render` und `sendUnprefixed` erhalten keinen Systempräfix.

Die abhängigen Plugin- und Spielupdates werden eigenständig gegen das veröffentlichte Paket gebaut. Die Veröffentlichung der Bibliothek allein belegt noch keine abgeschlossene Ausrollung der Verbraucher oder aktivierte Grafiken auf einem Liveserver.

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

## Offene Integrationsgates

- GitHub verlangt eine unabhängige Review für [Agones 87](https://github.com/groundsgg/plugin-agones/pull/87), [Player 49](https://github.com/groundsgg/plugin-player/pull/49) und [Docs 51](https://github.com/groundsgg/docs/pull/51).
- [Social 23](https://github.com/groundsgg/plugin-social/pull/23) besteht regulären Build und Docker-Prüfung. Seine zusätzliche Vorschau scheitert vor dem Signieren an fehlendem `envsubst`. Die Korrektur in [.github 34](https://github.com/groundsgg/.github/pull/34) besteht Workflow-Vertrag und Buildx-Smokecheck und wartet auf die vorgeschriebene Review.
- [Notifications 18](https://github.com/groundsgg/plugin-notifications/pull/18) besteht den eigenständigen lokalen Build einschließlich der drei Artefaktverträge. Die GitHub-CI kann den privaten Minestom-Fork noch nicht lesen. Die Maven-Quelle und die Weitergabe von `PACKAGES_TOKEN` sind ergänzt; der CI-Lesetoken benötigt Zugriff auf das private Minestom-Paket und muss diesem Repository zur Verfügung stehen. Build-Lesetoken und Veröffentlichungs-Token bleiben getrennt.
- [Containers 287](https://github.com/groundsgg/containers/pull/287) ist nach ausdrücklicher Freigabe möglicher produktiver Image-Auswirkungen gemergt. [Image-Veröffentlichung](https://github.com/groundsgg/containers/actions/runs/38652505129) und CI sind erfolgreich. Feste Tags: `ghcr.io/groundsgg/velocity:sha-e291dc4` und `ghcr.io/groundsgg/paper:sha-e291dc4`. Die Agones-Integration wartet weiterhin auf deren Pflichtreview.
- [Packprodukt 45](https://github.com/groundsgg/resourcepacks/pull/45) wartet auf die Sichtprüfung im Java-26.3-Client. Es wurde weder der Edge-/Stable-Kanal umgestellt noch die Font-Allowlist aktiviert.

Die [Lobby-Integration 146](https://github.com/groundsgg/minestom-lobby/pull/146) verwendet die veröffentlichten Versionen Chat 2.3.0, Lobby 2.10.0 und Permissions 1.1.0. Eigenständiger Build, Formatierung und alle 104 Host-Tests sind erfolgreich; das ausführbare Gesamt-JAR enthält alle fünf gewählten Provider. Die Abhängigkeiten lösen Minestom 2026.10.07-26.3-grounds, Runtime API 0.8.0, i18n 0.4.0 sowie Adventure und MiniMessage 5.2.0 auf. Der PR bleibt im Entwurf, bis Notifications das CI-Zugriffsgate bestanden hat, veröffentlicht und mit seiner tatsächlichen neuen Version eingebunden ist.

## Veröffentlichte Versionen

| Repository | Version | Veröffentlichung |
| --- | --- | --- |
| library-i18n | 0.4.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/library-i18n/actions/runs/38645852984) |
| plugin-proxy | 2.6.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/plugin-proxy/actions/runs/38647306625) |
| plugin-scene-editor | 0.6.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/plugin-scene-editor/actions/runs/38647063044) |
| plugin-grounds-runtime | 0.2.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/plugin-grounds-runtime/actions/runs/38647486640) |
| plugin-coins | 0.3.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/plugin-coins/actions/runs/38649875652) |
| plugin-lobby | 2.10.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/plugin-lobby/actions/runs/38650290901) |
| plugin-permissions | 1.1.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/plugin-permissions/actions/runs/38649507762) |
| plugin-match | 0.11.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/plugin-match/actions/runs/38650302344) |
| plugin-resourcepacks | 1.1.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/plugin-resourcepacks/actions/runs/38649667471) |
| plugin-chat | 2.3.0 | [Erfolgreicher Release-Lauf](https://github.com/groundsgg/plugin-chat/actions/runs/38650751397) |

## Lokale Java-26.3-Sichtprüfung

Für den ersten Clienttest ist ein separates Testpaket unter `artifacts/chat-test-2026-10-15` vorbereitet. Es startet das geprüfte Lobby-JAR und eine lokale Testhilfe mit den echten Produktionsrenderern sowie den originalen Privat-/Teamübersetzungen. Der Server ist unter `localhost:25566` erreichbar; das Vorschau-Pack wird direkt angeboten. Start unter Windows:

```powershell
wsl.exe --distribution Ubuntu --cd /home/lukas/grounds/artifacts/chat-test-2026-10-15 /bin/sh ./start.sh
```

Im Client das Pack akzeptieren und `/chatpreview all` ausführen. Einzelprüfungen: `system`, `chat`, `emoji`, `text`, `graphics`, `pack`, `status`, `de`, `en`, `fr`, `es`. Native Köpfe und erfolgreiche Packbereitschaft bleiben unabhängig. Das Testpaket enthält eine Start-/Stoppanleitung, Prüfschritte, Quellstände und Prüfsummen. Die Beispieltexte sind Testdaten; Kontextlabels und Privat-/Teamlayouts stammen aus den produktiven Übersetzungen.

Der HTTP-Packdownload wurde aus Windows mit exakt passender SHA-1 geprüft. Minecraft meldet Protokoll 777 / Version 26.3. Die Testhilfe prüft zusätzlich 80 Kontext-/Level-/Sprachkombinationen und lehnt falsche Sitzungen oder unbekannte Packangebote bei der Fontfreigabe ab. Eine tatsächliche Sichtabnahme durch einen Minecraft-Client wird damit nicht behauptet.

Diese lokale Sichtprüfung ersetzt keine Netzwerk- und Serviceintegration: Social, Coins, Match, Moderationsdurchsetzung, Report-Versand, Geyser und Proxy-/Backend-Packwechsel benötigen die passende Integrationsumgebung. Die Testhilfe ist ausschließlich im lokalen Testpaket enthalten und wird nicht in Produktivserver eingebaut. Die Produktions-Font-Allowlist bleibt unverändert.

## Präfixregel mit Logo

[Die zentrale Layout-Korrektur](https://github.com/groundsgg/library-i18n/pull/12) entfernt die ausgeschriebene Grounds-Kennung ausschließlich aus dem grafischen Systempräfix. Mit geladenem Pack: `[Logo] · Freunde | Nachricht`; im Textfallback weiterhin `[Grounds] Freunde | Nachricht`. Mini-CD 0.7 und die lokale Java-Vorschau verwenden diese Regel. Die Testhilfe lädt dafür die aktualisierte zentrale Vorlage; die beigefügten Integrationsartefakte und bestehenden Produktions-Releases werden dadurch nicht als bereits aktualisierte Veröffentlichungen ausgegeben.

## Unverfälschte Skin-Köpfe

[Chat-Fix 48](https://github.com/groundsgg/plugin-chat/pull/48) setzt die gemeinsame native Kopf-Komponente explizit auf Weiß. Damit färbt die blaue Textfarbe des Privatlabels die Skin nicht mit ein. Die Regression wurde in den tatsächlichen ein- und ausgehenden Privatlayouts und im Teamlayout für EN/DE/FR/ES geprüft; alle 352 Chat-Tests sowie Build und Formatierung sind erfolgreich. Die lokale Testhilfe enthält dieselbe geprüfte `ChatSender`-Klasse. Ihr Selbsttest prüft die effektive Kopffarbe einschließlich der übergeordneten Layoutfarben.

Der grafische Systempräfix behält seinen Trenner: `[Logo] · Freunde | Nachricht`. Logo, Punkt und unverfärbte Köpfe wurden am 15. Oktober 2026 vom Betreiber im Java-Client bestätigt. Anschließend wurde „|“ als Systemtrenner beschlossen; die lokale Vorschau verwendet ihn in Grafik- und Textmodus.

## Senkrechter Systemtrenner

Nach der bestätigten Clientprüfung für Logo, Punkt und Köpfe verwendet [i18n-Fix 14](https://github.com/groundsgg/library-i18n/pull/14) den Trenner `|` für grafische Systemmeldungen und deren Textfallback: `[Logo] · Freunde | Nachricht` beziehungsweise `[Grounds] Freunde | Nachricht`. Spieler-, Privat- und Staffnachrichten behalten `›`. Die geänderten Erwartungen scheiterten zuerst am bisherigen Trenner; anschließend bestanden alle 42 Tests, Build und Formatierung. Die lokale Vorschau prüft beide Systemvarianten in 80 Kontext-/Level-/Sprachkombinationen. Die Paketveröffentlichung dieser neuen Änderung ist separat nachzuweisen.

## Rangfarbe am Namen, Rangbadges in Tabliste und Nametag

`ChatSender` zeigt keinen Rangpräfix und kein Rangbadge. Der Name erhält die konfigurierte Rangfarbe, mit Warmweiß `#F3F0EA` als Fallback. Der native Skin-Kopf bleibt explizit Weiß. Öffentlicher, privater und Staffchat verwenden dieselbe Absenderdarstellung. Die Nachricht bleibt ein separates Component mit ihren freigegebenen Inhaltsformatierungen.

Die bisherigen Kotlin/JVM-Einstiegspunkte und optionalen Nachrichtenfelder bleiben kompatibel. Mitgesendete Rangpräfixe oder Badge-Bezeichnungen erzeugen keine sichtbaren Tags mehr. Die Tabliste behält ihre Rangtags. Native Nametags über dem Spielerkopf verwenden dieselben grafischen Rangbadges wie die Tabliste, gefolgt vom optionalen weißen Bedrock-Logo aus dem Pflichtpack und dem weißen Namen. Die Rangkürzel, Badge-Flächen und die kleine weiße Schrift sind einheitlich; ausgeschriebener Rang und Punkt entfallen. Die Rangpalette wird zentral in den Rollen gepflegt: Administrator Terrakotta `#C9826B`, Developer Salbei `#A3B18A`, User Sand `#C1B9AA`. Ihr Rang-Lookup berücksichtigt auch sichtbare Spieler, deren Verbindung an einem anderen Proxy liegt; die Anzeigedaten werden separat und asynchron gespeichert und erweitern keine lokalen Berechtigungen.

## Permission-Katalog und Inhaltsformatierung

[Chat-PR 55](https://github.com/groundsgg/plugin-chat/pull/55) führt eine senderbezogene Freigabe im gemeinsamen `ChatContent`-Renderer ein. Der Proxy ermittelt die Rechte am Ursprung und trägt sie als optionales `contentPermissions`-Feld in öffentlichen, privaten und Staff-Nachrichten weiter. Ältere Nachrichten ohne dieses Feld erlauben keine Formatierung oder grafischen Aliasse. Moderationsdaten behalten den unveränderten Originaltext. Lokale Paper-Ausgabe prüft den Absender; Minestom fragt den optionalen Runtime-Dienst `Permissions` bei jeder Nachricht ab. Fehlende Dienste erteilen keine Rechte.

| Knoten | Freigabe |
| --- | --- |
| `grounds.chat.format.color` | `<red>`, `<#FFAA00>`, `&6`, `&#FFAA00` |
| `grounds.chat.format.decoration` | Fett, Kursiv, Unterstrichen |
| `grounds.chat.emoji.*` | Alle eigenen Chat-Emojis über den Permission-Resolver |
| `grounds.chat.emoji.heart` | `:heart:` |
| `grounds.chat.emoji.smile` | `:smile:` |
| `grounds.chat.emoji.laugh` | `:laugh:` |
| `grounds.chat.emoji.thumbsup` | `:thumbsup:` |
| `grounds.chat.emoji.gg` | `:gg:` |
| `grounds.chat.emoji.fire` | `:fire:` |
| `grounds.chat.emoji.creeper` | `:creeper:` |
| `grounds.chat.emoji.wrench` | `:wrench:` |

Diese elf neuen Einträge stehen mit Beschreibungen im gemeinsamen `common/src/main/resources/META-INF/grounds/permissions.json`, damit der Permission-Katalog und seine Autovervollständigung sie anbieten. Der Katalog enthält außerdem weiterhin `grounds.chat.staff`. Er ist in common sowie den gebündelten Velocity-/Paper-JARs vorhanden; das schlanke Minestom-Modul erhält ihn über seine common-Abhängigkeit. Vorhandene Rollen werden dadurch nicht automatisch berechtigt. `/emoji` listet nur erlaubte Aliasse auf.

Prüfnachweis: vollständiger Build und Formatierung erfolgreich, 373 Tests ohne Fehler oder übersprungene Tests. Geprüft sind erlaubte und verweigerte Tags, einzelne Emojis, Limit über formatierte Abschnitte, alle Spielerkanäle, unveränderte Moderationsdaten und die Katalogeinträge. Die unabhängige Korrektheits- und Sicherheitsprüfung meldete keine wesentlichen Befunde. Die lokale Vorschau zeigt mit `/chatpreview format` feste Testprofile ohne Änderung realer Berechtigungen; sie ersetzt keinen Test der Rollenvergabe im Permission-Dienst.

## Geprüfte Verbraucher am 17. Oktober 2026

Die folgenden Änderungen verwenden das tatsächlich veröffentlichte `library-i18n:0.4.3`. Jeder Build lief eigenständig einschließlich Formatierungsprüfung. Die Tabelle belegt Quellcode und lokale Tests; sie ist keine Bestätigung einer Live-Ausrollung.

| Repository | Änderung | Tests | Fehler | Bestehende Skips |
| --- | --- | ---: | ---: | ---: |
| plugin-lobby | [PR 97](https://github.com/groundsgg/plugin-lobby/pull/97) | 92 | 0 | 0 |
| plugin-proxy | [PR 81](https://github.com/groundsgg/plugin-proxy/pull/81) | 138 | 0 | 0 |
| plugin-permissions | [PR 40](https://github.com/groundsgg/plugin-permissions/pull/40) | 197 | 0 | 0 |
| plugin-match | [PR 31](https://github.com/groundsgg/plugin-match/pull/31) | 58 | 0 | 0 |
| plugin-coins | [PR 7](https://github.com/groundsgg/plugin-coins/pull/7) | 13 | 0 | 0 |
| plugin-scene-editor | [PR 32](https://github.com/groundsgg/plugin-scene-editor/pull/32) | 177 | 0 | 6 |
| plugin-grounds-runtime | [PR 8](https://github.com/groundsgg/plugin-grounds-runtime/pull/8) | 6 | 0 | 0 |
| plugin-resourcepacks | [PR 33](https://github.com/groundsgg/plugin-resourcepacks/pull/33) | 114 | 0 | 0 |
| game-getdown | [PR 9](https://github.com/groundsgg/game-getdown/pull/9) | 38 | 0 | 0 |
| game-keystone | [PR 9](https://github.com/groundsgg/game-keystone/pull/9) | 59 | 0 | 5 |
| game-bedwars | [PR 52](https://github.com/groundsgg/game-bedwars/pull/52) | 61 | 0 | 1 |
| game-survivalgames | [PR 13](https://github.com/groundsgg/game-survivalgames/pull/13) | 35 | 0 | 0 |
| duel | [PR 38](https://github.com/groundsgg/duel/pull/38) | 34 | 0 | 0 |
| plugin-social | [PR 23](https://github.com/groundsgg/plugin-social/pull/23) | 29 | 0 | 0 |
| plugin-chat | [PR 55](https://github.com/groundsgg/plugin-chat/pull/55) | 389 | 0 | 0 |
| plugin-agones | [PR 87](https://github.com/groundsgg/plugin-agones/pull/87) | 105 | 0 | 0 |
| plugin-player | [PR 49](https://github.com/groundsgg/plugin-player/pull/49) | 60 | 0 | 0 |
| plugin-notifications | [PR 18](https://github.com/groundsgg/plugin-notifications/pull/18) | 46 | 0 | 0 |

Die Bibliothek und diese 18 Verbraucher erfassen zusammen 1700 Testfälle: 1688 erfolgreich, zwölf Skips und keine Fehler. Die unabhängige Prüfung der Bibliothek und Adapter meldet nach Korrektur der Party-Leader-Markierung keine wesentlichen offenen Befunde. Diese Markierung ist zusätzlich in allen vier Social-Sprachen für Leader und normale Mitglieder geprüft. Die zwölf Skips bestehen aus sechs bestehenden Scene-Editor-Skips, fünf Keystone-Livetests ohne lokale Maps und einem nicht aktivierten BedWars-CDN-Livetest (`MAPS_LIVE_TEST`). Sie werden nicht als erfolgreiche Tests gezählt. Alle neu hinzugefügten Chat-Tests wurden ausgeführt.

Die bereits offenen Migrationen für Agones, Player und Notifications verwenden ebenfalls i18n 0.4.3. Player erhält dabei die neueren Jackson-/Bootstrap-Änderungen und die vom Capability-Renderer benötigte Proxy-API 2.6.0. Pflichtreviews werden nicht umgangen; der frühere private Paket-Zugriffsfehler von Notifications ist nach der Aktualisierung in CI erneut zu prüfen. Verbraucher-Releases, ausführbare Host-/Spiel-Images sowie Pack-/Bedrock-Freigabe bleiben getrennte Schritte.
