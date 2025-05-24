package de.justindev.gildensystem.database;

import de.justindev.gildensystem.GildenSystem;
import de.justindev.gildensystem.models.Gilde;
import de.justindev.gildensystem.models.GildeMitglied;
import de.justindev.gildensystem.models.GildeRang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final GildenSystem plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public DatabaseManager(GildenSystem plugin) {
        this.plugin = plugin;
        
        // Lade Konfigurationswerte oder setze Standardwerte
        plugin.saveDefaultConfig();
        this.host = plugin.getConfig().getString("database.host", "localhost");
        this.port = plugin.getConfig().getInt("database.port", 3306);
        this.database = plugin.getConfig().getString("database.database", "minecraft");
        this.username = plugin.getConfig().getString("database.username", "root");
        this.password = plugin.getConfig().getString("database.password", "");
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    return;
                }
                
                Class.forName("org.mariadb.jdbc.Driver");
                connection = DriverManager.getConnection(
                        "jdbc:mariadb://" + host + ":" + port + "/" + database, username, password);
                
                plugin.getLogger().info("Datenbankverbindung hergestellt!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Verbinden zur Datenbank: " + e.getMessage(), e);
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Datenbankverbindung geschlossen!");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Schließen der Datenbankverbindung: " + e.getMessage(), e);
        }
    }

    public void createTables() {
        connect();
        try (Statement statement = connection.createStatement()) {
            // Gilde Tabelle
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS gilden (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL UNIQUE, " +
                    "kuerzel VARCHAR(5) NOT NULL UNIQUE, " +
                    "ersteller VARCHAR(36) NOT NULL, " +
                    "erstellungsdatum DATETIME NOT NULL, " +
                    "offen BOOLEAN NOT NULL DEFAULT TRUE)");

            // Gildenmitglieder Tabelle
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS gildenmitglieder (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "gilden_id INT NOT NULL, " +
                    "spieler_uuid VARCHAR(36) NOT NULL, " +
                    "spieler_name VARCHAR(16) NOT NULL, " +
                    "rang ENUM('ANFÜHRER', 'ÄLTESTER', 'ANWÄRTER') NOT NULL, " +
                    "beitrittsdatum DATETIME NOT NULL, " +
                    "FOREIGN KEY (gilden_id) REFERENCES gilden(id) ON DELETE CASCADE, " +
                    "UNIQUE (spieler_uuid))");

            // Gildenanfragen Tabelle
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS gildenanfragen (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "gilden_id INT NOT NULL, " +
                    "spieler_uuid VARCHAR(36) NOT NULL, " +
                    "spieler_name VARCHAR(16) NOT NULL, " +
                    "anfragedatum DATETIME NOT NULL, " +
                    "FOREIGN KEY (gilden_id) REFERENCES gilden(id) ON DELETE CASCADE, " +
                    "UNIQUE (spieler_uuid, gilden_id))");
            
            plugin.getLogger().info("Datenbanktabellen wurden erstellt oder existieren bereits!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen der Datenbanktabellen: " + e.getMessage(), e);
        }
    }

    // Prüfen und wiederherstellen der Verbindung
    private void checkConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Prüfen der Datenbankverbindung: " + e.getMessage(), e);
            connect();
        }
    }

    // Gilde erstellen
    public boolean createGilde(String name, String kuerzel, Player ersteller) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO gilden (name, kuerzel, ersteller, erstellungsdatum, offen) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, name);
            statement.setString(2, kuerzel);
            statement.setString(3, ersteller.getUniqueId().toString());
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            statement.setBoolean(5, true);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int gildenId = generatedKeys.getInt(1);
                    
                    // Ersteller als Anführer hinzufügen
                    addMitglied(gildenId, ersteller, GildeRang.ANFÜHRER);
                    
                    return true;
                }
            }
            
            return false;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen einer Gilde: " + e.getMessage(), e);
            return false;
        }
    }

    // Mitglied zur Gilde hinzufügen
    public boolean addMitglied(int gildenId, Player spieler, GildeRang rang) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO gildenmitglieder (gilden_id, spieler_uuid, spieler_name, rang, beitrittsdatum) VALUES (?, ?, ?, ?, ?)")) {
            
            statement.setInt(1, gildenId);
            statement.setString(2, spieler.getUniqueId().toString());
            statement.setString(3, spieler.getName());
            statement.setString(4, rang.name());
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Hinzufügen eines Mitglieds: " + e.getMessage(), e);
            return false;
        }
    }

    // Mitglied aus Gilde entfernen
    public boolean removeMitglied(UUID spielerUuid) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM gildenmitglieder WHERE spieler_uuid = ?")) {
            
            statement.setString(1, spielerUuid.toString());
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Entfernen eines Mitglieds: " + e.getMessage(), e);
            return false;
        }
    }

    // Rang eines Mitglieds ändern
    public boolean updateMitgliedRang(UUID spielerUuid, GildeRang neuerRang) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE gildenmitglieder SET rang = ? WHERE spieler_uuid = ?")) {
            
            statement.setString(1, neuerRang.name());
            statement.setString(2, spielerUuid.toString());
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren des Rangs: " + e.getMessage(), e);
            return false;
        }
    }

    // Gilde löschen
    public boolean deleteGilde(int gildenId) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM gilden WHERE id = ?")) {
            
            statement.setInt(1, gildenId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen einer Gilde: " + e.getMessage(), e);
            return false;
        }
    }

    // Alle Gilden abrufen
    public List<Gilde> getAllGilden() {
        checkConnection();
        List<Gilde> gilden = new ArrayList<>();
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM gilden")) {
            
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String kuerzel = resultSet.getString("kuerzel");
                UUID ersteller = UUID.fromString(resultSet.getString("ersteller"));
                LocalDateTime erstellungsdatum = resultSet.getTimestamp("erstellungsdatum").toLocalDateTime();
                boolean offen = resultSet.getBoolean("offen");
                
                Gilde gilde = new Gilde(id, name, kuerzel, ersteller, erstellungsdatum, offen);
                gilden.add(gilde);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Abrufen aller Gilden: " + e.getMessage(), e);
        }
        
        return gilden;
    }

    // Gilde nach ID abrufen
    public Gilde getGildeById(int gildenId) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM gilden WHERE id = ?")) {
            
            statement.setInt(1, gildenId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String kuerzel = resultSet.getString("kuerzel");
                    UUID ersteller = UUID.fromString(resultSet.getString("ersteller"));
                    LocalDateTime erstellungsdatum = resultSet.getTimestamp("erstellungsdatum").toLocalDateTime();
                    boolean offen = resultSet.getBoolean("offen");
                    
                    return new Gilde(gildenId, name, kuerzel, ersteller, erstellungsdatum, offen);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Abrufen einer Gilde nach ID: " + e.getMessage(), e);
        }
        
        return null;
    }

    // Gilde eines Spielers abrufen
    public Gilde getGildeByPlayer(UUID spielerUuid) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT g.* FROM gilden g " +
                "JOIN gildenmitglieder gm ON g.id = gm.gilden_id " +
                "WHERE gm.spieler_uuid = ?")) {
            
            statement.setString(1, spielerUuid.toString());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String kuerzel = resultSet.getString("kuerzel");
                    UUID ersteller = UUID.fromString(resultSet.getString("ersteller"));
                    LocalDateTime erstellungsdatum = resultSet.getTimestamp("erstellungsdatum").toLocalDateTime();
                    boolean offen = resultSet.getBoolean("offen");
                    
                    return new Gilde(id, name, kuerzel, ersteller, erstellungsdatum, offen);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Abrufen der Gilde eines Spielers: " + e.getMessage(), e);
        }
        
        return null;
    }

    // Alle Mitglieder einer Gilde abrufen
    public List<GildeMitglied> getGildeMitglieder(int gildenId) {
        checkConnection();
        List<GildeMitglied> mitglieder = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM gildenmitglieder WHERE gilden_id = ?")) {
            
            statement.setInt(1, gildenId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UUID spielerUuid = UUID.fromString(resultSet.getString("spieler_uuid"));
                    String spielerName = resultSet.getString("spieler_name");
                    GildeRang rang = GildeRang.valueOf(resultSet.getString("rang"));
                    LocalDateTime beitrittsdatum = resultSet.getTimestamp("beitrittsdatum").toLocalDateTime();
                    
                    GildeMitglied mitglied = new GildeMitglied(gildenId, spielerUuid, spielerName, rang, beitrittsdatum);
                    mitglieder.add(mitglied);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Abrufen der Gildenmitglieder: " + e.getMessage(), e);
        }
        
        return mitglieder;
    }

    // Gildeneinstellungen aktualisieren (offen/geschlossen)
    public boolean updateGildeSettings(int gildenId, boolean offen) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE gilden SET offen = ? WHERE id = ?")) {
            
            statement.setBoolean(1, offen);
            statement.setInt(2, gildenId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Aktualisieren der Gildeneinstellungen: " + e.getMessage(), e);
            return false;
        }
    }

    // Beitrittsanfrage erstellen
    public boolean createBeitrittsanfrage(int gildenId, Player spieler) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO gildenanfragen (gilden_id, spieler_uuid, spieler_name, anfragedatum) VALUES (?, ?, ?, ?)")) {
            
            statement.setInt(1, gildenId);
            statement.setString(2, spieler.getUniqueId().toString());
            statement.setString(3, spieler.getName());
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Erstellen einer Beitrittsanfrage: " + e.getMessage(), e);
            return false;
        }
    }

    // Beitrittsanfragen für eine Gilde abrufen
    public List<String[]> getBeitrittsanfragen(int gildenId) {
        checkConnection();
        List<String[]> anfragen = new ArrayList<>();
        
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT spieler_uuid, spieler_name, anfragedatum FROM gildenanfragen WHERE gilden_id = ?")) {
            
            statement.setInt(1, gildenId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String spielerUuid = resultSet.getString("spieler_uuid");
                    String spielerName = resultSet.getString("spieler_name");
                    String anfragedatum = resultSet.getTimestamp("anfragedatum").toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    
                    anfragen.add(new String[]{spielerUuid, spielerName, anfragedatum});
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Abrufen der Beitrittsanfragen: " + e.getMessage(), e);
        }
        
        return anfragen;
    }

    // Beitrittsanfrage löschen
    public boolean deleteBeitrittsanfrage(int gildenId, UUID spielerUuid) {
        checkConnection();
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM gildenanfragen WHERE gilden_id = ? AND spieler_uuid = ?")) {
            
            statement.setInt(1, gildenId);
            statement.setString(2, spielerUuid.toString());
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Löschen einer Beitrittsanfrage: " + e.getMessage(), e);
            return false;
        }
    }
}
