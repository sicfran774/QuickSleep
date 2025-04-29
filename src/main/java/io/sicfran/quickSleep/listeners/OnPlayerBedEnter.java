package io.sicfran.quickSleep.listeners;

import io.sicfran.quickSleep.QuickSleep;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class OnPlayerBedEnter implements Listener {

    private final QuickSleep plugin;

    public OnPlayerBedEnter(QuickSleep plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        int playerAmount = plugin.getServer().getOnlinePlayers().size();
        if (playerAmount > 1 && event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {

            Player player = event.getPlayer();
            plugin.addSleeper(player.getUniqueId());

            final Component message = text()
                    .append(text("Type "))
                    .append(text("/sleep confirm", color(0xE63E44)))
                    .append(text(" to turn it daytime."))
                    .build();
            player.sendMessage(message);
        }
    }
}
