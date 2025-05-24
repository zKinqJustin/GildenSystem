package de.justindev.gildensystem.listeners;

import de.justindev.gildensystem.GildenSystem;
import de.justindev.gildensystem.gui.GildeGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    private final GildenSystem plugin;
    private final GildeGUI gildeGUI;
    private Map<UUID, String> gildeCreationProcess;

    public ChatListener(GildenSystem plugin) {
        this.plugin = plugin;
        this.gildeGUI = new GildeGUI(plugin);
        this.gildeCreationProcess = new HashMap<>();
    }
    
    // Diese Methode wird vom GildenSystem aufgerufen, um die Map zu setzen
    public void setGildeCreationProcess(Map<UUID, String> process) {
        this.gildeCreationProcess = process;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String message = event.getMessage();

        // Nutze die lokale Map, die vom GildenSystem gesetzt wurde
        Map<UUID, String> creationProcess = this.gildeCreationProcess;
        
        if (creationProcess.containsKey(uuid)) {
            event.setCancelled(true);
            
            String step = creationProcess.get(uuid);
            
            if (step.equals("NAME")) {
                // Speichere den Namen und frage nach dem Kürzel
                if (message.length() < 3 || message.length() > 20) {
                    player.sendMessage("§cDer Gildenname muss zwischen 3 und 20 Zeichen lang sein!");
                    return;
                }
                
                creationProcess.put(uuid, "NAME:" + message);
                player.sendMessage("§6Bitte gib das Kürzel deiner Gilde im Chat ein (max. 5 Zeichen):");
            } else if (step.startsWith("NAME:")) {
                // Speichere das Kürzel und erstelle die Gilde
                String name = step.substring(5);
                String kuerzel = message;
                
                if (kuerzel.length() < 1 || kuerzel.length() > 5) {
                    player.sendMessage("§cDas Gildenkürzel muss zwischen 1 und 5 Zeichen lang sein!");
                    return;
                }
                
                // Erstelle die Gilde
                if (plugin.getGildeManager().createGilde(name, kuerzel, player)) {
                    player.sendMessage("§aDie Gilde §6" + name + " §8[§6" + kuerzel + "§8] §awurde erfolgreich erstellt!");
                } else {
                    player.sendMessage("§cFehler beim Erstellen der Gilde! Möglicherweise existiert bereits eine Gilde mit diesem Namen oder Kürzel.");
                }
                
                // Entferne den Prozess
                creationProcess.remove(uuid);
            }
        } else {
            // Füge das Gildenpräfix zum Chat hinzu
            String prefix = plugin.getGildeManager().getGildenPrefix(uuid);
            event.setFormat(prefix + "%s: %s");
        }
    }
}
