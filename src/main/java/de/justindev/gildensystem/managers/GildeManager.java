package de.justindev.gildensystem.managers;

import de.justindev.gildensystem.GildenSystem;
import de.justindev.gildensystem.models.Gilde;
import de.justindev.gildensystem.models.GildeMitglied;
import de.justindev.gildensystem.models.GildeRang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class GildeManager {

    private final GildenSystem plugin;
    private final Map<UUID, Integer> spielerGilden;
    
    public GildeManager(GildenSystem plugin) {
        this.plugin = plugin;
        this.spielerGilden = new HashMap<>();
        
        // Lade alle Spieler-Gilden-Zuordnungen in den Cache
        loadPlayerGuilds();
    }
    
    private void loadPlayerGuilds() {
        spielerGilden.clear();
        
        // Lade alle Gilden
        List<Gilde> gilden = plugin.getDatabaseManager().getAllGilden();
        
        // Für jede Gilde, lade die Mitglieder und speichere die Zuordnung
        for (Gilde gilde : gilden) {
            List<GildeMitglied> mitglieder = plugin.getDatabaseManager().getGildeMitglieder(gilde.getId());
            
            for (GildeMitglied mitglied : mitglieder) {
                spielerGilden.put(mitglied.getSpielerUuid(), gilde.getId());
            }
        }
    }
    
    public boolean createGilde(String name, String kuerzel, Player ersteller) {
        // Prüfe, ob der Spieler bereits in einer Gilde ist
        if (isPlayerInGilde(ersteller.getUniqueId())) {
            return false;
        }
        
        // Erstelle die Gilde in der Datenbank
        boolean success = plugin.getDatabaseManager().createGilde(name, kuerzel, ersteller);
        
        if (success) {
            // Aktualisiere den Cache
            Gilde gilde = plugin.getDatabaseManager().getGildeByPlayer(ersteller.getUniqueId());
            if (gilde != null) {
                spielerGilden.put(ersteller.getUniqueId(), gilde.getId());
            }
        }
        
        return success;
    }
    
    public boolean joinGilde(int gildenId, Player spieler) {
        // Prüfe, ob der Spieler bereits in einer Gilde ist
        if (isPlayerInGilde(spieler.getUniqueId())) {
            return false;
        }
        
        // Prüfe, ob die Gilde existiert
        Gilde gilde = plugin.getDatabaseManager().getGildeById(gildenId);
        if (gilde == null) {
            return false;
        }
        
        // Füge den Spieler zur Gilde hinzu
        boolean success = plugin.getDatabaseManager().addMitglied(gildenId, spieler, GildeRang.ANWÄRTER);
        
        if (success) {
            // Aktualisiere den Cache
            spielerGilden.put(spieler.getUniqueId(), gildenId);
        }
        
        return success;
    }
    
    public boolean leaveGilde(UUID spielerUuid) {
        // Prüfe, ob der Spieler in einer Gilde ist
        if (!isPlayerInGilde(spielerUuid)) {
            return false;
        }
        
        // Hole die Gilde des Spielers
        Gilde gilde = getPlayerGilde(spielerUuid);
        if (gilde == null) {
            return false;
        }
        
        // Prüfe, ob der Spieler der Ersteller ist
        if (gilde.getErsteller().equals(spielerUuid)) {
            // Der Ersteller kann die Gilde nicht verlassen, nur löschen
            return false;
        }
        
        // Entferne den Spieler aus der Gilde
        boolean success = plugin.getDatabaseManager().removeMitglied(spielerUuid);
        
        if (success) {
            // Aktualisiere den Cache
            spielerGilden.remove(spielerUuid);
        }
        
        return success;
    }
    
    public boolean deleteGilde(int gildenId, UUID spielerUuid) {
        // Prüfe, ob die Gilde existiert
        Gilde gilde = plugin.getDatabaseManager().getGildeById(gildenId);
        if (gilde == null) {
            return false;
        }
        
        // Prüfe, ob der Spieler der Ersteller ist
        if (!gilde.getErsteller().equals(spielerUuid)) {
            return false;
        }
        
        // Hole alle Mitglieder der Gilde
        List<GildeMitglied> mitglieder = plugin.getDatabaseManager().getGildeMitglieder(gildenId);
        
        // Lösche die Gilde
        boolean success = plugin.getDatabaseManager().deleteGilde(gildenId);
        
        if (success) {
            // Aktualisiere den Cache für alle Mitglieder
            for (GildeMitglied mitglied : mitglieder) {
                spielerGilden.remove(mitglied.getSpielerUuid());
            }
        }
        
        return success;
    }
    
    public boolean updateMitgliedRang(UUID spielerUuid, UUID targetUuid, GildeRang neuerRang) {
        // Prüfe, ob beide Spieler in einer Gilde sind
        if (!isPlayerInGilde(spielerUuid) || !isPlayerInGilde(targetUuid)) {
            return false;
        }
        
        // Prüfe, ob beide Spieler in der gleichen Gilde sind
        int gildenId = spielerGilden.get(spielerUuid);
        if (gildenId != spielerGilden.get(targetUuid)) {
            return false;
        }
        
        // Hole die Gilde
        Gilde gilde = plugin.getDatabaseManager().getGildeById(gildenId);
        if (gilde == null) {
            return false;
        }
        
        // Prüfe, ob der Spieler der Ersteller ist
        if (!gilde.getErsteller().equals(spielerUuid)) {
            return false;
        }
        
        // Prüfe, ob der Ziel-Spieler nicht der Ersteller ist (Ersteller-Rang kann nicht geändert werden)
        if (gilde.getErsteller().equals(targetUuid)) {
            return false;
        }
        
        // Aktualisiere den Rang des Mitglieds
        return plugin.getDatabaseManager().updateMitgliedRang(targetUuid, neuerRang);
    }
    
    public boolean updateGildeSettings(int gildenId, UUID spielerUuid, boolean offen) {
        // Prüfe, ob die Gilde existiert
        Gilde gilde = plugin.getDatabaseManager().getGildeById(gildenId);
        if (gilde == null) {
            return false;
        }
        
        // Prüfe, ob der Spieler der Ersteller ist
        if (!gilde.getErsteller().equals(spielerUuid)) {
            return false;
        }
        
        // Aktualisiere die Gildeneinstellungen
        return plugin.getDatabaseManager().updateGildeSettings(gildenId, offen);
    }
    
    public boolean createBeitrittsanfrage(int gildenId, Player spieler) {
        // Prüfe, ob der Spieler bereits in einer Gilde ist
        if (isPlayerInGilde(spieler.getUniqueId())) {
            return false;
        }
        
        // Prüfe, ob die Gilde existiert
        Gilde gilde = plugin.getDatabaseManager().getGildeById(gildenId);
        if (gilde == null) {
            return false;
        }
        
        // Erstelle die Beitrittsanfrage
        return plugin.getDatabaseManager().createBeitrittsanfrage(gildenId, spieler);
    }
    
    public boolean acceptBeitrittsanfrage(int gildenId, UUID spielerUuid, UUID targetUuid) {
        // Prüfe, ob der Spieler in der Gilde ist
        if (!isPlayerInGilde(spielerUuid)) {
            return false;
        }
        
        // Prüfe, ob die Gilde existiert
        Gilde gilde = plugin.getDatabaseManager().getGildeById(gildenId);
        if (gilde == null) {
            return false;
        }
        
        // Prüfe, ob der Spieler der Ersteller oder ein Ältester ist
        GildeMitglied mitglied = getMitglied(gildenId, spielerUuid);
        if (mitglied == null || (mitglied.getRang() != GildeRang.ANFÜHRER && mitglied.getRang() != GildeRang.ÄLTESTER)) {
            return false;
        }
        
        // Hole den Zielspieler
        Player targetPlayer = Bukkit.getPlayer(targetUuid);
        if (targetPlayer == null) {
            return false;
        }
        
        // Lösche die Beitrittsanfrage
        boolean deleted = plugin.getDatabaseManager().deleteBeitrittsanfrage(gildenId, targetUuid);
        
        if (deleted) {
            // Füge den Spieler zur Gilde hinzu
            boolean joined = plugin.getDatabaseManager().addMitglied(gildenId, targetPlayer, GildeRang.ANWÄRTER);
            
            if (joined) {
                // Aktualisiere den Cache
                spielerGilden.put(targetUuid, gildenId);
                return true;
            }
        }
        
        return false;
    }
    
    public boolean denyBeitrittsanfrage(int gildenId, UUID spielerUuid, UUID targetUuid) {
        // Prüfe, ob der Spieler in der Gilde ist
        if (!isPlayerInGilde(spielerUuid)) {
            return false;
        }
        
        // Prüfe, ob die Gilde existiert
        Gilde gilde = plugin.getDatabaseManager().getGildeById(gildenId);
        if (gilde == null) {
            return false;
        }
        
        // Prüfe, ob der Spieler der Ersteller oder ein Ältester ist
        GildeMitglied mitglied = getMitglied(gildenId, spielerUuid);
        if (mitglied == null || (mitglied.getRang() != GildeRang.ANFÜHRER && mitglied.getRang() != GildeRang.ÄLTESTER)) {
            return false;
        }
        
        // Lösche die Beitrittsanfrage
        return plugin.getDatabaseManager().deleteBeitrittsanfrage(gildenId, targetUuid);
    }
    
    public boolean isPlayerInGilde(UUID spielerUuid) {
        return spielerGilden.containsKey(spielerUuid);
    }
    
    public Gilde getPlayerGilde(UUID spielerUuid) {
        if (!isPlayerInGilde(spielerUuid)) {
            return null;
        }
        
        int gildenId = spielerGilden.get(spielerUuid);
        return plugin.getDatabaseManager().getGildeById(gildenId);
    }
    
    public GildeMitglied getMitglied(int gildenId, UUID spielerUuid) {
        List<GildeMitglied> mitglieder = plugin.getDatabaseManager().getGildeMitglieder(gildenId);
        
        for (GildeMitglied mitglied : mitglieder) {
            if (mitglied.getSpielerUuid().equals(spielerUuid)) {
                return mitglied;
            }
        }
        
        return null;
    }
    
    public List<Gilde> getAllGilden() {
        return plugin.getDatabaseManager().getAllGilden();
    }
    
    public List<GildeMitglied> getGildeMitglieder(int gildenId) {
        return plugin.getDatabaseManager().getGildeMitglieder(gildenId);
    }
    
    public List<String[]> getBeitrittsanfragen(int gildenId) {
        return plugin.getDatabaseManager().getBeitrittsanfragen(gildenId);
    }
    
    public String getGildenPrefix(UUID spielerUuid) {
        if (!isPlayerInGilde(spielerUuid)) {
            return "";
        }
        
        Gilde gilde = getPlayerGilde(spielerUuid);
        if (gilde == null) {
            return "";
        }
        
        return "§8[§6" + gilde.getKuerzel() + "§8] §r";
    }
}
