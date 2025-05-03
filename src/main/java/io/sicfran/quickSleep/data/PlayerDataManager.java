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
            }
        }

        this.playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public void savePlayerWakeupMsg(UUID playerId, String wakeupMessage){
        playerDataConfig.set("players." + playerId + ".message.wakeup", wakeupMessage);
        savePlayerDataFile();
    }

    public void savePlayerCancelMsg(UUID playerId, String cancelMessage){
        playerDataConfig.set("players." + playerId + ".message.cancel", cancelMessage);
        savePlayerDataFile();
    }

    public PlayerData loadPlayerData(UUID playerId){
        return new PlayerData(
                playerDataConfig.getString("players." + playerId + ".message.wakeup", QuickSleep.DEFAULT_WAKEUP),
                playerDataConfig.getString("players." + playerId + ".message.cancel", QuickSleep.DEFAULT_CANCEL)
        );
    }

    private void savePlayerDataFile() {
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e){
            plugin.getLogger().severe("Failed to save player_data.yml");
        }
    }

    public record PlayerData(String wakeupMessage, String cancelMessage) { }
}
