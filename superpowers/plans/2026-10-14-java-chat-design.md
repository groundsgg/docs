---
title: "Java-Chatdesign: Umsetzungsplan"
description: "Implementiere das Grounds-Chatdesign für Java 26.3 mit originalem Logo, Spielerköpfen, Emojis und textbasiertem Fallback."
---

# Java-Chatdesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans for native execution, or superpowers:subagent-driven-development if the user selects delegation. Implement task by task; track steps with checkboxes.

**Goal:** Java-Spieler erhalten einen einheitlichen Chat mit dem originalen farbigen G, Spielerköpfen, klar unterscheidbaren Systemmeldungen und einem kleinen Emoji-Set. Bedrock behält zunächst eine lesbare Textdarstellung.

**Architecture:** Verwende die aktuelle gemeinsame ChatLine aus plugin-chat und die zentrale Systemausgabe aus library-i18n. Ergänze empfängerabhängige Darstellungsprofile und die Chatgrafiken im bestehenden Zwei-Pack-Produkt. Formatiere unmittelbar vor der Zustellung; Routing und Moderationsmetadaten bleiben getrennt von der Darstellung.

**Tech Stack:** Kotlin/JVM, Adventure, Grounds-Minestom-Fork 2026.10.02-26.3-grounds.1, Velocity, library-i18n, resourcepacks, JUnit, Gradle.

**Spec:** [Mini-CD v0.5](../../design/minecraft-mini-cd-and-chat.md), [Confluence](https://grounds.atlassian.net/wiki/spaces/gkd/pages/300154881).

## Global Constraints

- Java 26.3 zuerst; keine neue grafische Bedrock-Oberfläche in dieser Umsetzung.
- Originales farbiges G: transparente 128×128-Quelle, Seitenverhältnis bewahren; erste Anzeigehöhe 8 GUI-Pixel, ascent 7; erst nach Clientabnahme ändern.
- Eigener Font grounds:chat; reservierte Glyphen werden nicht in die Standardspielerschrift eingebaut.
- Gold #E7B84B, Text #F2EBDD, Sekundärtext #C1B9AA, Cyan #67D9EC. Erfolg/Warnung/Fehler behalten die Mini-CD-Token. Vorhandene Rank-/Teamzuordnung bleibt erhalten.
- Logo- und Emoji-Komponenten sind weiß eingefärbt und nicht fett/kursiv; ihre Originalfarben bleiben erhalten.
- System: Logo Kategorie › Nachricht. Spieler: Kopf Name › Nachricht. Privat- und Staffkanäle bleiben ausdrücklich bezeichnet.
- Die acht Emoji-Aliase sind :heart:, :smile:, :laugh:, :thumbsup:, :gg:, :fire:, :creeper: und :wrench:. Höchstens fünf grafische Ersetzungen, übrige Aliase bleiben Text.
- Freier Spielertext bleibt unparsed; keine MiniMessage-Auswertung von Nachrichten.
- Der bestehende PackSet besteht aus grounds-content und grounds-platform; aktuelle main-Quelle verwendet Packformat 97.
- Releases erzeugt Release Please. Keine manuell erfundenen Versionen oder Aktivierung von Production.
- Build, Packangebot und Clientabnahme sind getrennte Zustände.

## Ursprünglicher Code und Ausgangsstand

Historischer Ausgangsstand vor der Umsetzung am 14. Oktober 2026; der aktuelle Stand steht im Ausführungsbericht:

- plugin-chat origin/main 05e5a5a: ChatLine ist bereits gemeinsam; private Nachrichten und Staffchat sind lokalisiert. Der lokale Hauptcheckout ist älter und darf nicht als Implementierungsbasis dienen.
- resourcepacks origin/main 0a8a9c6: vorhandene TabFont/Tab-Logo-Integration und gehaltene, hashgeprüfte Artworkquellen verwenden.
- grounds-minestom-runtime origin/main c1480de: Runtime-ServiceRegistry ist die Minestom-Integrationsgrenze.
- library-i18n ist lokal noch nicht als Projektcheckout vorhanden. Zum Lesen liegt origin/main unter /tmp/grounds-chat-i18n-review; die Implementierung benötigt einen dauerhaften Checkout.
- library-i18n Prefix.GROUNDS ist derzeit statisch. Translations.send versieht Systemmeldungen mit dem Präfix; render und sendUnprefixed dürfen weiterhin kein Systempräfix erhalten.
- plugin-resourcepacks ResourcePackStatusListener protokolliert Ladeereignisse; ein öffentliches, fontbezogenes Readiness-Query ist in den geprüften Quellen noch nicht vorhanden.
- Die Docs sind ein eigenes Git-Repository. Vor Änderungen aktuelle Basis prüfen und fremde untracked Dateien erhalten.

## Review Focus

1. Ein Java-Spieler mit einem alten, noch geladenen Pack darf keine unbekannten Chatglyphen erhalten.
2. Bedrock und unbekannte Plattform-/Ladezustände bekommen weder native Java-Köpfe noch Fontglyphen.
3. Cross-Proxy-Sender, PM-Echos und Staffchat dürfen weder Identität noch Nachrichtenrichtung verlieren.
4. Reports müssen die tatsächlich zugestellte Darstellung den richtigen Empfängern und bestehenden Message-IDs zuordnen.
5. Plugin-Classloader dürfen keine zweite ServiceRegistry oder Adventure-Implementierung laden.

## Task 1: Aktuelle Basis und reproduzierbare Chatassets

**Files**
- Create resourcepacks/resourcepacks-product/src/main/kotlin/gg/grounds/resourcepacks/product/ChatFont.kt.
- Modify resourcepacks/resourcepacks-product/src/main/kotlin/gg/grounds/resourcepacks/product/ProductGraph.kt.
- Create resourcepacks/art/platform/chat/logo.png und emoji/*.png.
- Modify resourcepacks/art/platform/ASSET_ORIGINS.json.
- Create resourcepacks/resourcepacks-product/src/test/kotlin/gg/grounds/resourcepacks/product/ChatPackTest.kt.
- Modify resourcepacks/resourcepacks-catalog/src/test/kotlin/gg/grounds/resourcepacks/catalog/PackArtworkLicenseContractTest.kt, falls die Artworkliste erweitert werden muss.

**Interfaces**
- ChatFont.LOGO = 0xE100; Emojis in der oben festgelegten Reihenfolge = 0xE101 bis 0xE108.
- ChatFont.json(): String erzeugt assets/grounds/font/chat.json mit ausschließlich expliziten Bitmapprovidern.
- Texturen liegen im ZIP unter assets/grounds/textures/font/chat/.
- Alle neuen Artworkquellen werden vor Komposition geöffnet, gegen SHA-256/Größe geprüft und über die bestehende Immutable-Source-Pipeline verarbeitet.

- [ ] Ausgangspunkt origin/main verwenden; keine bestehenden fremden Worktrees übernehmen. Gitstatus und Baselineprüfungen dokumentieren.
- [ ] ChatPackTest fordert 128×128-Logo mit Alpha, height=8/ascent=7, eindeutige Glyphen, vorhandene acht Emoji-PNGs, richtige Packreihenfolge und unveränderte minecraft-Fontdateien.
- [ ] Den Test ausführen und sein erwartetes Fehlen der Chatassets nachweisen.
- [ ] Originales farbiges G aus dem Betreiberlogo verwenden. Das Bild wird deterministisch zugeschnitten/eingepasst; keine KI-Neuzeichnung. Bestehende Emojiassets nur mit nachvollziehbarer Herkunft übernehmen, sonst kleine neue Pixelassets erstellen und die Herkunft dokumentieren.
- [ ] ChatFont und gehaltene Artwork-Komposition implementieren. Die vollständige Wortmarke bleibt als größeres Asset für ausgewählte Meldungen vorgesehen; keine Verkleinerung der ganzen Wortmarke auf das quadratische G.
- [ ] :resourcepacks-product:test und :resourcepacks-catalog:test ausführen; zwei Builds mit denselben expliziten Inputs byteweise vergleichen.
- [ ] Änderungen einzeln committen, sobald die Prüfungen bestehen.

## Task 2: Empfängerprofil und zentrale Systemdarstellung

**Files**
- Create library-i18n/src/main/kotlin/gg/grounds/i18n/SystemMessageRenderer.kt.
- Modify library-i18n/src/main/kotlin/gg/grounds/i18n/Translations.kt.
- Modify library-i18n/src/main/kotlin/gg/grounds/i18n/Prefix.kt.
- Create library-i18n/src/test/kotlin/gg/grounds/i18n/SystemMessageRendererTest.kt.
- Create plugin-proxy/api/src/main/kotlin/gg/grounds/proxy/api/PlayerChatAppearanceQuery.kt.
- Modify plugin-resourcepacks/velocity/src/main/kotlin/gg/grounds/resourcepacks/velocity/ResourcePackStatusListener.kt und GroundsResourcePacksPlugin.kt.
- Create plugin-resourcepacks/velocity/src/test/kotlin/gg/grounds/resourcepacks/velocity/ChatAppearanceStateTest.kt.

**Interfaces**
- PlayerChatAppearanceQuery.appearanceOf(playerId: UUID): PlayerChatAppearance.
- PlayerChatAppearance enthält javaHeadsSupported: Boolean und chatFontReady: Boolean; Default ist beides false.
- SystemMessageRenderer.render(audience: Audience, key: MessageKey, message: Component): Component.
- Translations erhält einen optionalen Renderer über einen neuen benannten Factory-Einstieg forBundleWithSystemRenderer; vorhandene forBundle-Signaturen bleiben binär erhalten.
- Der Standardrenderer reproduziert das bestehende Prefix-Verhalten. Ein explizit installierter Minecraft-Renderer erzeugt die neue Markenzeile für geeignete Empfänger.

- [ ] Tests zuerst: render und sendUnprefixed bleiben unverändert; send verwendet den Empfängerrenderer; bestehende Factoryaufrufe und Java-Overloads bleiben kompatibel.
- [ ] Kontext und Level werden über explizite MessageKey-Metadaten bestimmt. Fehler-/Usage-/Reportkeys erhalten geprüfte Statuslevel und behalten ihren fachlichen Kontext; kein Raten aus zufälligen Textfarben.
- [ ] Readiness erst nach erfolgreichem Ladeereignis des exakt angebotenen Platformpacks setzen. Ein bloß gesendetes, abgelehntes, fehlgeschlagenes oder veraltetes Pack setzt chatFontReady nicht.
- [ ] Assetfähigkeit wird zusätzlich gegen die tatsächlich eingeführte Chat-Pack-Version bzw. einen bekannten Manifest-/Packhash geprüft. Eine Auswahl älterer PackSets setzt sie zurück. Die konkreten Releasepins werden nach Veröffentlichung aus den echten Releaseartefakten übernommen; vorher bleibt die Produktivfunktion deaktiviert.
- [ ] Disconnect, Packwechsel und Shutdown löschen den Zustand. Ein verspätetes Statusereignis für den vorherigen Packfingerprint darf ihn nicht wieder aktivieren.
- [ ] Query über die bestehende ProxyServiceRegistry bereitstellen; plugin-proxy-api compileOnly verwenden. Bei fehlendem Provider greift immer der Textfallback.
- [ ] Minestom erhält dieselben booleschen Werte über die vorhandene Backend-Kommunikation als neues versioniertes Capability-Signal; alte Gegenstellen ignorieren es. Sender- und Empfängerzustand werden nicht verwechselt.
- [ ] Gradleprüfungen in library-i18n, plugin-proxy und plugin-resourcepacks ausführen; vorhandene Lade-/Lifecycle-Tests bestehen weiter.

## Task 3: Gemeinsame Spielernachrichten und Emoji-Auflösung

**Files**
- Modify plugin-chat/common/src/main/kotlin/gg/grounds/chat/format/ChatLine.kt.
- Create plugin-chat/common/src/main/kotlin/gg/grounds/chat/format/ChatAppearance.kt.
- Create plugin-chat/common/src/main/kotlin/gg/grounds/chat/format/ChatEmojis.kt.
- Modify plugin-chat/common/src/test/kotlin/gg/grounds/chat/format/ChatLineTest.kt.
- Create plugin-chat/common/src/test/kotlin/gg/grounds/chat/format/ChatEmojisTest.kt.
- Modify plugin-chat/common/build.gradle.kts ausschließlich soweit eine nachgewiesen kompatible ObjectComponent-API dies erfordert.

**Interfaces**
- ChatAppearance(javaHeadsSupported: Boolean, chatFontReady: Boolean), mit TEXT_ONLY.
- ChatEmojis.render(rawText: String, appearance: ChatAppearance): Component.
- ChatLine erhält zusätzliche Overloads mit senderId: UUID, appearance: ChatAppearance und den vorhandenen optionalen Rankinformationen. Bestehende Overloads bleiben erhalten.
- Native Köpfe werden aus der tatsächlichen Sender-UUID erstellt; keine synchronen Skin-HTTP-Aufrufe während einer Nachricht.

- [ ] Mit dependencyInsight die tatsächlich aufgelösten Adventure-APIs und Serializer in Minestom und Velocity vergleichen. Native ObjectComponent-Erzeugung nur mit einer auf beiden Java-Hosts kompatiblen API implementieren; keine Adventure-Schattierung.
- [ ] Tests zuerst für Kopf/kein Kopf, ‹Name › Nachricht›, helle Nachrichtfarbe, vorhandene Rankfarben, ungültige Rankfarbe und unveränderte vom Aufrufer übergebene Component-Stile.
- [ ] Emoji-Tests: genau fünf Ersetzungen, sechster Alias bleibt Text, unbekannte Aliase bleiben unverändert, Fallback enthält keine reservierten Glyphen, Markup wie `<red>` bleibt wörtlicher Text.
- [ ] Ein expliziter Literalfilter verhindert, dass frei eingegebene reservierte Chatglyphen als Systemzeichen erscheinen.
- [ ] Die bisherigen Längenregeln am Eingang erhalten; Surrogat-/Unicode-Grenzen prüfen. Es wird zunächst das feste Alias-Set unterstützt, keine angebliche vollständige Unicode-Emoji-Normalisierung.
- [ ] :common:test und die bestehenden Codec-/Reporting-Tests ausführen.

## Task 4: Java-Ausgabewege, private Nachrichten und /emoji

**Files**
- Modify plugin-chat/velocity/src/main/kotlin/gg/grounds/chat/velocity/handler/GlobalChatHandler.kt.
- Modify plugin-chat/velocity/src/main/kotlin/gg/grounds/chat/velocity/handler/PrivateMessageHandler.kt.
- Modify plugin-chat/velocity/src/main/kotlin/gg/grounds/chat/velocity/handler/StaffChatHandler.kt.
- Modify plugin-chat/minestom/src/main/kotlin/gg/grounds/chat/minestom/listener/ChatListener.kt.
- Modify plugin-chat/minestom/src/main/kotlin/gg/grounds/chat/minestom/GroundsChatMinestom.kt.
- Modify plugin-chat/velocity/src/main/kotlin/gg/grounds/chat/velocity/GroundsChatPlugin.kt.
- Create plugin-chat/velocity/src/main/kotlin/gg/grounds/chat/velocity/command/EmojiCommand.kt.
- Modify die vorhandenen gg/grounds/chat/messages*.properties.
- Extend bestehende Handler-/Listener-/Reportingtests; create EmojiCommandTest.kt.

**Interfaces**
- Jeder Empfänger erhält sein aktuelles Darstellungsprofil unmittelbar vor dem Send.
- Private Nachrichten verwenden weiterhin die vorhandenen Lokalisierungskeys und sichere Component-Placeholder für Kopf, Name und Emojiinhalt.
- /emoji zeigt die acht verfügbaren Aliase; bei chatFontReady zusätzlich die Grafiken. Es ist keine neue klickbare Client-Eingabeleiste.

- [ ] Tests zuerst für zwei Empfänger derselben Nachricht mit unterschiedlicher Grafikfähigkeit, Cross-Proxy-Sender und lokale Minestom-Zustellung.
- [ ] PM-Echo bezeichnet den tatsächlichen Sender; eingehend Name → du, ausgehend du → Name. Privat und Staff sind lokalisiert; Staff bleibt eine Spielernachricht ohne Systemlogo.
- [ ] Reporting speichert pro tatsächlicher Darstellung die richtigen zugestellten Empfänger, Rohtexte und bestehenden Message-IDs. Sichtbarkeits-/Block-/Ratelimitregeln bleiben bestehen.
- [ ] Formatted-Component-Broadcasts behalten ihre Inhalte; fremde Component-Fonts werden nicht pauschal umgeschrieben.
- [ ] /emoji registrieren, Übersetzungen ergänzen und vorhandene Permission-Manifest-Konventionen anwenden.
- [ ] :common:test :velocity:test :minestom:test :paper:test sowie die Formatierungsprüfungen ausführen. Paper erhält die gemeinsame Textgrammatik; seine grafikfähige Abnahme ist kein erster Releaseblocker.

## Task 5: Systemmeldungen im aktiven Java-Lobbyumfang

**Files**
- Modify Bundle-Erstellung in plugin-chat/velocity/GroundsChatPlugin.kt.
- Modify Bundle-Erstellung in plugin-social/velocity und plugin-lobby/minestom an den tatsächlichen forBundle-Aufrufen.
- Modify deren relevante Nachrichten-/Systemausgabetests.
- Modify minestom-lobby/build.gradle.kts erst mit echten veröffentlichten Consumer-Versionen.

**Interfaces**
- Alle adoptierten Bundleinstanzen verwenden dieselbe SystemMessageRenderer-Implementierung und empfängerabhängige Capability-Auflösung.
- Bestehende render-Nutzung für Menüs, Scoreboard, Tab und Lore wird nicht verändert.

- [ ] forBundle-, direkte sendMessage- und sendUnprefixed-Aufrufe in den aktiven Lobby-/Social-/Chatmodulen inventarisieren; legitime Gesprächszeilen von Systemausgaben unterscheiden.
- [ ] Tests zuerst für Chatfehler, Freund-/Partyaktionen und Navigator-/Queueantworten mit und ohne geladenes Chatpack.
- [ ] Kategorien und Formatierung an den gefundenen Systemausgaben übernehmen. Englische, deutsche und bestehende weitere Locale-Bundles konsistent pflegen.
- [ ] Bestehende Menü-/Tab-/Scoreboardtests müssen nachweisen, dass kein Systempräfix auf andere Oberflächen gelangt.
- [ ] Repos einzeln prüfen; Abhängigkeiten nach ihrem Release in den Consumer übernehmen. Kein automatischer Versionssprung nur aufgrund eines geplanten Features.

## Task 6: Dokumentation, Review und reale Java-Abnahme

**Files**
- Update docs/design/minecraft-mini-cd-and-chat.md mit YAML-Frontmatter nach docs/AGENTS.md.
- Create docs/minecraft/chat-design.mdx und passenden Navigationslink in docs/docs.json.
- Update README bzw. Integrationsdocs in plugin-chat, library-i18n und resourcepacks.
- Update die existierende Confluence-Mini-CD-Seite; Sollstand und verifizierten Iststand getrennt halten.

- [ ] Docs erklären Alias-Eingabe, Absenderarten, Packlade-Fallback, die 128×128-Quelle und tatsächliche Anzeigegröße.
- [ ] Fachlichen Gesamtänderungsreview durchführen, insbesondere Registry-Klassenidentität, Threading, Readiness-Races und Reporting.
- [ ] Reviewbare PRs pro betroffenem Repository mit erforderlichen Tests und Abhängigkeitsreihenfolge vorbereiten. Erstellte PRs an diesen Chat anhängen.
- [ ] Ein isoliertes Java-Testpaket erzeugen; kein globales PackSet für einen Bildschirmtest umstellen.
- [ ] Reale Java-26.3-Abnahme bei mehreren GUI-/Chatskalierungen: G-Logo, Kopf, Privat-/Staffzeilen, fünf Emojis, lange Zeilen, helle/dunkle Umgebung, Scrollen, Auswahl/Kopieren und Ladezustandswechsel.
- [ ] Bedrock-Smokecheck nur auf lesbare Textausgabe ohne Java-Objekte/Glyphen. Bedrockgrafiken und zusätzliche Shader-/Flächeneffekte sind ein späterer eigener Ausbauschritt.
- [ ] Gesicherte Testergebnisse dokumentieren. Erst nach Review/Clientabnahme einen konkreten Stage-Aktivierungsschritt vorlegen; Production-Aktivierung benötigt eine eigene Freigabe.

## Ausführungsbericht — 14. Oktober 2026

Die lokale Quellcodeumsetzung ist abgeschlossen. Die ursprünglichen Detailchecklisten oben beschreiben den Planungsstand; für den verifizierten Iststand gilt dieser Bericht.

- [x] Isolierte aktuelle Worktrees und GitHub-Inventar: 104 sichtbare Repositories, 60 lokal, alle `plugin-*` vorhanden.
- [x] Originales 128×128-G und acht reproduzierbare Emoji-Assets im bestehenden Java-Packprodukt integriert.
- [x] Gemeinsame System- und Spielerlayouts einschließlich Trennzeichen und Platzhalterreihenfolge in `library-i18n`; fachliche Texte bleiben in Plugin-Bundles.
- [x] Spieler-, Privat- und Staffausgabe je Empfänger; sichere Komponenten, Emoji-Hilfe und bestehende Reporting-Verträge geprüft.
- [x] Feste Nachrichten aller aktiven Plugins auditiert und gefundene Ausgaben vereinheitlicht. Umfang und bewusste Grenzen stehen in der Integrationsdokumentation.
- [x] Vertrauenswürdige Edition, bestätigter Packzustand, Proxy-/Minestom-Bridge und zustellungsbezogenes Social-Rendering integriert.
- [x] Build-/Formatierungsprüfungen und 1550 Testfälle: 1544 erfolgreich, 6 bestehende Scene-Editor-Skips, keine Fehler. Unabhängiger Review der Integrations- und Lifecycle-Grenzen abgeschlossen.
- [x] Mini-CD und Integration in Docs sowie Confluence dokumentiert. Die Integrationsseite liegt unter `design/minecraft-chat-integration` statt des ursprünglich geplanten zweiten Minecraft-Pfads.
- [x] library-i18n 0.4.0 und plugin-proxy-api 2.6.0 veröffentlicht; echte Consumer-Versionspins aktualisiert und alle 13 Plugin-Builds eigenständig erfolgreich geprüft.
- [x] Änderungen committen und PRs für die betroffenen Repositories erstellen; alle Quellcodeänderungen sind committet und als Entwurfs-PRs veröffentlicht.
- [ ] Reale Java-26.3-Abnahme mit verschiedenen Chat-/GUI-Skalierungen und Bedrock-Text-Smokecheck durchführen.
- [ ] Tatsächlich erzeugten Plattformpack-Hash nach Abnahme freigeben und Stage aktivieren. `CHAT_FONT_PACK_SHA1S` bleibt standardmäßig leer.

Die ersten Tests verwendeten lokale Composite-Builds für die geänderte i18n-Bibliothek und Proxy-API. Anschließend wurden alle 13 Plugin-Builds mit den tatsächlich veröffentlichten Artefakten eigenständig geprüft. Erfolgreiche lokale Prüfung ist keine Veröffentlichung oder Live-Aktivierung. Die konkreten Verträge, Testzahlen und Veröffentlichungsreihenfolge stehen in [Minecraft Chat: Plugin-Integration](../../design/minecraft-chat-integration.md).

## Freigegebene Präzisierung: Kontext und Level

Der Benutzer hat die Trennung ausdrücklich zur Umsetzung freigegeben. `SystemCategory` enthält nur fachliche Kontexte; `SystemLevel` enthält die Statuswerte. Gemeinsame Renderer reichen beide Felder unabhängig weiter, Plugin-Keys behalten ihren fachlichen Kontext auch bei Fehlern. Standards sind `GENERAL` und `INFO`; Labels und optionale Platzierung beider Felder bleiben in i18n. Die Änderungen gehen in die bestehenden Entwurfs-PRs. Neue Tests prüfen alle Kontext-/Level-Kombinationen, Default-Keys, Lokalisierung, Richtext-Verträge sowie fachliche Consumer-Zuordnungen.
