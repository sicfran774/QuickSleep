
package io.sicfran.quickSleep;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.sicfran.quickSleep.data.PlayerDataManager;
import io.sicfran.quickSleep.listeners.OnPlayerBedEnter;
import io.sicfran.quickSleep.listeners.OnPlayerBedLeave;
import io.sicfran.quickSleep.tools.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import io.sicfran.quickSleep.commands.CommandTree;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class QuickSleep extends JavaPlugin implements Listener {

    public static final String VERSION = "1.1.1";
    public static final String DEFAULT_WAKEUP = "<b><yellow>Good morning!</yellow></b>";
    public static final String DEFAULT_CANCEL = "<b>Boooo!!!</b>";

    private final PlayerDataManager playerData;
    private final Set<UUID> sleepingPlayers;
    private boolean sleepTimerStarted;

    public QuickSleep(){
        super();

        playerData = new PlayerDataManager(this, this.getDataFolder());
        sleepingPlayers = new HashSet<>();
        sleepTimerStarted = false;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {
        //create config
        saveDefaultConfig();
        //get missing keys due to update
        getConfig().options().copyDefaults(true);
        saveConfig();

        //initialize bstats metrics
        int pluginId = 25667;
        new Metrics(this, pluginId);

        //initialize and register all listeners
        registerListeners();

        //register commands
        this.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS, commands ->
                        commands.registrar().register(new CommandTree(this).createCommand().build()));

        //successful
        getLogger().info("QuickSleep " + VERSION + " successfully loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().info("QuickSleep " + VERSION + " has been disabled.");
    }

    private void registerListeners(){
        Bukkit.getPluginManager().registerEvents(new OnPlayerBedEnter(this), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerBedLeave(this), this);
    }

    public void addSleeper(UUID player) {
        sleepingPlayers.add(player);
    }

    public void removeSleeper(UUID player) {
        sleepingPlayers.remove(player);
    }

    public Set<UUID> getSleepingPlayers() {
        return sleepingPlayers;
    }

    public void clearSleepers(){
        sleepingPlayers.clear();
    }

    public boolean isSleepTimerStarted() {
        return sleepTimerStarted;
    }

    public void setSleepTimerStarted(boolean sleepTimerStarted) {
        this.sleepTimerStarted = sleepTimerStarted;
    }

    public PlayerDataManager getPlayerData() {
        return playerData;
    }
}
