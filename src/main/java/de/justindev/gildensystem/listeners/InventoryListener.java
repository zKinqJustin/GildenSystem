package de.justindev.gildensystem.listeners;

import de.justindev.gildensystem.GildenSystem;
import de.justindev.gildensystem.gui.GildeGUI;
import de.justindev.gildensystem.models.Gilde;
import de.justindev.gildensystem.models.GildeMitglied;
import de.justindev.gildensystem.models.GildeRang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final GildenSystem plugin;
    private final GildeGUI gildeGUI;
    private final Map<UUID, String> gildeCreationProcess;
    private final Map<UUID, UUID> rangChangeProcess;
    
    // Getter für den ChatListener
    public Map<UUID, String> getGildeCreationProcess() {
        return gildeCreationProcess;
    }

    public InventoryListener(GildenSystem plugin) {
        this.plugin = plugin;
        this.gildeGUI = new GildeGUI(plugin);
        this.gildeCreationProcess = new HashMap<>();
        this.rangChangeProcess = new HashMap<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("§6") && event.getCurrentItem() != null) {
            event.setCancelled(true);
            
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            String inventoryTitle = event.getView().getTitle();
            
            // Hauptmenü
            if (inventoryTitle.equals("§6Gilden System")) {
                handleMainMenuClick(player, clickedItem);
            }
            // Gilde erstellen Menü
            else if (inventoryTitle.equals("§6Gilde erstellen")) {
                handleCreateMenuClick(player, clickedItem);
            }
            // Gilden Liste Menü
            else if (inventoryTitle.startsWith("§6Gilden Liste")) {
                handleGildenListMenuClick(player, clickedItem, event.getRawSlot());
            }
            // Meine Gilde Menü
            else if (inventoryTitle.equals("§6Meine Gilde")) {
                handleMyGildeMenuClick(player, clickedItem);
            }
            // Gildenmitglieder Menü
            else if (inventoryTitle.equals("§6Gildenmitglieder")) {
                handleMembersMenuClick(player, clickedItem, event.getRawSlot());
            }
            // Gildeneinstellungen Menü
            else if (inventoryTitle.equals("§6Gildeneinstellungen")) {
                handleSettingsMenuClick(player, clickedItem);
            }
            // Beitrittsanfragen Menü
            else if (inventoryTitle.equals("§6Beitrittsanfragen")) {
                handleRequestsMenuClick(player, clickedItem, event.getRawSlot(), event.isRightClick());
            }
            // Rang ändern Menü
            else if (inventoryTitle.equals("§6Rang ändern")) {
                handleChangeRangMenuClick(player, clickedItem);
            }
        }
    }

    private void handleMainMenuClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.SHIELD) {
            // Gilde erstellen
            gildeGUI.openCreateMenu(player);
            
            // Starte den Erstellungsprozess
            player.closeInventory();
            player.sendMessage("§6Bitte gib den Namen deiner Gilde im Chat ein:");
            gildeCreationProcess.put(player.getUniqueId(), "NAME");
        } else if (clickedItem.getType() == Material.CHEST) {
            // Gilden Liste anzeigen
            gildeGUI.openGildenListMenu(player, 1);
        } else if (clickedItem.getType() == Material.GOLDEN_HELMET) {
            // Meine Gilde anzeigen
            gildeGUI.openMyGildeMenu(player);
        }
    }

    private void handleCreateMenuClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.LIME_WOOL) {
            // Bestätigen (wird nicht verwendet, da wir den Chat für die Eingabe nutzen)
            player.closeInventory();
        } else if (clickedItem.getType() == Material.RED_WOOL) {
            // Abbrechen
            gildeGUI.openMainMenu(player);
        }
    }

    private void handleGildenListMenuClick(Player player, ItemStack clickedItem, int slot) {
        if (clickedItem.getType() == Material.BOOK) {
            // Gilde beitreten
            if (plugin.getGildeManager().isPlayerInGilde(player.getUniqueId())) {
                player.sendMessage("§cDu bist bereits in einer Gilde!");
                return;
            }
            
            // Hole alle Gilden
            Gilde selectedGilde = null;
            int page = Integer.parseInt(player.getOpenInventory().getTitle().split("Seite ")[1]);
            int index = slot + (page - 1) * 45;
            
            try {
                selectedGilde = plugin.getGildeManager().getAllGilden().get(index);
            } catch (IndexOutOfBoundsException e) {
                player.sendMessage("§cFehler beim Auswählen der Gilde!");
                return;
            }
            
            if (selectedGilde != null) {
                if (selectedGilde.isOffen()) {
                    // Direkt beitreten
                    if (plugin.getGildeManager().joinGilde(selectedGilde.getId(), player)) {
                        player.closeInventory();
                        player.sendMessage("§aDu bist der Gilde §6" + selectedGilde.getName() + " §abeigetreten!");
                    } else {
                        player.sendMessage("§cFehler beim Beitreten der Gilde!");
                    }
                } else {
                    // Anfrage stellen
                    if (plugin.getGildeManager().createBeitrittsanfrage(selectedGilde.getId(), player)) {
                        player.closeInventory();
                        player.sendMessage("§aDu hast eine Beitrittsanfrage an die Gilde §6" + selectedGilde.getName() + " §agestellt!");
                    } else {
                        player.sendMessage("§cFehler beim Stellen der Beitrittsanfrage!");
                    }
                }
            }
        } else if (clickedItem.getType() == Material.ARROW) {
            // Navigation
            int page = Integer.parseInt(player.getOpenInventory().getTitle().split("Seite ")[1]);
            
            if (clickedItem.getItemMeta().getDisplayName().equals("§6Vorherige Seite")) {
                gildeGUI.openGildenListMenu(player, page - 1);
            } else if (clickedItem.getItemMeta().getDisplayName().equals("§6Nächste Seite")) {
                gildeGUI.openGildenListMenu(player, page + 1);
            }
        } else if (clickedItem.getType() == Material.BARRIER) {
            // Zurück
            gildeGUI.openMainMenu(player);
        }
    }

    private void handleMyGildeMenuClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            // Mitglieder anzeigen
            Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
            if (gilde != null) {
                gildeGUI.openMembersMenu(player, gilde.getId());
            }
        } else if (clickedItem.getType() == Material.REDSTONE_TORCH) {
            // Einstellungen
            gildeGUI.openSettingsMenu(player);
        } else if (clickedItem.getType() == Material.PAPER) {
            // Anfragen
            gildeGUI.openRequestsMenu(player);
        } else if (clickedItem.getType() == Material.RED_WOOL) {
            // Gilde verlassen
            if (plugin.getGildeManager().leaveGilde(player.getUniqueId())) {
                player.closeInventory();
                player.sendMessage("§aDu hast die Gilde verlassen!");
            } else {
                player.sendMessage("§cFehler beim Verlassen der Gilde!");
            }
        } else if (clickedItem.getType() == Material.BARRIER) {
            // Gilde löschen
            Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
            if (gilde != null) {
                if (plugin.getGildeManager().deleteGilde(gilde.getId(), player.getUniqueId())) {
                    player.closeInventory();
                    player.sendMessage("§aDie Gilde wurde gelöscht!");
                } else {
                    player.sendMessage("§cFehler beim Löschen der Gilde!");
                }
            }
        } else if (clickedItem.getType() == Material.ARROW) {
            // Zurück
            gildeGUI.openMainMenu(player);
        }
    }

    private void handleMembersMenuClick(Player player, ItemStack clickedItem, int slot) {
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            // Rang ändern
            Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
            if (gilde != null && gilde.getErsteller().equals(player.getUniqueId())) {
                // Hole alle Mitglieder
                GildeMitglied selectedMitglied = null;
                
                try {
                    selectedMitglied = plugin.getGildeManager().getGildeMitglieder(gilde.getId()).get(slot);
                } catch (IndexOutOfBoundsException e) {
                    player.sendMessage("§cFehler beim Auswählen des Mitglieds!");
                    return;
                }
                
                if (selectedMitglied != null && !selectedMitglied.getSpielerUuid().equals(gilde.getErsteller())) {
                    // Speichere das ausgewählte Mitglied
                    rangChangeProcess.put(player.getUniqueId(), selectedMitglied.getSpielerUuid());
                    
                    // Öffne das Rang-Ändern-Menü
                    gildeGUI.openChangeRangMenu(player, selectedMitglied.getSpielerUuid());
                }
            }
        } else if (clickedItem.getType() == Material.ARROW) {
            // Zurück
            gildeGUI.openMyGildeMenu(player);
        }
    }

    private void handleSettingsMenuClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.LIME_WOOL || clickedItem.getType() == Material.RED_WOOL) {
            // Beitrittseinstellungen ändern
            Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
            if (gilde != null) {
                boolean newValue = !gilde.isOffen();
                
                if (plugin.getGildeManager().updateGildeSettings(gilde.getId(), player.getUniqueId(), newValue)) {
                    player.sendMessage("§aDie Gildeneinstellungen wurden aktualisiert!");
                    gildeGUI.openSettingsMenu(player);
                } else {
                    player.sendMessage("§cFehler beim Aktualisieren der Gildeneinstellungen!");
                }
            }
        } else if (clickedItem.getType() == Material.ARROW) {
            // Zurück
            gildeGUI.openMyGildeMenu(player);
        }
    }

    private void handleRequestsMenuClick(Player player, ItemStack clickedItem, int slot, boolean isRightClick) {
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            // Anfrage annehmen/ablehnen
            Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
            if (gilde != null) {
                // Hole alle Anfragen
                List<String[]> anfragen = plugin.getGildeManager().getBeitrittsanfragen(gilde.getId());
                
                if (anfragen != null && !anfragen.isEmpty() && slot < anfragen.size()) {
                    String[] anfrage = anfragen.get(slot);
                    UUID targetUuid = UUID.fromString(anfrage[0]);
                    
                    if (isRightClick) {
                        // Ablehnen
                        if (plugin.getGildeManager().denyBeitrittsanfrage(gilde.getId(), player.getUniqueId(), targetUuid)) {
                            player.sendMessage("§aDie Anfrage wurde abgelehnt!");
                            gildeGUI.openRequestsMenu(player);
                        } else {
                            player.sendMessage("§cFehler beim Ablehnen der Anfrage!");
                        }
                    } else {
                        // Annehmen
                        if (plugin.getGildeManager().acceptBeitrittsanfrage(gilde.getId(), player.getUniqueId(), targetUuid)) {
                            player.sendMessage("§aDie Anfrage wurde angenommen!");
                            gildeGUI.openRequestsMenu(player);
                        } else {
                            player.sendMessage("§cFehler beim Annehmen der Anfrage!");
                        }
                    }
                }
            }
        } else if (clickedItem.getType() == Material.ARROW) {
            // Zurück
            gildeGUI.openMyGildeMenu(player);
        }
    }

    private void handleChangeRangMenuClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.GOLDEN_HELMET || clickedItem.getType() == Material.LEATHER_HELMET) {
            // Rang ändern
            UUID targetUuid = rangChangeProcess.get(player.getUniqueId());
            if (targetUuid != null) {
                GildeRang neuerRang = clickedItem.getType() == Material.GOLDEN_HELMET ? GildeRang.ÄLTESTER : GildeRang.ANWÄRTER;
                
                if (plugin.getGildeManager().updateMitgliedRang(player.getUniqueId(), targetUuid, neuerRang)) {
                    player.sendMessage("§aDer Rang wurde aktualisiert!");
                    
                    // Entferne den Prozess
                    rangChangeProcess.remove(player.getUniqueId());
                    
                    // Öffne das Mitglieder-Menü
                    Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
                    if (gilde != null) {
                        gildeGUI.openMembersMenu(player, gilde.getId());
                    } else {
                        player.closeInventory();
                    }
                } else {
                    player.sendMessage("§cFehler beim Aktualisieren des Rangs!");
                }
            }
        } else if (clickedItem.getType() == Material.ARROW) {
            // Zurück
            Gilde gilde = plugin.getGildeManager().getPlayerGilde(player.getUniqueId());
            if (gilde != null) {
                gildeGUI.openMembersMenu(player, gilde.getId());
            } else {
                player.closeInventory();
            }
            
            // Entferne den Prozess
            rangChangeProcess.remove(player.getUniqueId());
        }
    }
}
