package de.justindev.gildensystem.gui;

import de.justindev.gildensystem.GildenSystem;
import de.justindev.gildensystem.models.Gilde;
import de.justindev.gildensystem.models.GildeMitglied;
import de.justindev.gildensystem.models.GildeRang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GildeGUI {

    private final GildenSystem plugin;
    
    public GildeGUI(GildenSystem plugin) {
        this.plugin = plugin;
    }
    
    // Hauptmenü
    public void openMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "§6Gilden System");
        
        // Prüfe, ob der Spieler in einer Gilde ist
        boolean inGilde = plugin.getGildeManager().isPlayerInGilde(player.getUniqueId());
        
        // Gilde erstellen Item (Schild)
        if (!inGilde) {
            ItemStack createItem = new ItemStack(Material.SHIELD);
            ItemMeta createMeta = createItem.getItemMeta();
            createMeta.setDisplayName("§6Gilde erstellen");
            List<String> createLore = new ArrayList<>();
            createLore.add("§7Klicke, um eine neue Gilde zu erstellen");
            createMeta.setLore(createLore);
            createItem.setItemMeta(createMeta);
            inventory.setItem(11, createItem);
        }
        
        // Alle Gilden anzeigen Item (Truhe)
        ItemStack listItem = new ItemStack(Material.CHEST);
        ItemMeta listMeta = listItem.getItemMeta();
        listMeta.setDisplayName("§6Gilden Liste");
        List<String> listLore = new ArrayList<>();
        listLore.add("§7Klicke, um alle Gilden anzuzeigen");
        listMeta.setLore(listLore);
        listItem.setItemMeta(listMeta);
        inventory.setItem(13, listItem);
        
        // Meine Gilde Item (nur wenn in einer Gilde)
        if (inGilde) {
            ItemStack myGildeItem = new ItemStack(Material.GOLDEN_HELMET);
            ItemMeta myGildeMeta = myGildeItem.getItemMeta();
            myGildeMeta.setDisplayName("§6Meine Gilde");
            List<String> myGildeLore = new ArrayList<>();
            myGildeLore.add("§7Klicke, um deine Gilde zu verwalten");
            myGildeMeta.setLore(myGildeLore);
            myGildeItem.setItemMeta(myGildeMeta);
            inventory.setItem(15, myGildeItem);
        }
        
        player.openInventory(inventory);
    }
    
    // Gilde erstellen Menü
    public void openCreateMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "§6Gilde erstellen");
        
        // Bestätigen Item
        ItemStack confirmItem = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName("§aBestätigen");
        List<String> confirmLore = new ArrayList<>();
        confirmLore.add("§7Klicke, um die Gilde zu erstellen");
        confirmLore.add("§7Name: §f<Name eingeben>");
        confirmLore.add("§7Kürzel: §f<Kürzel eingeben>");
        confirmMeta.setLore(confirmLore);
        confirmItem.setItemMeta(confirmMeta);
        inventory.setItem(15, confirmItem);
        
        // Abbrechen Item
        ItemStack cancelItem = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName("§cAbbrechen");
        List<String> cancelLore = new ArrayList<>();
        cancelLore.add("§7Klicke, um abzubrechen");
        cancelMeta.setLore(cancelLore);
        cancelItem.setItemMeta(cancelMeta);
        inventory.setItem(11, cancelItem);
        
        player.openInventory(inventory);
    }
    
    // Gilden Liste Menü
    public void openGildenListMenu(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, "§6Gilden Liste - Seite " + page);
        
        // Hole alle Gilden
        List<Gilde> gilden = plugin.getGildeManager().getAllGilden();
        
        // Berechne Start- und Endindex für die aktuelle Seite
        int start = (page - 1) * 45;
        int end = Math.min(start + 45, gilden.size());
        
        // Füge Gilden hinzu
        for (int i = start; i < end; i++) {
            Gilde gilde = gilden.get(i);
            
            ItemStack gildeItem = new ItemStack(Material.BOOK);
            ItemMeta gildeMeta = gildeItem.getItemMeta();
            gildeMeta.setDisplayName("§6" + gilde.getName() + " §8[§6" + gilde.getKuerzel() + "§8]");
            
            List<String> gildeLore = new ArrayList<>();
            gildeLore.add("§7Erstellt am: §f" + gilde.getFormattedErstellungsdatum());
            gildeLore.add("§7Beitreten: §f" + (gilde.isOffen() ? "Offen" : "Nur mit Anfrage"));
            gildeLore.add("§7Mitglieder: §f" + plugin.getGildeManager().getGildeMitglieder(gilde.getId()).size());
            gildeLore.add("");
            gildeLore.add("§7Klicke, um beizutreten oder eine Anfrage zu stellen");
            
            gildeMeta.setLore(gildeLore);
            gildeItem.setItemMeta(gildeMeta);
            
            inventory.setItem(i - start, gildeItem);
        }
        
        // Navigations-Items
        if (page > 1) {
            ItemStack prevItem = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevItem.getItemMeta();
            prevMeta.setDisplayName("§6Vorherige Seite");
            prevItem.setItemMeta(prevMeta);
            inventory.setItem(45, prevItem);
        }
        
        if (end < gilden.size()) {
            ItemStack nextItem = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextItem.getItemMeta();
            nextMeta.setDisplayName("§6Nächste Seite");
            nextItem.setItemMeta(nextMeta);
            inventory.setItem(53, nextItem);
        }
        
        // Zurück-Item
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        backItem.setItemMeta(backMeta);
        inventory.setItem(49, backItem);
        
        player.openInventory(inventory);
    }
    
    // Meine Gilde Menü
    public void openMyGildeMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "§6Meine Gilde");
        
        // Hole die Gilde des Spielers
        Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
        if (gilde == null) {
            player.closeInventory();
            player.sendMessage("§cDu bist in keiner Gilde!");
            return;
        }
        
        // Gilde Info Item
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6" + gilde.getName() + " §8[§6" + gilde.getKuerzel() + "§8]");
        
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Erstellt am: §f" + gilde.getFormattedErstellungsdatum());
        infoLore.add("§7Beitreten: §f" + (gilde.isOffen() ? "Offen" : "Nur mit Anfrage"));
        infoLore.add("§7Mitglieder: §f" + plugin.getGildeManager().getGildeMitglieder(gilde.getId()).size());
        
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Mitglieder anzeigen Item
        ItemStack membersItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta membersMeta = membersItem.getItemMeta();
        membersMeta.setDisplayName("§6Mitglieder");
        List<String> membersLore = new ArrayList<>();
        membersLore.add("§7Klicke, um alle Mitglieder anzuzeigen");
        membersMeta.setLore(membersLore);
        membersItem.setItemMeta(membersMeta);
        inventory.setItem(11, membersItem);
        
        // Prüfe, ob der Spieler der Ersteller ist
        boolean isErsteller = gilde.getErsteller().equals(player.getUniqueId());
        
        // Einstellungen Item (nur für Ersteller)
        if (isErsteller) {
            ItemStack settingsItem = new ItemStack(Material.REDSTONE_TORCH);
            ItemMeta settingsMeta = settingsItem.getItemMeta();
            settingsMeta.setDisplayName("§6Einstellungen");
            List<String> settingsLore = new ArrayList<>();
            settingsLore.add("§7Klicke, um die Gildeneinstellungen zu ändern");
            settingsMeta.setLore(settingsLore);
            settingsItem.setItemMeta(settingsMeta);
            inventory.setItem(15, settingsItem);
        }
        
        // Anfragen Item (nur für Ersteller und Älteste)
        GildeMitglied mitglied = plugin.getGildeManager().getMitglied(gilde.getId(), player.getUniqueId());
        if (mitglied != null && (mitglied.getRang() == GildeRang.ANFÜHRER || mitglied.getRang() == GildeRang.ÄLTESTER)) {
            ItemStack requestsItem = new ItemStack(Material.PAPER);
            ItemMeta requestsMeta = requestsItem.getItemMeta();
            requestsMeta.setDisplayName("§6Beitrittsanfragen");
            
            List<String[]> anfragen = plugin.getGildeManager().getBeitrittsanfragen(gilde.getId());
            
            List<String> requestsLore = new ArrayList<>();
            requestsLore.add("§7Offene Anfragen: §f" + anfragen.size());
            requestsLore.add("§7Klicke, um Anfragen zu verwalten");
            
            requestsMeta.setLore(requestsLore);
            requestsItem.setItemMeta(requestsMeta);
            
            inventory.setItem(13, requestsItem);
        }
        
        // Verlassen Item (nicht für Ersteller)
        if (!isErsteller) {
            ItemStack leaveItem = new ItemStack(Material.RED_WOOL);
            ItemMeta leaveMeta = leaveItem.getItemMeta();
            leaveMeta.setDisplayName("§cGilde verlassen");
            List<String> leaveLore = new ArrayList<>();
            leaveLore.add("§7Klicke, um die Gilde zu verlassen");
            leaveMeta.setLore(leaveLore);
            leaveItem.setItemMeta(leaveMeta);
            inventory.setItem(22, leaveItem);
        } else {
            // Löschen Item (nur für Ersteller)
            ItemStack deleteItem = new ItemStack(Material.BARRIER);
            ItemMeta deleteMeta = deleteItem.getItemMeta();
            deleteMeta.setDisplayName("§cGilde löschen");
            List<String> deleteLore = new ArrayList<>();
            deleteLore.add("§7Klicke, um die Gilde zu löschen");
            deleteLore.add("§c§lAchtung: Diese Aktion kann nicht rückgängig gemacht werden!");
            deleteMeta.setLore(deleteLore);
            deleteItem.setItemMeta(deleteMeta);
            inventory.setItem(22, deleteItem);
        }
        
        // Zurück-Item
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        backItem.setItemMeta(backMeta);
        inventory.setItem(18, backItem);
        
        player.openInventory(inventory);
    }
    
    // Mitglieder Menü
    public void openMembersMenu(Player player, int gildenId) {
        Inventory inventory = Bukkit.createInventory(null, 54, "§6Gildenmitglieder");
        
        // Hole die Gilde
        Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
        if (gilde == null || gilde.getId() != gildenId) {
            player.closeInventory();
            player.sendMessage("§cDu bist nicht in dieser Gilde!");
            return;
        }
        
        // Hole alle Mitglieder
        List<GildeMitglied> mitglieder = plugin.getGildeManager().getGildeMitglieder(gildenId);
        
        // Sortiere Mitglieder nach Rang
        mitglieder.sort((m1, m2) -> {
            if (m1.getRang() == m2.getRang()) {
                return m1.getSpielerName().compareTo(m2.getSpielerName());
            }
            return m1.getRang().ordinal() - m2.getRang().ordinal();
        });
        
        // Füge Mitglieder hinzu
        for (int i = 0; i < mitglieder.size() && i < 45; i++) {
            GildeMitglied mitglied = mitglieder.get(i);
            
            ItemStack memberItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta memberMeta = (SkullMeta) memberItem.getItemMeta();
            memberMeta.setDisplayName("§6" + mitglied.getSpielerName());
            
            List<String> memberLore = new ArrayList<>();
            memberLore.add("§7Rang: §f" + getRangDisplayName(mitglied.getRang()));
            memberLore.add("§7Beigetreten am: §f" + mitglied.getFormattedBeitrittsdatum());
            
            // Prüfe, ob der Spieler der Ersteller ist und das Mitglied nicht der Ersteller ist
            if (gilde.getErsteller().equals(player.getUniqueId()) && !gilde.getErsteller().equals(mitglied.getSpielerUuid())) {
                memberLore.add("");
                memberLore.add("§7Klicke, um den Rang zu ändern");
            }
            
            memberMeta.setLore(memberLore);
            memberItem.setItemMeta(memberMeta);
            
            inventory.setItem(i, memberItem);
        }
        
        // Zurück-Item
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        backItem.setItemMeta(backMeta);
        inventory.setItem(49, backItem);
        
        player.openInventory(inventory);
    }
    
    // Einstellungen Menü
    public void openSettingsMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "§6Gildeneinstellungen");
        
        // Hole die Gilde des Spielers
        Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
        if (gilde == null) {
            player.closeInventory();
            player.sendMessage("§cDu bist in keiner Gilde!");
            return;
        }
        
        // Prüfe, ob der Spieler der Ersteller ist
        if (!gilde.getErsteller().equals(player.getUniqueId())) {
            player.closeInventory();
            player.sendMessage("§cNur der Gildenersteller kann die Einstellungen ändern!");
            return;
        }
        
        // Beitrittseinstellungen Item
        ItemStack joinItem = new ItemStack(gilde.isOffen() ? Material.LIME_WOOL : Material.RED_WOOL);
        ItemMeta joinMeta = joinItem.getItemMeta();
        joinMeta.setDisplayName("§6Beitrittseinstellungen");
        
        List<String> joinLore = new ArrayList<>();
        joinLore.add("§7Aktuell: §f" + (gilde.isOffen() ? "Offen (Jeder kann beitreten)" : "Geschlossen (Nur mit Anfrage)"));
        joinLore.add("");
        joinLore.add("§7Klicke, um zu " + (gilde.isOffen() ? "schließen" : "öffnen"));
        
        joinMeta.setLore(joinLore);
        joinItem.setItemMeta(joinMeta);
        
        inventory.setItem(13, joinItem);
        
        // Zurück-Item
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        backItem.setItemMeta(backMeta);
        inventory.setItem(18, backItem);
        
        player.openInventory(inventory);
    }
    
    // Anfragen Menü
    public void openRequestsMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "§6Beitrittsanfragen");
        
        // Hole die Gilde des Spielers
        Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
        if (gilde == null) {
            player.closeInventory();
            player.sendMessage("§cDu bist in keiner Gilde!");
            return;
        }
        
        // Prüfe, ob der Spieler der Ersteller oder ein Ältester ist
        GildeMitglied mitglied = plugin.getGildeManager().getMitglied(gilde.getId(), player.getUniqueId());
        if (mitglied == null || (mitglied.getRang() != GildeRang.ANFÜHRER && mitglied.getRang() != GildeRang.ÄLTESTER)) {
            player.closeInventory();
            player.sendMessage("§cNur der Gildenersteller oder Älteste können Anfragen verwalten!");
            return;
        }
        
        // Hole alle Anfragen
        List<String[]> anfragen = plugin.getGildeManager().getBeitrittsanfragen(gilde.getId());
        
        // Füge Anfragen hinzu
        for (int i = 0; i < anfragen.size() && i < 45; i++) {
            String[] anfrage = anfragen.get(i);
            String spielerUuid = anfrage[0];
            String spielerName = anfrage[1];
            String anfragedatum = anfrage[2];
            
            ItemStack requestItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta requestMeta = (SkullMeta) requestItem.getItemMeta();
            requestMeta.setDisplayName("§6" + spielerName);
            
            List<String> requestLore = new ArrayList<>();
            requestLore.add("§7Anfrage vom: §f" + anfragedatum);
            requestLore.add("");
            requestLore.add("§aLinksklick: Annehmen");
            requestLore.add("§cRechtsklick: Ablehnen");
            
            requestMeta.setLore(requestLore);
            requestItem.setItemMeta(requestMeta);
            
            inventory.setItem(i, requestItem);
        }
        
        // Zurück-Item
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        backItem.setItemMeta(backMeta);
        inventory.setItem(49, backItem);
        
        player.openInventory(inventory);
    }
    
    // Rang ändern Menü
    public void openChangeRangMenu(Player player, UUID targetUuid) {
        Inventory inventory = Bukkit.createInventory(null, 27, "§6Rang ändern");
        
        // Hole die Gilde des Spielers
        Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
        if (gilde == null) {
            player.closeInventory();
            player.sendMessage("§cDu bist in keiner Gilde!");
            return;
        }
        
        // Prüfe, ob der Spieler der Ersteller ist
        if (!gilde.getErsteller().equals(player.getUniqueId())) {
            player.closeInventory();
            player.sendMessage("§cNur der Gildenersteller kann Ränge ändern!");
            return;
        }
        
        // Hole das Zielmitglied
        GildeMitglied targetMitglied = plugin.getGildeManager().getMitglied(gilde.getId(), targetUuid);
        if (targetMitglied == null) {
            player.closeInventory();
            player.sendMessage("§cDieses Mitglied ist nicht in deiner Gilde!");
            return;
        }
        
        // Prüfe, ob das Zielmitglied nicht der Ersteller ist
        if (gilde.getErsteller().equals(targetUuid)) {
            player.closeInventory();
            player.sendMessage("§cDer Rang des Erstellers kann nicht geändert werden!");
            return;
        }
        
        // Ältester Item
        ItemStack aeltesterItem = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta aeltesterMeta = aeltesterItem.getItemMeta();
        aeltesterMeta.setDisplayName("§6Ältester");
        List<String> aeltesterLore = new ArrayList<>();
        aeltesterLore.add("§7Kann Anfragen verwalten");
        aeltesterLore.add("");
        aeltesterLore.add("§7Klicke, um diesen Rang zuzuweisen");
        aeltesterMeta.setLore(aeltesterLore);
        aeltesterItem.setItemMeta(aeltesterMeta);
        inventory.setItem(11, aeltesterItem);
        
        // Anwärter Item
        ItemStack anwaerterItem = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta anwaerterMeta = anwaerterItem.getItemMeta();
        anwaerterMeta.setDisplayName("§6Anwärter");
        List<String> anwaerterLore = new ArrayList<>();
        anwaerterLore.add("§7Standardrang für neue Mitglieder");
        anwaerterLore.add("");
        anwaerterLore.add("§7Klicke, um diesen Rang zuzuweisen");
        anwaerterMeta.setLore(anwaerterLore);
        anwaerterItem.setItemMeta(anwaerterMeta);
        inventory.setItem(15, anwaerterItem);
        
        // Zurück-Item
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        backItem.setItemMeta(backMeta);
        inventory.setItem(22, backItem);
        
        player.openInventory(inventory);
    }
    
    // Hilfsmethode für Rang-Anzeigenamen
    private String getRangDisplayName(GildeRang rang) {
        switch (rang) {
            case ANFÜHRER:
                return "Anführer";
            case ÄLTESTER:
                return "Ältester";
            case ANWÄRTER:
                return "Anwärter";
            default:
                return "Unbekannt";
        }
    }
}
