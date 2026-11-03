---
title: "Portal-Meldungen: Entwurf und Umsetzungsplan"
description: "Im Portal gepflegte HUD-Meldungen mit einer gemeinsamen Fläche, mehreren Meldungen pro Zeile und Servertypen-Auswahl pro Meldung."
---

# Portal-Meldungen: Entwurf und Umsetzungsplan

Stand: 3. November 2026. Status: Die Richtung ist im Chat freigegeben; die Funktion ist auf Stage und im Portal aktiv. Dieses Dokument beschreibt den Entwurf; der Umsetzungsbericht enthält den aktuellen Betriebsstand.

Fortschreibung: Die Proxy-Architektur und die Servertypen-Auswahl pro Meldung sind im Chat bestätigt. Der [detaillierte Implementierungsplan](../plans/2026-11-03-portal-announcements.md) konkretisiert Schnittstellen, Dateien, Prüfschritte und die sequenzielle Migration. Die aktuelle Discovery klassifiziert unter anderem `lobby`, `game` und `match`; einzelne Spielmodi sind dadurch nicht automatisch eigene Zieltypen. Der Plan nutzt einen expliziten, umgebungsbezogenen Katalog dieser tatsächlichen Rollen und kennzeichnet weitere Detailfestlegungen als technische Entscheidungen des Autors.

**Umsetzungsstand, aktualisiert am 3. November 2026:** Die Funktion und der größere Sand-Punkt (`•`, U+2022) sind auf allen drei Stage-Proxies aktiv. Die Portal-Worker-Version ist direkt Live aktiviert. Die Integration erfolgt ohne separate Releases, weil weitere Grounds-Chats an Portal und Moderation arbeiten. Der [Umsetzungsbericht](../reports/2026-11-03-portal-announcements-implementation.md) nennt die geprüften Artefakte und verbleibende Grenzen.

## Ziel und bestätigte Entscheidungen

Du verwaltest die Hinweise aus der oberen Lobby-Anzeige im Portal. Jede Meldung erhält eine zusammenhängende Hintergrundfläche, die ihren Text und optionalen Link umfasst. Mehrere Meldungen können nebeneinander stehen.

Im Chat bestätigt:

- Die Meldungen sollen im Portal einstellbar sein.
- Der Start konzentriert sich auf Lobbys, weil Hinweise während eines Spiels stören können.
- Die Auswahl der Servertypen erfolgt **pro Meldung**, nicht gemeinsam für alle Meldungen.

Die gemeinsame Fläche und mehrere Meldungen nebeneinander greifen den ursprünglichen Gestaltungsvorschlag auf. Die folgenden Detailregeln und die technische Architektur sind Vorschläge des Autors, keine bereits bestätigten Nutzerentscheidungen.

## Ansätze und Auswahl

| Ansatz | Vorteil | Grenze | Ergebnis |
| --- | --- | --- | --- |
| Feste Lobby-Anzeige | Kleiner Umfang | Weitere Servertypen erfordern eine spätere Erweiterung | Verworfen zugunsten der Auswahl pro Meldung |
| Gemeinsame Servertypen-Auswahl für alle Meldungen | Wenige Einstellungen | Unterschiedliche Zielbereiche einzelner Meldungen sind unmöglich | Nutzer bevorzugt Auswahl pro Meldung |
| Servertypen-Auswahl pro Meldung | Jede Meldung erreicht nur den gewünschten Bereich | Ein zusätzliches Feld je Meldung | Im Chat ausgewählt; neue Meldungen schlagen nur `lobby` vor |

Für die technische Umsetzung empfiehlt der Autor die Proxy-Seite: Der Proxy kennt das aktuelle Backend und kann dieselbe Funktion über Backend-Technologien hinweg anbieten. Ein ausschließlich in `plugin-lobby` verankerter Controller würde die gewählte Zielauswahl außerhalb der Lobby nicht umsetzen. Ein eigenes Modul in jedem Backend wäre möglich, erforderte aber zusätzliche Integrationen je Plattform.

## Produktverhalten

### Eine Meldung, eine Fläche

- Text und Link teilen sich einen dunklen Hintergrund mit den vorhandenen HUD-Ecken und Innenabständen.
- Der Text bleibt warmweiß, die optionale Adresse goldfarben und fett. Innerhalb der Fläche darf die Formatierung wechseln.
- Zwischen zwei Meldungen liegt ein größerer sichtbarer Abstand als zwischen Text und Adresse derselben Meldung.
- Die Adresse dient als lesbarer Hinweis. Eine Bossbar erhält dadurch keine anklickbare Web-Aktion.
- Inhalte sind strukturierter Klartext. HTML, freie MiniMessage-Tags und ausführbare Aktionen gehören nicht zum Editor.

### Auswahl und Wechsel

- Filtere zuerst nach Aktivierung und exaktem Servertyp, dann ordne nach der gespeicherten Reihenfolge.
- Vorschlag: höchstens zwei vollständige Meldungen pro Zeile. Passt die zweite nicht in das gemeinsame Pixelbudget, wandert sie in die nächste Gruppe.
- Vorschlag: Gruppen wechseln alle zehn Sekunden anhand der UTC-Epochenzeit; ein einzelnes Paar bleibt ruhig stehen. Gruppen werden pro Sprache deterministisch gebildet.
- Miss sichtbare Glyphen einschließlich Fettung, Innenabständen und Zwischenraum. Zeichenzahl allein reicht nicht.
- Zeige eine lange Meldung allein. Passt bereits diese nicht in das Zeilenbudget, lehne das Veröffentlichen mit einem konkreten Hinweis ab; kürze weder Text noch Adresse stillschweigend.
- Den genauen Abstand und das konservative Pixelbudget legt die Umsetzung anhand einer nativen Client-Prüfung fest und verwendet anschließend dieselben Werte in Vorschau, API-Validierung und Renderer. Der Server kennt Fensterbreite und GUI-Skalierung des Clients nicht zuverlässig; dies ist keine automatisch responsive Web-Zeile.
- Gibt es keine passenden Meldungen, verschwindet ausschließlich die eigene Meldungs-Bossbar.

### Servertypen

- Jede Meldung besitzt eine explizite Liste stabiler Servertyp-Schlüssel, beispielsweise `lobby`.
- Verwende den tatsächlichen `grounds/server-type`-Wert aus der Discovery, keine Pod-Namen, Präfix-Heuristiken oder vom Spieler stammenden Angaben.
- Die Auswahl ist keine Liste einzelner Serverinstanzen. `MINIGAME` aus der Minestom-Runtime ist ein grober Runtime-Typ und ersetzt keine genauere Discovery-Klassifikation.
- Neue Meldungen starten als inaktiver Entwurf mit `serverTypes: ["lobby"]`. Erst Speichern/Veröffentlichen aktiviert die gewünschte Konfiguration.
- Neue Servertypen werden bestehenden Meldungen nicht automatisch hinzugefügt; es gibt keinen impliziten Platzhalter für alle Typen.
- Ein aktiver Eintrag benötigt mindestens einen Zieltyp. Unbekannte oder aktuell nicht erreichbare, früher konfigurierte Typen bleiben im Editor erhalten und werden als nicht verfügbar gekennzeichnet.
- Ohne bestätigte Backend-Zuordnung erscheint keine Meldung. Nach einem Wechsel wird die Auswahl unmittelbar neu ausgewertet, auch beim Wechsel zwischen Proxies.

## Portal und Datenvertrag

In der bestehenden Netzwerkverwaltung erhält jede Umgebung eine Meldungsverwaltung. Stage und Produktion behalten getrennte Dokumente und die vorhandenen umgebungsbezogenen Lese- und Schreibrechte.

Die Oberfläche bietet Liste, Aktivierung, Bearbeitung, Löschen und Änderung der Reihenfolge. Der Editor enthält einen internen Namen, Text/Übersetzungen, optionale Adresse sowie die Servertypen-Auswahl. Eine Vorschau zeigt die gefilterte und gruppierte Zeile für eine ausgewählte Sprache und einen Servertyp. Speichern meldet einen Erfolg erst nach bestätigter API-Antwort; es behauptet damit keine bereits bestätigte Zustellung an alle Proxies.

Vorgeschlagener gespeicherter Vertrag:

```ts
type Announcement = {
  id: string;                    // stabile UUID
  name: string;                  // nur im Portal
  enabled: boolean;
  serverTypes: string[];         // explizite Discovery-Schlüssel
  text: Record<string, string>;  // Sprachcode -> Klartext
  address: string | null;        // lesbare Adresse, keine Aktion
};

type AnnouncementSettings = {
  schemaVersion: 1;
  announcements: Announcement[]; // Arrayposition bestimmt die Reihenfolge
};
```

Für den Start werden `en`, `de`, `fr`, `es` entsprechend dem vorhandenen Hinweisbestand unterstützt. Englisch ist die erforderliche Fallback-Sprache; fehlende Übersetzungen verwenden Englisch, fehlende Adresse erzeugt keinen leeren Bereich. Interner Name und Ziele werden nicht übersetzt. Beschränkungen für Dokumentgröße, Anzahl und Textlängen sind serverseitig durchzusetzen und zwischen TypeScript und Kotlin mit gemeinsamen Vertragsfixtures zu prüfen. Das konkrete Größenbudget ist eine technische Umsetzungsentscheidung, zusätzlich zur verbindlichen visuellen Breitenprüfung.

Schreibzugriffe verwenden das vorhandene `expectedVersion`-Verfahren. Ein Konflikt zeigt einen Neuladehinweis und überschreibt keinen fremden Stand. Audit-Einträge enthalten Akteur, Umgebung sowie alte/neue Dokumentversion. Servertypen stammen aus einem expliziten Katalog derselben Discovery-Klassifikation; ein kurzfristig leeres Discovery-Ergebnis darf keine gespeicherten Ziele löschen. Die Umsetzung integriert diesen Katalog in die bestehende Netzwerk-API, statt die Typen in der Oberfläche zu erfinden.

## Vorhandene Quellen und Grenzen der Untersuchung

Die Arbeitskopien haben unterschiedliche Entwicklungsstände. Die folgende Analyse verwendet vorhandene Dateien und lokale Worktrees; sie ist keine Aussage über den aktuell live deployten Stand. Vor Implementierung werden die Zielrevisionen dieser Repositories abgeglichen, ohne fremde Änderungen zu verwerfen.

| Bereich | Untersuchte Quelle relativ zu `/home/lukas/grounds` | Befund |
| --- | --- | --- |
| Aktuelle Hinweislogik | `.worktrees/queue-scene-plugin-lobby/minestom/src/main/kotlin/gg/grounds/lobby/LobbyTitleBar.kt` | Drei feste Einträge; zehn Sekunden Rotation; Text und Adresse als getrennte `GroundsBossBar.Segment`-Flächen |
| HUD-Renderer | `.worktrees/scoreboard-library-minigame-api/minestom/src/main/kotlin/gg/grounds/minigame/minestom/GroundsBossBar.kt` | Minestom-gebundener Lebenszyklus; pro Segment genau ein Textstil und ein Hintergrund |
| HUD-Assets | `resourcepacks/resourcepacks-product/src/main/kotlin/gg/grounds/resourcepacks/product/HudFont.kt` | Vorhandene HUD-Slices und transparente pinke Bossbar; Codepoints beibehalten |
| Servertyp | `plugin-proxy/api/src/main/kotlin/gg/grounds/proxy/api/ServerDisplayQuery.kt` | `ServerDisplay.kind` entspricht `grounds/server-type`; registrierten Provider im Zielstand prüfen |
| Packbestätigung | `plugin-resourcepacks/velocity/src/main/kotlin/gg/grounds/resourcepacks/velocity/PlayerChatAppearance.kt` | Hashgebundene Chat-Font-Bestätigung; kein eigenständiger Nachweis der HUD-Fähigkeit |
| Netzwerk-Konfiguration | `grounds-forge/src/network/settings.ts`, `configClient.ts`, `src/routes/network.ts` | Umgebungstrennung, Versionsprüfung und Audithook vorhanden |
| Portal-Netzwerkverwaltung | `.worktrees/grounds-portal-contextual-motd/src/components/network/`, `src/lib/api/network-maintenance.ts` | Bestehende Netzwerk-Oberfläche und Zugriffspfade als Integrationsbasis |
| Verteilung | `service-config/README.md`, `plugin-config/common/src/` | Gespeicherter Snapshot ist maßgeblich; NATS ist nur Aktualisierungshinweis |

## Technischer Entwurf

1. **Eine zentrale Quelle:** Ergänze `announcements/active` unter dem bereits von der Netzwerkverwaltung verwendeten App-Scope `velocity` und der jeweiligen Umgebung in `service-config`. Forge liest und schreibt über den vorhandenen Client. Kein eigenes Meldungs-Backend und keine neue Datenbank sind nötig.
2. **Ein Eigentümer pro Verbindung:** `plugin-proxy` verwaltet genau eine eigene Meldungs-Bossbar für jeden lokal verbundenen Spieler. Jeder Proxy liest dasselbe Umgebungsdokument, entscheidet aber nur über seine aktuellen Verbindungen. Es wird kein Netzwerk-Broadcast versandt, der auf jedem Proxy dieselbe Anzeige erneut erzeugt.
3. **Servertyp und Sprache:** Der Controller liest Backend-Metadaten über `ServerDisplayQuery` und die vorhandene serverseitige Sprachquelle. Er reagiert auf Verbindung, Backend-Wechsel, Sprachänderung, Konfigurationsänderung, Packzustand und Disconnect. Unbekannte Typen oder fehlende Capabilities bedeuten keine Anzeige; verspätete Metadaten lösen eine erneute Prüfung aus.
4. **Gemeinsamer Renderer:** Löse die reine Pixelmessung und die Komposition von Hintergrundflächen aus der Minestom-Lebenszyklusklasse. Eine Fläche enthält mehrere formatierte Klartextteile; die öffentliche bisherige Segment-API bleibt für Spielanzeigen kompatibel. Der plattformunabhängige Teil darf Minestom nicht transitiv in Velocity laden. Bestehende Font-Codepoints und Textmetriken werden wiederverwendet, keine abweichende vereinfachte Breitenmessung im Proxy.
5. **HUD-Fähigkeit:** Ergänze eine explizite, hashgebundene HUD-Capability über die Pack-Integration. Eine erfolgreiche Bestätigung eines beliebigen Chat-Packs genügt nicht. Bei Ablehnung, Entfernen oder unbekanntem Zustand wird die Meldungsanzeige entfernt. Der vorhandene verpflichtende Plattform-Pack bleibt die Asset-Quelle.
6. **Wiederabgleich:** Lade initial einen autoritativen Snapshot; verwende NATS nur als Refresh-Trigger und periodisches Snapshot-Polling als Reparaturpfad. Ohne erste gültige Konfiguration bleibt die Anzeige leer. Bei späterem Ausfall bleibt der letzte gültige Stand erhalten, der Zustand wird als veraltet protokolliert, und der Leseversuch wird wiederholt. Eine gültige leere Liste oder Deaktivierung muss zuvor sichtbare Meldungen entfernen.
7. **Migration:** Übernimm Discord, Shop und Turniere mit stabilen IDs, den vorhandenen vier Übersetzungen und ausschließlich `lobby` als Ziel. Seede idempotent nur bei fehlendem Dokument. Ein ausdrücklich leeres Dokument wird niemals mit Defaults aufgefüllt. Ablösung von `LobbyTitleBar` und Aktivierung des Proxy-Controllers erfolgen über einen umgebungsbezogenen Eigentümer-Schalter mit Default `legacy`, sodass während gemischter Versionen genau eine Implementierung zuständig ist. Der Schalter gehört zum Rollout, nicht in den Meldungseditor.

## Umsetzungsplan

Die Änderung umfasst Portal, Forge/API, gemeinsamen HUD-Renderer, Pack-Integration, Proxy und Ablösung der Lobby-Anzeige. Sie wird als zusammengehörige Funktion in fünf überprüfbaren Schritten umgesetzt.

### 1. Datenvertrag und Netzwerk-API

Ergänze Schema, Default/Seed, Dokumenttyp, Servertypen-Katalog und GET/PUT-Endpunkte in `grounds-forge/src/network/` und `src/routes/network.ts`. Nutze vorhandene Rechte, Versionsprüfung, Audit und Response-Begrenzung. Lege gemeinsame gültige/ungültige JSON-Fixtures an.

Nachweis: Stage/Produktion bleiben isoliert; unberechtigte Zugriffe scheitern; konkurrierende Updates ergeben einen Konflikt; leere Listen bleiben leer; bestehende Ziele überleben fehlende Discovery-Daten; unbekannte Versionen werden abgelehnt.

### 2. Gemeinsame HUD-Flächen und Pack-Fähigkeit

Extrahiere den plattformunabhängigen Renderteil aus `library-minigame-api` in ein geeignetes Minestom-freies Modul desselben Repositories. Erweitere die Darstellung um mehrere Textstile innerhalb einer Fläche und ergänze die HUD-Capability in `plugin-proxy/api` und `plugin-resourcepacks`. Die bestehenden Spielanzeigen behalten ihren Vertrag.

Nachweis: Eine Meldung zeichnet genau einen Hintergrund; Fettung und Zwischenräume stimmen; zwei Meldungen besitzen zwei Flächen; bisherige Bossbar-Tests bleiben gültig; ein bestätigter falscher/alter Pack-Hash schaltet die Anzeige nicht frei. Ein nativer Client-Test legt Breitenbudget und Abstand fest.

### 3. Proxy-Controller und Verteilung

Ergänze einen fokussierten Meldungsbereich in `plugin-proxy/velocity` für Snapshot-Lesen, Validierung, Filterung, Gruppierung und Lebenszyklus. Verwende denselben Datenvertrag und dieselben Metriken wie in Schritt 1/2. HTTP-/NATS-I/O läuft außerhalb des Ereignispfads; veraltete asynchrone Ergebnisse dürfen nach einem Serverwechsel keine Anzeige wiederherstellen.

Nachweis: Lobby zeigt passende Meldungen; ein nicht ausgewählter Spieltyp zeigt keine; ein explizit ausgewählter anderer Typ funktioniert; Sprachwechsel aktualisiert die Gruppen; NATS-Ausfall heilt durch Polling; Packverlust und Disconnect räumen auf; zwei Proxies erzeugen pro Verbindung jeweils genau eine Anzeige.

### 4. Portal-Editor und Vorschau

Ergänze API-Hooks und Meldungskomponenten neben der vorhandenen Netzwerkverwaltung. Übernehme deren Umgebungsauswahl, Zugriffsprüfung und Konfliktbehandlung. Nutze die verbindlichen Messregeln für die Vorschau und erhalte nicht verfügbare Zieltypen beim Bearbeiten. Neue Einträge bleiben bis zum bewussten Speichern inaktiv.

Nachweis: Erstellen, Übersetzen, Aktivieren, Sortieren und Löschen wirken im gespeicherten Dokument; nur `lobby` ist vorausgewählt; breite Inhalte werden mit verständlicher Rückmeldung abgelehnt; Sprach-/Typvorschau stimmt mit den Vertragsfixtures überein; ein fehlgeschlagener Speichervorgang zeigt keinen Erfolg.

### 5. Migration und Integration auf Stage

Seed ohne Überschreiben vorhandener Dokumente, aktualisierte Abhängigkeiten und Eigentümer-Schalter gemeinsam ausrollen. Zuerst neue Lobby-Versionen bereitstellen, die den Schalter beachten, anschließend Proxy-Versionen ohne aktive Anzeige. Prüfe diese Fähigkeiten je Instanz, bevor der Schalter auf `proxy` wechselt. Mischbetrieb ohne Unterstützung des Schalters blockiert die Umschaltung. Die Legacy-Seite muss das Ausblenden bestätigen, bevor der neue Eigentümer beginnt; wenn die vorhandene Infrastruktur diese Bestätigung nicht unterstützt, verwende eine bewusst sequenzielle Stage-Umschaltung mit kurzer leerer Anzeige statt parallelem Besitz.

Nachweis: Zwei Proxies und zwei Lobbys; zusätzlich ein nicht ausgewählter Spielserver und ein testweise ausgewählter anderer Servertyp. Prüfe Wechsel zwischen Backends/Proxies, Neustarts, neue und bestehende Verbindungen, schmale Ansichten, GUI-Skalierung und alle vier Sprachen. Stelle sicher, dass Countdown- und Fortschrittsanzeigen der Spiele unverändert funktionieren und die Legacy-Meldungsbar nicht zusätzlich erscheint. Rollback schaltet auf bestätigte Legacy-Unterstützung zurück und behält das Meldungsdokument.

Produktion ist kein Teil des Planungsauftrags. Für eine später beauftragte Portal-Veröffentlichung gilt: passenden Cloudflare-Worker-Build/Version direkt Live aktivieren, vorhandene Bindings/Secrets/Flags erhalten und keinen Portal-Releaseprozess starten.

## Nicht im ersten Umfang

- Zeitplanung, Kampagnen, zufällige Gewichtung und Prioritätsregeln.
- Klickaktionen, Klickstatistiken und individuelle Werbeprofile.
- Auswahl einzelner Serverpods oder automatische Freigabe neuer Servertypen.
- Ein allgemeiner Editor für Spiel-Countdowns, Fortschrittsbalken oder sämtliche HUD-Flächen.
- Unbeauftragtes Aktivieren in Produktion.

## Abnahmekriterien

Der Editor kann eine Meldung ohne Codeänderung ändern. Text und Adresse stehen in genau einer gemeinsamen Fläche. Zwei kurze Meldungen erscheinen nebeneinander, weitere passende Meldungen rotieren als vollständige Gruppen. Jede Meldung erreicht ausschließlich ihre ausgewählten Servertypen. Neue Einträge schlagen nur Lobby vor. Ein Wechsel in einen nicht ausgewählten Spieltyp entfernt die Anzeige. Diese Regeln funktionieren mit mindestens zwei Proxies, ohne doppelte Anzeigen, und nach einem verpassten NATS-Ereignis stellt der Snapshot-Abgleich den gespeicherten Zustand wieder her.
