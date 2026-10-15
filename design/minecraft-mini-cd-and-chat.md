---
title: "Grounds — Minecraft Mini-CD & Chatdesign"
description: "Gestaltungsregeln für Minecraft-Chat, Systemmeldungen, Original-Logo und Emojis."
---

# Grounds — Minecraft Mini-CD & Chatdesign

Version 0.10 · Stand: 15. Oktober 2026 · Zielplattform: Java 26.3 / Minestom und Bedrock über Geyser.

Status: Aus der Story abgeleitete Designspezifikation. Die gewählte Richtung „eigenständiger Grounds-Stil“ ist abgestimmt; konkrete Farbwerte, Signet-Ausführung und Effekte sind der daraus entwickelte erste Entwurf. Dieses Dokument beschreibt die Gestaltungsregeln. Die lokale Java-Umsetzung und Plugin-Migration sind im [Integrationsnachweis](/design/minecraft-chat-integration) dokumentiert; Die gemeinsamen APIs sind veröffentlicht; die Plugin-Releases laufen. Die visuelle Clientabnahme steht aus.

## 1. Grundlage und Gestaltungsprinzip

Grounds ist ein Zuhause zwischen den Matches. Die lebendige, optimistische Industriestadt verbindet Handwerk, Maschinenbau, Handel und Forschung. Stein, Kupferleitungen, Brücken, Werkstätten, Kräne und Kraftstationen erklären ihre Funktion. Vaerith ist ein natürlicher Energiespeicher; kontrollierte Nutzung, Kühlung und Wartung sind wesentlich. Die Geschichte ist freiwillig; alle Kernmodi bleiben ohne Story-Fortschritt erreichbar.

Daraus folgt: warm, handwerklich, präzise und einladend. Technik erhält erkennbare Funktionen. Ornamente dienen Orientierung und Wiedererkennung. Wettbewerbsinformationen sind unmittelbar lesbar. Piltover bleibt eine atmosphärische Referenz, keine zu kopierende Markenidentität.

Die visuelle Leitidee lautet „Werkstattschild trifft Stadtjournal“: dunkler Stein als Ruhefläche, warmer heller Text, Gold als Absenderakzent, Kupfer als ergänzender Werkstattakzent und kristallines Cyan für gezielte Energieakzente.

Die Farbe von Vaerith ist in der Story ausdrücklich noch offen. Das aus dem Logo abgeleitete Cyan ist ein UI-Token, keine Festlegung der Mineral-, Kern- oder Weltfarbe. „Mira“, „Stadtjournal“ und weitere Arbeitsnamen werden durch diese Spezifikation nicht zu endgültigen Lore-Namen.

## 2. Farben und Rollen

Die Hexwerte sind Zielwerte für Java und Grafikassets. Für Bedrock-Chat werden die tatsächlich unterstützten Textfarben geprüft und auf die nächstgeeigneten Farben abgebildet; exakte RGB-Gleichheit wird nicht vorausgesetzt. Kanal, Absenderart und Status bleiben zusätzlich als Text erkennbar.

| Token | Zielwert | Rolle | Konservativer Text-Fallback |
| --- | --- | --- | --- |
| stone.deep | #181D20 | Optionale dunkle UI-Flächen | Nativer Client-Hintergrund |
| stone.surface | #272E32 | Optionale abgesetzte Flächen | Nativer Client-Hintergrund |
| text.primary | #F2EBDD | Nachrichten, Namen und wichtige Werte | Weiß |
| text.secondary | #C1B9AA | Ergänzende Angaben | Grau |
| gold.primary | #E7B84B | Systemabsender und Markenakzent | Gold |
| copper.primary | #DDA16F | Ergänzender Werkstatt- und Materialakzent | Gold |
| energy.accent | #67D9EC | Gezielte Interaktionen und Auswahl | Aqua |
| status.success | #A4CE87 | Erfolg | Grün plus „Erfolg“ |
| status.warning | #F0C36A | Zeitkritische Hinweise und Warnung | Gelb plus „Warnung“ |
| status.error | #F28B82 | Fehler und abgelehnte Aktionen | Rot plus „Fehler“ |

Nachrichtentext bleibt standardmäßig text.primary. Statusfarben markieren Kennung und relevante Werte, nicht automatisch den gesamten Absatz. Gedämpfte Farbe ist kein Mittel, um wichtige Informationen zu verstecken. Teamfarben im Match bleiben für Teamzuordnung reserviert und werden nicht von Markenfarben überschrieben.

Kontrast wird am echten Client über hellen, dunklen und unruhigen Spielszenen beurteilt. Hexwerte allein garantieren keine Lesbarkeit bei frei eingestellter Chattransparenz.

## 3. Typografie, Zeichen und Logo

- Fließtext bleibt eine gut lesbare Minecraft-Pixelschrift mit vollständigen deutschen Zeichen. Keine dekorative Schrift für Nachrichten oder Befehle.
- Eigene Schriftakzente sind für Signet und kurze Labels zulässig. Unterschiedliche Schriften dürfen keinen höheren Zeilenabstand oder abgeschnittene Glyphen erzeugen.
- Systemmeldungen trennen Kontext und Nachricht mit „|“, sowohl mit Logo als auch im Textfallback. Spieler-, Privat- und Staffnachrichten verwenden weiterhin „›“. Zwischen Bestandteilen steht jeweils ein normales Leerzeichen.
- Keine Regenbogenverläufe, wechselnden Trenner, langen Zierlinien, gesperrten Buchstaben oder dauerhaft animierten Namen.
- Für Systemnachrichten wird das originale farbige G als Chatsignet verwendet: hochauflösende 128×128-Textur mit transparentem Hintergrund, bewahrtem Seitenverhältnis und möglichst wenig leerem Rand. Gold kennzeichnet den Systemabsender; das Logo behält seine eigenen Farben. Die flache Cracked-G-Variante bleibt eine Alternative, falls das Original bei kleiner Anzeige nicht ausreichend lesbar ist. Die vollständige Grounds-Wortmarke ist für größere Begrüßungen oder besondere Ankündigungen vorgesehen, nicht vor jeder Systemzeile.
- Texturauflösung und Anzeigegröße werden getrennt festgelegt. 128×128 bezeichnet die Quelldatei; ungefähr acht GUI-Pixel Höhe sind der Ausgangspunkt für die kompakte Chatdarstellung. Ein Java-Bitmap-Font skaliert die Grafik über height und richtet sie über ascent zur Textzeile aus; dafür ist kein Shader erforderlich. Die Logo-Komponente wird weiß eingefärbt, damit die Originalfarben erhalten bleiben. Feine Details können bei kleiner Anzeige verschwinden; Erkennbarkeit, Abstände und Zeilenüberschneidungen werden bei mehreren GUI-/Chatskalierungen getestet. Bedrock erhält eine separat geprüfte Font-/Assetdarstellung; Java-Fontparameter werden nicht als direkt übertragbar vorausgesetzt.
- Köpfe, Signet und Emojis orientieren sich an derselben optischen Höhe und Grundlinie. Abnahme erfolgt bei mehreren GUI- und Chatskalierungen.

### Logoabgleich und Farbkorrektur

Der visuelle Logoabgleich am 14. Oktober 2026 basiert auf den Originaldateien logo/logo-png-noglow.png und flat versions/PNG/icon blackstandard.png im vom Betreiber genannten Logoordner. Die Wortmarke verbindet warmen hellen Stein, dunkle braune Bruchkanten, goldene Metallflächen, leuchtende cyanfarbene Kristalle und kleine grüne Pflanzenakzente. Das flache Zeichen übernimmt die gebrochene G-Silhouette und eine markante Kristall-Aussparung links. Daher passt die steinerne, handwerkliche Grundrichtung; Gold und Cyan werden stärker an die vorhandene Marke angelehnt. Kupfer bleibt eine aus der Stadtstory abgeleitete Ergänzung. Die Hexwerte sind visuell gewählte UI-Näherungen und keine gemessenen oder verbindlich vom Logo übernommenen Markenfarbwerte. Das vorhandene Kristallmotiv legt Vaeriths Lore-Farbe nicht fest. Bestätigte Logoentscheidung: Das originale farbige G wird zuerst aus einer 128×128-Textur klein im Chat angezeigt. Steinstruktur und Kristallfarben bleiben Bestandteil der Grafik; zusätzliche animierte Effekte werden dadurch nicht beschlossen. Die Wortmarke erscheint nur in größeren, ausgewählten Meldungen. Die tatsächliche Erkennbarkeit des G bei kompakter Anzeige ist am Client zu prüfen; bei Bedarf steht die flache Variante zur Verfügung.

## 4. Verbindliche Nachrichtengrammatik

Die folgenden Regeln gelten für die geplante Darstellung von Lobby-, Match-, Gruppen-, Privat- und Staff-Chat sowie Systemantworten. Routing, Berechtigungen und Moderation werden durch die Gestaltung nicht verändert.

| Art | Format | Kennzeichnung |
| --- | --- | --- |
| Öffentlicher Spielerchat | [Kopf] Name › Nachricht | Kopf, klarer Name, heller Inhalt |
| Spielerchat mit erforderlichem Kontext | [Kopf] [Team] Name › Nachricht | Kanal bzw. Team vor dem Namen |
| Private Nachricht eingehend | [Kopf] [Privat] Name → du › Nachricht | Explizites Privatlabel und Richtung |
| Private Nachricht ausgehend | [Kopf] [Privat] du → Name › Nachricht | Kopf bezeichnet den tatsächlichen Sender |
| Partychat, sofern verfügbar | [Kopf] [Party] Name › Nachricht | Textlabel bleibt sichtbar |
| Staffchat | [Kopf] [Staff] Name › Nachricht | Nur im berechtigten Kanal; Staff bleibt Spielerabsender |
| Systemmeldung | [Logo] · Kategorie \| Nachricht | Logo als Absenderkennung |
| NPC-Dialog im Chat | [NPC-Porträt] [NPC] Name › Dialog | NPC-Kennung; nicht wie ein realer Spieler tarnen |

Die eckigen Platzhalter [Kopf], [Logo] und [NPC-Porträt] beschreiben Grafiken, keine wörtlich anzuzeigenden Texte. Mit geladenem Chat-Pack ersetzt das Logo die ausgeschriebene Grounds-Kennung; der Name steht ausschließlich im Textfallback. Ohne passende Grafikunterstützung entfallen Kopf/Porträt; das Systemlogo wird durch [Grounds] ersetzt. Beispielsweise: „[Grounds] Allgemein | Neustart in 5 Minuten.“

Öffentlicher und Gruppenchat behalten dieselbe Texthelligkeit. Ein Kanal wird durch ein kurzes Label unterschieden, wenn sein Kontext sonst mehrdeutig ist. „Team“ im Match und „Staff“ für Mitarbeiter sind unterschiedliche Kanäle.

Native Skin-Köpfe erhalten eine explizit weiße Farbmodulation. Farben von Privatlabels, Namen, Rängen oder übergeordneten Komponenten dürfen die Hauttextur nicht einfärben.

Vorhandene Rangtags verwenden dieselbe Bezeichnung und Rangfarbe wie die Tabliste. Mit geladenem Pack erscheint dasselbe grafische Badge wie in der Tabliste: farbige Fläche, weiße kompakte Beschriftung in `grounds:tab_labels`, bestehende Flächen aus `grounds:tab`. Ohne bestätigtes Pack bleibt das vorhandene Rangtag lesbarer Text in Rangfarbe. Der Spielername bleibt im normalen hellen Textton (#F2EBDD), der native Kopf neutral weiß. Ein fehlendes oder leeres Rangtag erzeugt keinen zusätzlichen Präfix. Beispiel: `[Kopf] [MOD] Lukas › Hallo zusammen!` (MOD ist eine beispielhafte Rangbezeichnung). Es wird genau das vorhandene Rangtag dargestellt; keine zusätzliche Kette aus Rang, Level, Titel und Cosmetics. Detailinformationen gehören in Profil oder unterstützte Hoveransicht. Kritische Informationen dürfen nie ausschließlich im Hover stehen.

Systemkategorie und Level sind getrennte Metadaten. Die Kategorie beschreibt den fachlichen Kontext: Allgemein, Freunde, Party, Match oder Story. Das Level beschreibt Status beziehungsweise Dringlichkeit: Info, Erfolg, Warnung oder Fehler. Eine fehlgeschlagene Freundesaktion bleibt deshalb in der Kategorie Freunde und erhält das Level Fehler. Das Level bestimmt die Statusfarbe des Kontextlabels; es ersetzt das Kontextlabel nicht. Standard sind `GENERAL` und `INFO`. Spielerbezogene Mitteilungen werden an die betroffenen Empfänger adressiert, nicht unnötig global verteilt. Spieler dürfen mit eigenen Eingaben weder das reservierte Systemsignet noch administrative Labels oder Formatierung erzeugen.

## 5. Sprache und Beispiele

Grounds spricht freundlich, direkt und praktisch. Eine Meldung nennt Ergebnis oder Problem zuerst und bei Bedarf einen nächsten Schritt. Technische Service-, Exception- und Transportnamen gehören nicht in normale Spielerhinweise. Lore-Begriffe erklären Funktionen, statt bekannte Bedienwörter zu verdrängen.

| Anlass | Muster |
| --- | --- |
| Spielerchat | [Kopf] Lukas › Hat jemand Lust auf ein Duell? |
| Party-Einladung | [Logo] · Party \| Mia lädt dich ein. [Annehmen] |
| Queue | [Logo] · Match \| Du wartest auf ein 1vs1. |
| Bestätigung (`GENERAL` / `SUCCESS`) | [Logo] · Allgemein \| Deine Einstellungen wurden gespeichert. |
| Neustart (`GENERAL` / `WARNING`) | [Logo] · Allgemein \| Neustart in 5 Minuten. |
| Fehler (`GENERAL` / `ERROR`) | [Logo] · Allgemein \| Diese Aktion ist gerade nicht möglich. Versuch es gleich noch einmal. |
| Freundesfehler (`FRIENDS` / `ERROR`) | [Logo] · Freunde \| Deine Freundesliste ist voll. |
| Freiwillige Story | [Logo] · Story \| In der Werkstatt gibt es Neuigkeiten. |
| Join im relevanten Umfeld | [Logo] · Allgemein \| Mia ist angekommen. |

Aktionsklammern bezeichnen echte unterstützte Aktionen; sie dürfen nicht wie klickbare Schaltflächen aussehen, wenn der Client keine Aktion anbietet. Für Bedrock erhält jede notwendige Aktion einen dort nutzbaren Weg, etwa einen geprüften Befehl oder ein Formular. Solange dieser fehlt, keine vorgetäuschte Klickbarkeit. Unbekannte Fehlerursachen werden nicht als angeblich bekannter Zustand ausgegeben.

Join/Leave bleibt auf sinnvolle Empfängergruppen begrenzt. Storymeldungen sind freiwillig und dürfen Spielauswahl, Queue oder Match nicht blockieren. Begrüßungstexte und Stadtmetaphern bleiben kurz; reguläre Systemantworten werden nicht als NPC-Sprache ausgegeben.

## 6. Emojis

Startset: :heart:, :smile:, :laugh:, :thumbsup:, :gg:, :fire:, :creeper: und :wrench:. Pixelgrafiken erhalten einen gemeinsamen Stil, eine kompakte Größe und erkennbare Silhouetten. Die Nachrichtenfarbe darf bunte Grafiken nicht unbeabsichtigt einfärben.

- Kurzformen funktionieren ohne besondere Tastatur und werden serverseitig über eine kuratierte Zuordnung aufgelöst.
- Unterstützte direkt eingegebene Unicode-Emojis können als zusätzliche Aliase normalisiert werden. Vollständige Unterstützung sämtlicher Unicode-Emojisequenzen wird nicht zugesagt.
- Ohne passende Pack-/Clientunterstützung bleiben lesbare Kurzformen wie :heart: sichtbar; keine fehlenden Glyphen oder privaten Codepoints im Fallback.
- /emoji ist der vorgeschlagene Einstieg zur Übersicht; Existenz, Suchfunktion und Vervollständigung sind noch umzusetzen.
- Richtwert: höchstens fünf grafisch ersetzte Emojis pro Nachricht. Weitere Kurzformen bleiben Text. Bestehende Längen- und Spamregeln gelten weiterhin.
- Die serverseitige Emoji-Auflösung darf die Längenprüfung nicht aushebeln. Relevante Rohtexte, normalisierte Bedeutung und tatsächlich zugestellte Darstellung bleiben für die vorhandene Moderations-/Reporting-Pipeline nachvollziehbar.
- Freier Spielertext wird nicht als MiniMessage oder ausführbarer Richtext geparst. Nur kontrollierte Emoji-Tokens und explizit freigegebene Komponenten werden eingesetzt.

## 7. Java, Bedrock und reduzierte Darstellung

| Fähigkeit | Java 26.3 | Bedrock über Geyser | Reduzierte Darstellung |
| --- | --- | --- | --- |
| Absender und Kategorien | Adventure-Komponenten | Eigener geprüfter Renderer | Namen, Labels und Trenner als Text |
| Skinkopf | Native Player-Object-Komponente | Eigener Machbarkeitstest; Java-Komponente nicht ungeprüft weitergeben | Name ohne Bild |
| Grounds-Signet und Emojis | Eigene Sprite-/Fontassets im Java-Pack | Eigene Bedrock-Assets und Zuordnung | [Grounds] und Emoji-Kurzformen |
| Farben | Zielwerte per Komponenten | Unterstützte Farbpalette prüfen | Klare Textlabels |
| Hover und Klick | Nur vom Client unterstützte Aktionen | Separat prüfen; notwendige Alternativen bereitstellen | Explizite Befehle oder verfügbare Menüs |
| Chatflächen und Effekte | Pack-/Shader-Prototyp | Bedrock-UI-Prototyp unabhängig davon | Nativer Chat ohne Effekte |

Java unterstützt Spieler- und Atlasobjekte in Text seit 1.21.9. Der exakte Weg durch die eingesetzten Minestom-, Adventure-, Velocity- und Geyser-Versionen muss dennoch getestet werden. Der veröffentlichte Geyser-Fehlerbericht zu Player Objects ist ein konkreter Grund für eine getrennte Ausgabe, kein Nachweis über den aktuell eingesetzten Build.

Gemeinsame Markenidentität bedeutet gleiche Bedeutung, Motive und Absenderlogik. Identische Shader, Layoutparameter oder Pixelergebnisse auf beiden Editionen sind kein zugesagtes Ziel.

Auch mit einem grundsätzlich erforderlichen Serverpack ist eine reduzierte Textdarstellung für Ladephase, Fehlerfälle und begrenzte Fähigkeiten sinnvoll. Diese Regel ändert die bestehende Pack-Zutrittsrichtlinie nicht.

## 8. Shader und Oberfläche

Shader sind eine optionale visuelle Erweiterung. Der erste Prototyp untersucht ausschließlich eine dezente Betonung des Systemsignets und gegebenenfalls eine lokale Systemmeldungsfläche. Geometrie, Umbruch und Effektgrenzen sind erst nach Clientprüfung festzulegen.

Kein globaler Bildschirmfilter, kein dauerhaftes Pulsieren, Wackeln oder Glühen im normalen Chat. Normale Spielernachrichten bleiben statisch. Informationen, Eingabe, Auswahl, Kopieren und Scrollen müssen ohne Effekt verständlich und bedienbar bleiben.

Java-Core-Shader erlauben keine beliebige neue Chat-Bedienlogik. Ein Chat mit freien Kartenlayouts, eigener Emoji-Schaltfläche, neuem Eingabefeld oder zuverlässiger Erkennung des offenen Chats wird durch diese Spezifikation nicht als machbar zugesagt. Postprocessing in 26.3 ist ebenfalls kein Ersatz für eine Chat-UI-Schnittstelle.

Core-Shader-Overrides sind von Mojang als nicht offiziell unterstützte Erweiterung beschrieben. 26.3 verändert unter anderem Shaderkompilierung und Textshader. Der Prototyp muss auf dem exakten Zielclient funktionieren; frühere Shaderrezepte aus dem Resourcepack-v2-Konzept gelten nicht als 26.3-Kompatibilitätsnachweis.

Für Bedrock wird die im Zielclient verwendete Chat-/HUD-Oberfläche separat auf Ressourcenpaket-Anpassbarkeit geprüft. Mojangs chat_screen.json ist ein Ausgangspunkt, kein Nachweis, dass jede Geräteoberfläche oder gewünschte Funktion identisch anpassbar ist.

## 9. Einordnung in die vorhandene Architektur

Das bestehende plugin-chat-Konzept beschreibt Server-, Gruppen-, Privat- und Staffchat sowie Ausgabe an mehreren Stellen. Neue Vorlagen sollen aus einer gemeinsamen semantischen Definition entstehen: Absenderart, Kategorie, Level, Kanal, relevante Spieler, Nachricht und erlaubte Aktionen. Erst am Ausgabeweg werden Clientfähigkeiten, Packzustand und Darstellung gewählt. Die lokale Umsetzung folgt dieser Trennung; Routing und Kanalberechtigungen behalten ihre bestehenden Verträge.

Kein für Java fertig gerendertes Bild-/Object-Component-Format ungeprüft an alle Empfänger senden. Bestehende Systemausgaben anderer Plugins müssen bei der späteren Integration erfasst werden: Social, Queue/Match, Moderation, Story, Befehlsantworten und Join/Leave.

Der dokumentierte Zwei-Pack-Vertrag bleibt erhalten: grounds-platform besitzt allgemeine Renderer-/Shader-/Font-Infrastruktur und allein assets/minecraft; grounds-content liefert thematische Markenmotive und Spielinhalte über den vorgesehenen Beitragsmechanismus. Allgemeine Chat-, Tab- und Menü-Fonts einschließlich des fest eingebundenen Chat-Signets gehören zur Plattform. Endgültige Pfade und Zuständigkeiten werden gegen die tatsächlichen Repositories geprüft. Keine neue dritte Packschicht und keine Live-Pack-Neuverteilung werden durch dieses CD eingeführt.

Die Scene-/Resource-Pack-Plattform führt Bedrock bisher ausdrücklich als zurückgestellt. Der hier gewünschte Bedrock-Chat ist neuer Zielumfang und benötigt eine eigene Umsetzung und Abnahme. Er erklärt die bestehende Plattform nicht nachträglich für Bedrock-kompatibel.

Diese Spezifikation ersetzt beim neuen Chatdesign die älteren Präsentationsbeispiele im plugin-chat-Konzept, nicht dessen Routing, Berechtigungen oder Implementierungsstatus. Das ältere Resourcepack-v2-Konzept bleibt Hintergrundmaterial; seine Shader- und Kompatibilitätsangaben müssen gegen 26.3 verifiziert werden.

## 10. Abnahme und nächste Arbeit

Logo und Assets wurden gesichtet, die Produktionsplugins inventarisiert und die Java-Implementierung lokal geprüft. Die eingesetzte Minestom-Zielversion ist der Grounds-Fork für Java 26.3; die visuelle Abnahme des gesamten Clientwegs steht aus.

Die nächste Abnahme vergleicht Spielernachricht, Privatnachricht, Systeminfo, Warnung, Fehler und Emoji im echten Client. Zuerst Java und der lesbare Bedrock-Fallback, anschließend optionale Effekte und eigene Bedrock-Grafiken.

Abnahmekriterien:

1. Spieler, System und NPC sind auf Anhieb durch Absenderkennung unterscheidbar, auch ohne Farbe oder Grafik.
2. Lange Nachrichten, Umlaute, Zeilenumbrüche und mehrere GUI-/Chatskalierungen bleiben lesbar; keine überlappenden Köpfe oder abgeschnittenen Icons.
3. Bedrock zeigt bei sämtlichen Beispielen Text; keine ungültigen Object-Komponenten, fehlenden Glyphen oder ausschließlich per Hover erreichbaren Informationen.
4. Ein-, ausgehende und eigene Privatnachrichten zeigen eindeutige Richtung; Team-, Party- und Stafflabels werden nicht verwechselt.
5. Emoji-Aliase, unbekannte Tokens, Grenzlängen und wiederholte Emojis ergeben vorhersehbare, moderierbare Darstellung.
6. Reduzierte Ausgabe funktioniert bei fehlender Packfähigkeit. Verhalten bei Ablehnung eines erforderlichen Packs folgt der vorhandenen Zutrittsrichtlinie.
7. Java-Effekte verändern keinen unbeteiligten HUD-/Welttext und sind bei Nichtunterstützung entbehrlich.
8. Helle/dunkle Umgebung, Scrollen, Fade, Auswahl und Kopieren werden am echten Client geprüft; Bedrock zusätzlich mit relevanten Touch-/Controller-Eingaben.
9. Rendererentscheidungen ändern keine Kanalberechtigung, Sichtbarkeit, Blockregel oder Reporting-Zuordnung.

Bestätigt sind die Gestaltungsrichtung und die Verwendung des originalen farbigen G aus einer 128×128-Textur, mit der Wortmarke für größere Meldungen und dem flachen G als Alternative. Clienttests sind weiterhin offen. Die finale Anzeigegröße und Ausrichtung, Bedrock-Logo-/Kopf-Darstellung, genaue Vaerith-Farbe und zusätzliche Effekte bleiben konkret zu prüfen.

## 11. Dokumentation und Quellen

Die Confluence-Seite erläutert die Story-Ableitung und Sollregeln. Der Docs-Spiegel liegt unter docs/design/minecraft-mini-cd-and-chat.md im Grounds-Arbeitsbereich. Beide Fassungen erhalten dieselbe Versionsnummer und gegenseitige Verweise; Regeländerungen werden gemeinsam gepflegt. Die Seiten liegen im Repository docs unter design/ und sind in der Navigation Reference → Minecraft Design eingetragen.

Interne Grundlagen:

- [Grounds — Story, Lobbywelt & Player Journey](https://grounds.atlassian.net/wiki/spaces/gkd/pages/294649857)
- [Konzept: plugin-chat](https://grounds.atlassian.net/wiki/spaces/gkd/pages/156172297)
- [Grounds Resourcepack – Vollständiges Konzept (v2)](https://grounds.atlassian.net/wiki/spaces/gkd/pages/138641411)
- [Scene, NPC & Resource Pack Platform — Concept and Master Plan](https://grounds.atlassian.net/wiki/spaces/GARCHITECT/pages/257622018)

Technische Referenzen:

- [Minecraft Java 1.21.9: Textobjekte](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-9)
- [Minecraft Java 26.3: Shader und Postprocessing](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3)
- [Minestom: Adventure](https://minestom.net/docs/adventure/what-is-adventure)
- [Geyser: Ressourcenpakete](https://geysermc.org/wiki/geyser/packs/)
- [Geyser: Player-Object-Fehlerbericht](https://github.com/GeyserMC/Geyser/issues/6301)
- [Mojang: Bedrock chat_screen.json](https://github.com/Mojang/bedrock-samples/blob/main/resource_pack/ui/chat_screen.json)

Confluence: [Grounds — Minecraft Mini-CD & Chatdesign](https://grounds.atlassian.net/wiki/spaces/gkd/pages/300154881)


## 12. Verbindlicher i18n- und Layoutvertrag

Jedes Plugin besitzt seine fachlichen Nachrichtenschlüssel und `messages*.properties`. `library-i18n` übernimmt Locale-Auswahl, Sprachfallback, sichere Platzhalter und die gemeinsame Darstellung. Eine Übersetzung oder neue Fachnachricht benötigt dadurch keinen eigenen Nachrichtenrenderer.

Die gemeinsame Bibliothek besitzt die Layoutvorlagen einschließlich Platzhalterreihenfolge, Trennzeichen, Markenbezeichnung und Kategorien. Dazu gehören `layout.text-prefix`, `layout.graphical-prefix` und `layout.player` im Bundle `gg.grounds.i18n.system`. Sprachvarianten dürfen die Reihenfolge verändern. Privat- und Staffkanäle besitzen ebenfalls übersetzbare Vorlagen mit Komponentenplatzhaltern für Absender, Empfänger und Nachricht. Keine sprachlichen Präfixe werden im Plugin-Code zusammengesetzt.

Der Renderer besitzt Farben und grafische Komponenten. Die Nachrichtenschlüssel tragen Kategorie und Level als unabhängige semantische Felder; beides wird nicht aus dem englischen Text oder dessen Farbe erraten. `SystemCategory` enthält nur Kontexte, `SystemLevel` nur Statuswerte. Das Standardlayout zeigt `<category>`; übersetzbare Vorlagen können zusätzlich `<level>` anzeigen. Der Renderer bestimmt Statusfarben, während i18n Kontext- und Levelbezeichnungen sowie deren Platzierung besitzt. Clientfähigkeiten bestimmen ausschließlich die Darstellung, niemals Sprache, Berechtigung oder Routing.

- `send`: lokalisierte Systemnachricht mit gemeinsamem Kategorienpräfix.
- `sendRendered`: bereits zusammengesetzte Systemkomponenten mit demselben Präfix; Klick- und Hoveraktionen bleiben erhalten.
- `render`: unpräfixierte Komponenten für Menüs, Titel, Actionbars und Verbindungsdialoge.
- `sendUnprefixed`: übersetzbare Unterhaltungsvorlagen; keine Systemmeldungen.

Spielereingaben und externe Texte werden als uninterpretierte Strings eingesetzt. Nur explizite Komponentenplatzhalter erhalten Formatierung oder Aktionen. Bestehende dynamische Serverdiagnosen, Transportfelder und Konsolenausgaben werden nicht durch Übersetzungen verändert.

### Integrations- und Freigabegrenzen

Das Java-Pack besitzt `grounds:chat`: das originale 128×128-G auf U+E100, acht Emoji-Glyphen U+E101 bis U+E108, Bitmaphöhe 8 und Ascent 7. Die zwei bestehenden Packschichten und Pack-UUIDs bleiben bestehen.

Eine Proxy-Ausgabe darf das Logo nur nach bestätigtem erfolgreichem Laden eines tatsächlich angebotenen Packs mit explizit freigegebenem SHA-1 verwenden. Die Operator-Allowlist `CHAT_FONT_PACK_SHA1S` ist standardmäßig leer. Versenden oder Akzeptieren des Packs allein zählt nicht als geladen. Ein neues Pack-Angebot, Ladefehler oder ein Disconnect setzen die grafische Darstellung auf die lesbare Textausgabe zurück. Bei einem Backendwechsel startet die neue Spielerinstanz ohne Fontbereitschaft und erhält einen aktuellen Proxy-Snapshot; die bestätigten identischen Clientpacks bleiben gültig und werden nicht allein für das Chatdesign neu geladen. Alte oder mehrdeutige Packbestätigungen schalten keine Grafik frei. Bedrock und unbekannte Fähigkeiten erhalten keine Java-Objektkomponenten.

Die Umstellung wurde zunächst in isolierten Branches und Composite-Builds geprüft. Nach Veröffentlichung von library-i18n 0.4.0 und plugin-proxy-api 2.6.0 wurden alle 13 betroffenen Plugin-Builds eigenständig mit den veröffentlichten Artefakten geprüft. Die visuelle Abnahme im echten Java-26.3-Client bleibt vor der Packveröffentlichung erforderlich. Shader, Bedrock-Grafiken und Clientabnahme bleiben getrennte Folgearbeit.

### Empfängergebundene Darstellung

Der Proxy überträgt den bestätigten Fontstatus über `grounds:chat_style` an die konkrete aktuelle Backendverbindung. Der Vertrag enthält exakt 26 Bytes in Big Endian: Version `1` als Byte, Ziel-UUID als zwei Longs, positive monotone Revision als Long und Bereitschaft `0` oder `1` als Byte. Die Minestom-Spielerinstanz speichert den Status unter `grounds:chat-font-ready`. Neue Instanzen beginnen ohne Bereitschaft; Disconnect und Modulstopp löschen den Zustand. UUID muss zum Ereignisspieler passen; ältere oder doppelte Revisionen überschreiben keinen neueren Zustand. Clientpakete auf diesem Kanal werden am Proxy behandelt und nicht ungeprüft weitergeleitet.

Remote-Social-Meldungen bleiben im Transport semantische Übersetzungskeys mit Argumenten. Der empfängerbezogene `PlayerMessageRenderer` entscheidet erst auf dem zustellenden Proxy über Sprache und Logo. Fehlende oder inkompatible Renderer und fremde Komponenten behalten den bestehenden Ausgabeweg. Registrierung und Entfernen des Providers sind atomar und an dessen tatsächliche Instanz gebunden.

## 14. Berechtigungen für Spielernachrichten

Farben und Textstile gelten ausschließlich im Nachrichtentext. `grounds.chat.format.color` erlaubt benannte und Hexfarben über MiniMessage sowie `&0`–`&f` und `&#RRGGBB`; `grounds.chat.format.decoration` erlaubt Fett, Kursiv und Unterstrichen (`<bold>`, `<italic>`, `<underlined>` beziehungsweise `&l`, `&o`, `&n`). `&r` setzt freigegebene Formatierung zurück. Klickaktionen, Hover, Fontwechsel und verschleierter Text sind nicht freigegeben. Ohne jeweilige Berechtigung bleibt die Eingabe wörtlicher Text.

Eigene grafische Aliasse benötigen `grounds.chat.emoji.<name>`; `grounds.chat.emoji.*` kann alle freigeben. Konkrete Rechte werden beim Absender abgefragt, damit gezielte Sperren trotz Wildcard gelten. Maximal fünf genehmigte Grafiken pro Nachricht, auch über mehrere formatierte Abschnitte. Der Empfänger benötigt zusätzlich das bestätigt geladene Pack; andernfalls bleiben Aliasse lesbarer Text. Normale Unicode-Emojis bleiben Text. Diese Regeln gelten für öffentlichen, privaten und Staffchat sowie lokale Paper-/Minestom-Ausgabe. Rangbadge, Spielername und Kopf sind davon unabhängig.
