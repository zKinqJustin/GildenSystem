# GildenSystem

Ein Minecraft Plugin für Paper 1.21.4, das ein umfassendes Gildensystem mit GUI-Interface bietet.

## Features

- **Gilden erstellen**: Spieler können eigene Gilden mit Namen und Kürzel erstellen
- **Gilden verwalten**: Gildenersteller können Einstellungen ändern und Mitglieder verwalten
- **Rangsystem**: Drei Ränge (Anführer, Ältester, Anwärter) mit unterschiedlichen Berechtigungen
- **Beitrittsanfragen**: Geschlossene Gilden erfordern eine Anfrage zum Beitreten
- **Gildenkürzel im Chat**: Das Gildenkürzel wird im Chat vor dem Spielernamen angezeigt
- **Persistente Speicherung**: Alle Daten werden in einer MariaDB-Datenbank gespeichert

## Installation

1. Stelle sicher, dass du einen Paper-Server (Version 1.21.4) verwendest
2. Lade das Plugin in den `plugins`-Ordner deines Servers
3. Starte den Server neu oder lade das Plugin mit `/reload`
4. Konfiguriere die Datenbankverbindung in der `config.yml`

## Datenbank-Konfiguration

Das Plugin benötigt eine MariaDB-Datenbank. Konfiguriere die Verbindungsdaten in der `config.yml`:

```yaml
database:
  host: localhost
  port: 3306
  database: minecraft
  username: root
  password: ""
```

## Befehle

- `/gilde` - Öffnet das Gilden-GUI

## Berechtigungen

Das Plugin verwendet keine speziellen Berechtigungen. Jeder Spieler kann eine Gilde erstellen und verwalten.

## Verwendung

1. Führe den Befehl `/gilde` aus, um das Gilden-GUI zu öffnen
2. Klicke auf das Schild-Item, um eine neue Gilde zu erstellen
3. Gib den Namen und das Kürzel deiner Gilde im Chat ein
4. Verwalte deine Gilde über das GUI

## Entwickelt von

zKinqJustin
