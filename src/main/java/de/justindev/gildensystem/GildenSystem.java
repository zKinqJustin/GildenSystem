package de.justindev.gildensystem;

import de.justindev.gildensystem.commands.GildeCommand;
import de.justindev.gildensystem.database.DatabaseManager;
import de.justindev.gildensystem.listeners.ChatListener;
import de.justindev.gildensystem.listeners.InventoryListener;
import de.justindev.gildensystem.managers.GildeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GildenSystem extends JavaPlugin {

    private static GildenSystem instance;
    private DatabaseManager databaseManager;
    private GildeManager gildeManager;
    private InventoryListener inventoryListener;
    private ChatListener chatListener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        // Konfiguration speichern
        saveDefaultConfig();

        // Datenbank-Manager initialisieren
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        databaseManager.createTables();

        // Gilden-Manager initialisieren
        gildeManager = new GildeManager(this);

        // Listener initialisieren und verbinden
        inventoryListener = new InventoryListener(this);
        chatListener = new ChatListener(this);

        // Teile die gildeCreationProcess Map zwischen den Listenern
        chatListener.setGildeCreationProcess(inventoryListener.getGildeCreationProcess());

        // Befehle registrieren
        getCommand("gilde").setExecutor(new GildeCommand(this));

        // Listener registrieren
        getServer().getPluginManager().registerEvents(inventoryListener, this);
        getServer().getPluginManager().registerEvents(chatListener, this);

        getLogger().info("GildenSystem wurde erfolgreich aktiviert!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        getLogger().info("GildenSystem wurde deaktiviert!");
    }

    public static GildenSystem getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public GildeManager getGildeManager() {
        return gildeManager;
    }
}
