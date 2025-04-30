package io.sicfran.quickSleep.data;

import io.sicfran.quickSleep.QuickSleep;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataManager {

    private final QuickSleep plugin;
    private final FileConfiguration playerDataConfig;
    private final File playerDataFile;

    public PlayerDataManager(QuickSleep plugin, File pluginDataFolder){
        this.plugin = plugin;

        if(!pluginDataFolder.exists()){
            boolean success = pluginDataFolder.mkdirs();
        }

        playerDataFile = new File(pluginDataFolder, "player_data.yml");
        if(!playerDataFile.exists()){
            try{
                boolean success = playerDataFile.createNewFile();
            } catch (IOException e){
                plugin.getLogger().severe("Failed to create player_data.yml");
                plugin.onDisable();
            }
        }

        this.playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public void savePlayerData(UUID playerId, String sleepMessage){
        playerDataConfig.set("players." + playerId + ".sleepMessage", sleepMessage);
        savePlayerDataFile();
    }

    public String loadPlayerData(UUID playerId){
        return playerDataConfig.getString("players." + playerId + ".sleepMessage", "Good morning!");
    }

    private void savePlayerDataFile() {
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e){
            plugin.getLogger().severe("Failed to save player_data.yml");
            plugin.onDisable();
        }
    }

}
