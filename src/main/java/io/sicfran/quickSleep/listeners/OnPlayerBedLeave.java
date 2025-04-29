package io.sicfran.quickSleep.listeners;

import io.sicfran.quickSleep.QuickSleep;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class OnPlayerBedLeave implements Listener {

    private final QuickSleep plugin;

    public OnPlayerBedLeave(QuickSleep plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event){
        Player player = event.getPlayer();
        long time = player.getWorld().getTime();
        plugin.removeSleeper(player.getUniqueId());

        // If the player pressed "Leave Bed" (it's still night so sleep wasn't successful)
        // and wasn't interrupted by the command
        if((time >= 12000 && time < 24000) && plugin.isSleepTimerStarted() && plugin.getSleepingPlayers().isEmpty()){
            plugin.setSleepTimerStarted(false);
            final Component message = text()
                    .append(text(player.getName(), color(0x00FFFF)))
                    .append(text(" got up. Nevermind!"))
                    .build();
            plugin.getServer().broadcast(message);
        }
    }
}
