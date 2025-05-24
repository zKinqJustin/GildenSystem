package de.justindev.gildensystem.commands;

import de.justindev.gildensystem.GildenSystem;
import de.justindev.gildensystem.gui.GildeGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GildeCommand implements CommandExecutor {

    private final GildenSystem plugin;
    private final GildeGUI gildeGUI;

    public GildeCommand(GildenSystem plugin) {
        this.plugin = plugin;
        this.gildeGUI = new GildeGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        // Öffne das Hauptmenü
        gildeGUI.openMainMenu(player);
        return true;
    }
}
